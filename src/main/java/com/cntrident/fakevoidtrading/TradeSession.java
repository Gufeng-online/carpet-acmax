package com.cntrident.fakevoidtrading;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.Comparator;
import java.util.UUID;

final class TradeSession {
    enum State { SEEK, WAIT_GATEWAY, DELAY, TRADE, STORE, CHEST, HOLD_CHEST, RETURN }
    private final UUID botId;
    private final CommandSourceStack owner;
    private final Item target;
    private final Identifier enchantment;
    private final int requested;
    private final boolean countMode;
    private long produced;
    private final ResourceKey<Level> dimension;
    private State state = State.SEEK;
    private Villager villager;
    private UUID villagerId;
    private AbstractContainerMenu ownedMenu;
    private MerchantOffer offer;
    private int offerIndex;
    private ItemStack costA = ItemStack.EMPTY, costB = ItemStack.EMPTY, result = ItemStack.EMPTY;
    private Vec3 previousPosition;
    private int age, deadline = ServerConfig.get.phaseTimeoutTicks, armedUntil = -1, delayUntil;
    private int completed, trades, pending;
    private boolean jumped;
    private boolean materialsExhausted;
    private boolean resourceNoticeSent;
    private String reason;
    private final OfferSelection selectedOffer;

    TradeSession(ServerPlayer bot, CommandSourceStack owner, Item target, Identifier enchantment, int requested) {
        this(bot, owner, target, enchantment, requested, null, null);
    }
    TradeSession(ServerPlayer bot, CommandSourceStack owner, Item target, Identifier enchantment, int requested,
                 UUID boundVillager, OfferSelection selectedOffer) {
        this(bot, owner, target, enchantment, requested, boundVillager, selectedOffer, false);
    }
    TradeSession(ServerPlayer bot, CommandSourceStack owner, Item target, Identifier enchantment, int requested,
                 UUID boundVillager, OfferSelection selectedOffer, boolean countMode) {
        this.botId = bot.getUUID(); this.owner = owner; this.target = target;
        this.enchantment = enchantment; this.requested = requested;
        this.countMode = countMode;
        this.dimension = bot.level().dimension(); this.previousPosition = bot.position();
        this.villagerId = boundVillager; this.selectedOffer = selectedOffer;
    }
    String status() {
        return reason != null ? reason : "阶段 " + state + "；" + progress()
                + "；交易 " + trades + " 次；本轮待存 " + pending + " 件";
    }
    private String progress() { return countMode ? "新成交产物 " + produced + "/" + requested + " 件；完成 " + completed + " 轮"
            : "完成 " + completed + "/" + requested + " 轮；新成交产物 " + produced + " 件"; }
    private final ReturnGate returnGate = new ReturnGate();
    private void expandBoxes(ServerPlayer bot, boolean forOutput) {
        var inventory = new InventoryStore(bot.getInventory());
        int expanded = forOutput ? inventory.prepareOutput(result) : inventory.preparePayment(costA, costB);
        if (expanded > 0) {
            inventory.commit(bot.getInventory());
            owner.sendSuccess(() -> Component.literal("已展开 " + expanded + " 个堆叠潜影盒，保留原有组件和内容"), false);
        }
    }
    boolean keepsMenu(ServerPlayer bot, MerchantMenu menu, net.minecraft.world.item.trading.Merchant merchant) {
        return reason == null && bot.isAlive() && bot.containerMenu == ownedMenu && menu == ownedMenu
                && merchant == villager && villager.getTradingPlayer() == bot
                && dimension.equals(bot.level().dimension())
                && (state == State.WAIT_GATEWAY || state == State.DELAY || state == State.TRADE || state == State.STORE)
                && (villager.getRemovalReason() == Entity.RemovalReason.UNLOADED_TO_CHUNK
                    || !villager.isRemoved() && villager.isAlive());
    }
    boolean tick(ServerPlayer bot) {
        age++;
        if (!dimension.equals(bot.level().dimension())) return stop(bot, "假人跨维度，流程终止");
        if (age > deadline) return stop(bot, "阶段 " + state + " 超时，请检查折跃门、区块加载和返程装置");
        if (ownedMenu != null && bot.containerMenu != ownedMenu) {
            if (state == State.HOLD_CHEST && bot.containerMenu == bot.inventoryMenu) {
                ownedMenu = null; enter(State.RETURN, ServerConfig.get.phaseTimeoutTicks);
            } else return stop(bot, "自动化菜单被关闭或替换");
        }
        if (ownedMenu == null && bot.containerMenu != bot.inventoryMenu) return stop(bot, "假人被打开了其他容器");
        switch (state) {
            case SEEK -> seek(bot);
            case WAIT_GATEWAY -> {
                if (nearGateway(bot)) armedUntil = age + ServerConfig.get.gatewayArmTicks;
                if (!jumped && age <= armedUntil && bot.position().distanceToSqr(previousPosition) >= 256) {
                    jumped = true;
                    // Start at arrival, so trading and storage consume the same cooldown budget.
                    returnGate.start(age, ServerConfig.get.returnCooldownSeconds);
                }
                if (villager.isRemoved() && villager.getRemovalReason() != Entity.RemovalReason.UNLOADED_TO_CHUNK)
                    return stop(bot, "目标村民非正常卸载（可能已死亡或转移）");
                if (jumped && villager.getRemovalReason() == Entity.RemovalReason.UNLOADED_TO_CHUNK) {
                    delayUntil = age + ServerConfig.get.postTeleportTradeDelayTicks; enter(State.DELAY, 400);
                }
            }
            case DELAY -> { if (age >= delayUntil) enter(State.TRADE, ServerConfig.get.phaseTimeoutTicks); }
            case TRADE -> { if (!trade(bot)) return false; }
            case STORE -> {
                closeOwned(bot);
                if (pending > 0) {
                    expandBoxes(bot, true);
                    var plan = new InventoryStore(bot.getInventory());
                    int stored = plan.storeAvailableInBoxes(result, pending);
                    plan.commit(bot.getInventory());
                    pending -= stored;
                    // Use the available single box before requesting another on the next tick.
                    if (pending > 0 && new InventoryStore(bot.getInventory()).prepareOutput(result) > 0) break;
                    if (pending > 0) owner.sendSuccess(() -> Component.literal("潜影盒容量不足，剩余 "
                            + pending + " 件保留在假人背包；继续流程").withStyle(net.minecraft.ChatFormatting.YELLOW), false);
                    pending = 0;
                }
                completed++;
                if (goalReached(countMode, completed, produced, requested) && !materialsExhausted)
                    return stop(bot, "已达到交易目标");
                returnGate.finishStorage();
                enter(State.CHEST, ServerConfig.get.trappedChestSearchTicks + returnGate.remaining(age));
            }
            case CHEST -> openChest(bot);
            case HOLD_CHEST -> {
                if (age >= delayUntil) { closeOwned(bot); enter(State.RETURN, ServerConfig.get.phaseTimeoutTicks); }
            }
            case RETURN -> {
                Entity loaded = bot.level().getEntity(villagerId);
                if (loaded instanceof Villager next && canReach(bot, next.getEyePosition())) {
                    if (materialsExhausted) return stop(bot, "现有材料已完成所有可支付交易，产物已收纳并完成返程");
                    villager = next; enter(State.SEEK, ServerConfig.get.phaseTimeoutTicks);
                }
            }
        }
        previousPosition = bot.position();
        return reason == null;
    }
    private void seek(ServerPlayer bot) {
        if (villagerId == null) {
            villager = bot.level().getEntitiesOfClass(Villager.class, bot.getBoundingBox().inflate(4.5),
                    v -> eligible(v, bot)).stream().min(Comparator.comparingDouble(v -> v.distanceToSqr(bot))).orElse(null);
        } else {
            var entity = bot.level().getEntity(villagerId);
            villager = entity instanceof Villager v && eligible(v, bot) ? v : null;
        }
        if (villager == null) return;
        int best = -1;
        var offers = villager.getOffers();
        if (selectedOffer != null && (selectedOffer.index() >= offers.size()
                || !selectedOffer.matches(offers.get(selectedOffer.index())))) {
            stop(bot, "绑定报价发生变化，请重新执行 trades 并选择报价"); return;
        }
        for (int i = 0; i < offers.size(); i++) {
            var candidate = offers.get(i);
            if (selectedOffer != null && i != selectedOffer.index()) continue;
            if (!matches(candidate)) continue;
            if (best == -1 || candidate.getCostA().is(Items.EMERALD) && offers.get(best).getCostA().is(Items.EMERALD)
                    && compare(candidate, offers.get(best)) < 0) best = i;
        }
        if (best == -1) { stop(bot, "村民没有未售罄的目标报价"); return; }
        offerIndex = best; offer = offers.get(best);
        villager.mobInteract(bot, InteractionHand.MAIN_HAND);
        if (!(bot.containerMenu instanceof MerchantMenu menu) || villager.getTradingPlayer() != bot) {
            stop(bot, "无法打开目标村民交易菜单"); return;
        }
        ownedMenu = menu; villagerId = villager.getUUID();
        costA = offer.getCostA().copy(); costB = offer.getCostB().copy(); result = offer.getResult().copy();
        expandBoxes(bot, false);
        int affordable = new InventoryStore(bot.getInventory()).affordableTrades(costA, costB);
        if (!resourceNoticeSent) {
            var warning = countMode ? countWarning(requested, result.getCount(), affordable)
                    : resourceWarning(requested, offer.getMaxUses() - offer.getUses(), offer.getMaxUses(), affordable);
            if (warning != null) owner.sendSuccess(() -> Component.literal(warning).withStyle(net.minecraft.ChatFormatting.YELLOW), false);
            resourceNoticeSent = true;
        }
        if (!canPay(new InventoryStore(bot.getInventory()), costA, costB)) {
            stop(bot, "现有材料不足以支付一次完整报价，正常结束（不足一次的材料保留）"); return;
        }
        jumped = false; armedUntil = -1; previousPosition = bot.position();
        enter(State.WAIT_GATEWAY, ServerConfig.get.phaseTimeoutTicks);
    }
    static String resourceWarning(int rounds, int remaining, int perRound, int affordable) {
        long required = remaining + (long) (rounds - 1) * perRound;
        return affordable < required ? "[材料提示] 按当前实际报价，库存可支付 " + affordable + " 次交易，预计不足计划 "
                + rounds + " 轮（约需 " + required + " 次）。仍继续交易，材料不足以支付下一笔时正常收纳、返程并停止；零头保留。" : null;
    }
    static boolean goalReached(boolean count, int rounds, long produced, int target) {
        return count ? produced >= target : rounds >= target;
    }
    static long tradesForCount(int target, int output) { return ((long) target + output - 1) / output; }
    static String countWarning(int target, int output, int affordable) {
        long needed = tradesForCount(target, output);
        return affordable < needed ? "[材料提示] 当前库存可支付 " + affordable + " 次、约产出 " + ((long) affordable * output)
                + " 件，预计不足目标 " + target + " 件。仍尽力交易，材料不足时正常收纳、返程并停止。" : null;
    }
    private static boolean eligible(Villager v, ServerPlayer bot) {
        return v.isAlive() && !v.isBaby() && !v.isTrading() && canReach(bot, v.getEyePosition());
    }
    private boolean matches(MerchantOffer candidate) {
        if (candidate.isOutOfStock() || !candidate.getResult().is(target) || candidate.getCostA().isEmpty()) return false;
        if (enchantment == null) return true;
        var stored = candidate.getResult().get(DataComponents.STORED_ENCHANTMENTS);
        return stored != null && stored.keySet().stream().anyMatch(holder -> holder.unwrapKey()
                .map(key -> key.identifier().equals(enchantment)).orElse(false));
    }
    private static int emeralds(MerchantOffer offer) {
        return offer.getCostA().getCount() + (offer.getCostB().is(Items.EMERALD) ? offer.getCostB().getCount() : 0);
    }
    static int compare(MerchantOffer a, MerchantOffer b) {
        int score = Long.compare((long) emeralds(a) * b.getResult().getCount(), (long) emeralds(b) * a.getResult().getCount());
        return score != 0 ? score : Integer.compare(emeralds(a), emeralds(b));
    }
    private boolean trade(ServerPlayer bot) {
        var menu = (MerchantMenu) ownedMenu;
        if (villager.getRemovalReason() != Entity.RemovalReason.UNLOADED_TO_CHUNK || !menu.stillValid(bot))
            return stop(bot, "村民卸载状态或交易菜单失效");
        if (menu.getOffers().size() <= offerIndex || menu.getOffers().get(offerIndex) != offer
                || !ItemStack.matches(costA, offer.getCostA()) || !ItemStack.matches(costB, offer.getCostB())
                || !ItemStack.matches(result, offer.getResult())) return stop(bot, "报价发生变化");
        if (offer.isOutOfStock()) { enter(State.STORE, 200); return true; }
        if (!menu.getSlot(0).getItem().isEmpty() || !menu.getSlot(1).getItem().isEmpty())
            return stop(bot, "交易输入槽出现了非预期物品");
        expandBoxes(bot, false);
        var plan = new InventoryStore(bot.getInventory());
        if (!canPay(plan, costA, costB)) {
            materialsExhausted = true; enter(State.STORE, 200); return true;
        }
        if (!plan.canReceive(result) && pending > 0) {
            expandBoxes(bot, true);
            var storage = new InventoryStore(bot.getInventory());
            int stored = storage.storeAvailableInBoxes(result, pending);
            if (stored > 0) {
                storage.commit(bot.getInventory()); pending -= stored;
                plan = new InventoryStore(bot.getInventory());
                if (!canPay(plan, costA, costB)) { materialsExhausted = true; enter(State.STORE, 200); return true; }
            }
        }
        if (!plan.canReceive(result)) return stop(bot, "假人背包和潜影盒均无法接收下一笔完整产物；已有产物保留");
        plan.commit(bot.getInventory());
        menu.setSelectionHint(offerIndex);
        menu.getSlot(0).set(costA.copy()); menu.getSlot(1).set(costB.copy());
        if (!ItemStack.matches(menu.getSlot(2).getItem(), result)) return stop(bot, "原版菜单未生成预期交易产物");
        int before = offer.getUses();
        menu.quickMoveStack(bot, 2);
        if (offer.getUses() != before + 1) return stop(bot, "原版交易未完成，输入通过菜单关闭退回");
        pending += result.getCount(); trades++; produced += result.getCount(); menu.broadcastChanges();
        if (countMode && goalReached(true, completed, produced, requested)) enter(State.STORE, 200);
        return true;
    }
    static boolean canPay(InventoryStore plan, ItemStack first, ItemStack second) {
        return plan.take(first) && plan.take(second);
    }
    private void openChest(ServerPlayer bot) {
        if (!returnGate.ready(age)) return;
        BlockPos nearest = null; double distance = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(bot.blockPosition().offset(-4, -4, -4), bot.blockPosition().offset(4, 4, 4))) {
            if (!bot.level().hasChunkAt(pos) || !bot.level().getBlockState(pos).is(Blocks.TRAPPED_CHEST)) continue;
            Vec3 center = Vec3.atCenterOf(pos);
            if (!canReach(bot, center)) continue;
            double d = bot.getEyePosition().distanceToSqr(center);
            if (d < distance) { distance = d; nearest = pos.immutable(); }
        }
        if (nearest == null) return;
        var state = bot.level().getBlockState(nearest);
        state.useWithoutItem(bot.level(), bot, new BlockHitResult(Vec3.atCenterOf(nearest), Direction.UP, nearest, false));
        if (bot.containerMenu == bot.inventoryMenu) return;
        ownedMenu = bot.containerMenu; delayUntil = age + ServerConfig.get.trappedChestOpenTicks; enter(State.HOLD_CHEST, 400);
    }
    private static boolean nearGateway(ServerPlayer bot) {
        for (BlockPos pos : BlockPos.betweenClosed(bot.blockPosition().offset(-2, -2, -2), bot.blockPosition().offset(2, 2, 2)))
            if (bot.level().hasChunkAt(pos) && bot.level().getBlockState(pos).is(Blocks.END_GATEWAY)) return true;
        return false;
    }
    private static boolean canReach(ServerPlayer bot, Vec3 point) {
        if (bot.getEyePosition().distanceToSqr(point) > 20.25) return false;
        var hit = bot.level().clip(new ClipContext(bot.getEyePosition(), point, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, bot));
        return hit.getType() == HitResult.Type.MISS || hit.getLocation().distanceToSqr(point) < 0.8;
    }
    private void enter(State state, int timeout) { this.state = state; deadline = age + timeout; }
    private void closeOwned(ServerPlayer bot) {
        if (bot != null && ownedMenu != null && bot.containerMenu == ownedMenu) bot.closeContainer();
        ownedMenu = null;
    }
    boolean stop(ServerPlayer bot, String reason) {
        closeOwned(bot); this.reason = reason;
        String message = "[虚空交易 " + botId + "] " + reason + "；" + progress() + "，交易 " + trades + " 次";
        owner.sendSuccess(() -> Component.literal(message), false);
        FakeVoidTrading.LOGGER.info(message);
        return false;
    }
}
