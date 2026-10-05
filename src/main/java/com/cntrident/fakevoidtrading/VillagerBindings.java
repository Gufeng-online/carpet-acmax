package com.cntrident.fakevoidtrading;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import java.util.*;

public final class VillagerBindings {
    record Binding(UUID villager, ResourceKey<Level> dimension, List<OfferSelection> offers, ServerPlayer owner) {}
    private static final Map<UUID, Binding> BINDINGS = new HashMap<>();
    private VillagerBindings() {}
    static void tick(MinecraftServer server) {
        // Carpet fake disconnects can bypass Fabric's normal network disconnect event.
        BINDINGS.entrySet().removeIf(e -> server.getPlayerList().getPlayer(e.getKey()) != e.getValue().owner());
    }
    private static Villager nearest(ServerPlayer bot) {
        return bot.level().getEntitiesOfClass(Villager.class, bot.getBoundingBox().inflate(5),
                    v -> eligible(v) && v.distanceToSqr(bot) <= 25).stream()
                    .min(Comparator.comparingDouble((Villager v) -> v.distanceToSqr(bot))
                            .thenComparing(v -> v.getUUID().toString())).orElse(null);
    }
    static boolean eligible(Villager villager) {
        return villager.isAlive() && !villager.isBaby() && !villager.isTrading()
                && villager.getOffers().stream().anyMatch(o -> !o.isOutOfStock());
    }
    static int bindFacing(ServerPlayer bot, CommandSourceStack source) {
        var eye = bot.getEyePosition();
        var direction = bot.getViewVector(1.0F).scale(5);
        var end = eye.add(direction);
        var blockHit = bot.level().clip(new net.minecraft.world.level.ClipContext(eye, end,
                net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, bot));
        if (blockHit.getType() != net.minecraft.world.phys.HitResult.Type.MISS) end = blockHit.getLocation();
        var hit = ProjectileUtil.getEntityHitResult(bot, eye, end, bot.getBoundingBox().expandTowards(direction).inflate(1),
                e -> e != bot && e.isPickable() && !e.isSpectator(), eye.distanceToSqr(end));
        Villager target = hit != null && hit.getEntity() instanceof Villager villager
                && eligible(villager) ? villager : nearest(bot);
        if (target == null) {
            source.sendFailure(Component.literal("假人视线及半径 5 格内没有可交易村民")); return 0;
        }
        bind(bot, target, source); return 1;
    }
    private static void bind(ServerPlayer bot, Villager villager, CommandSourceStack source) {
        BINDINGS.put(bot.getUUID(), new Binding(villager.getUUID(), bot.level().dimension(), snapshot(villager), bot));
        villager.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, false, false));
        source.sendSuccess(() -> Component.literal("已将 " + bot.getName().getString() + " 绑定到村民 "
                + villager.getUUID() + "（发光 3 秒）"), false);
        show(bot, villager, source);
    }
    private static List<OfferSelection> snapshot(Villager villager) {
        var list = new ArrayList<OfferSelection>();
        for (int i = 0; i < villager.getOffers().size(); i++) list.add(OfferSelection.capture(i, villager.getOffers().get(i)));
        return List.copyOf(list);
    }
    static Binding get(ServerPlayer bot) {
        var binding = BINDINGS.get(bot.getUUID());
        if (binding != null && binding.owner() != bot) { BINDINGS.remove(bot.getUUID()); return null; }
        return binding;
    }
    static int showBound(ServerPlayer bot, CommandSourceStack source) {
        var binding = get(bot);
        if (binding == null || !binding.dimension().equals(bot.level().dimension())
                || !(bot.level().getEntity(binding.villager()) instanceof Villager villager) || !villager.isAlive()) {
            source.sendFailure(Component.literal("没有当前已加载的绑定村民，请先 /voidtrade " + bot.getName().getString() + " bind")); return 0;
        }
        BINDINGS.put(bot.getUUID(), new Binding(binding.villager(), binding.dimension(), snapshot(villager), bot));
        show(bot, villager, source); return 1;
    }
    static String suggestion(String name, String item, int index) {
        return goalSuggestion(name, index, false);
    }
    static String goalSuggestion(String name, int index, boolean count) {
        return "/voidtrade " + name + " offer " + (index + 1) + (count ? " count " : " cycle ");
    }
    static java.util.stream.Stream<String> itemSuggestions(ServerPlayer bot) {
        var binding = get(bot);
        if (binding == null) return net.minecraft.core.registries.BuiltInRegistries.ITEM.keySet().stream().map(Object::toString);
        if (!binding.dimension().equals(bot.level().dimension())
                || !(bot.level().getEntity(binding.villager()) instanceof Villager villager) || !villager.isAlive())
            return java.util.stream.Stream.empty();
        return itemIds(villager.getOffers()).stream();
    }
    static List<String> itemIds(List<net.minecraft.world.item.trading.MerchantOffer> offers) {
        return offers.stream().filter(o -> !o.isOutOfStock()).map(o ->
                net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(o.getResult().getItem()).toString())
                .distinct().sorted().toList();
    }
    private static void show(ServerPlayer bot, Villager villager, CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("交易选项（悬停查看报价，点击[按轮次]或[按数量]填入指令；末尾填写目标数字）："), false);
        var actualOffers = TradePrices.forBot(villager, bot);
        for (int i = 0; i < actualOffers.size(); i++) {
            var offer = actualOffers.get(i);
            var line = option(bot.getName().getString(), i, offer);
            source.sendSuccess(() -> line, false);
        }
    }
    static Component option(String name, int index, net.minecraft.world.item.trading.MerchantOffer offer) {
            String item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(offer.getResult().getItem()).toString();
            String payment = ChineseTradeNames.stack(offer.getCostA())
                    + (offer.getCostB().isEmpty() ? "" : " + " + ChineseTradeNames.stack(offer.getCostB()));
            var hover = Component.literal("假人当前实际报价：" + payment + " → " + ChineseTradeNames.stack(offer.getResult())
                    + "\n物品 ID：" + item + "\n本次剩余次数：" + Math.max(0, offer.getMaxUses() - offer.getUses())
                    + "\n" + (offer.isOutOfStock() ? "当前已售罄" : "[按轮次]按 cycle 轮数；[按数量]按 count 新成交产物，末尾输入目标数字"));
            var line = Component.literal("[" + (index + 1) + "] " + ChineseTradeNames.stack(offer.getResult())
                    + (offer.isOutOfStock() ? "（已售罄）" : ""));
            line.withStyle(style -> style.withColor(offer.isOutOfStock() ? ChatFormatting.GRAY : ChatFormatting.GREEN)
                    .withHoverEvent(new HoverEvent.ShowText(hover))
                    .withClickEvent(null));
            for (boolean count : new boolean[]{false, true}) {
                line.append(Component.literal(count ? " [按数量]" : " [按轮次]").withStyle(style -> style
                        .withColor(offer.isOutOfStock() ? ChatFormatting.GRAY : count ? ChatFormatting.AQUA : ChatFormatting.YELLOW)
                        .withHoverEvent(new HoverEvent.ShowText(hover))
                        .withClickEvent(offer.isOutOfStock() ? null : new ClickEvent.SuggestCommand(goalSuggestion(name, index, count)))));
            }
            return line;
    }
    static void disconnect(ServerPlayer player) { BINDINGS.remove(player.getUUID()); }
    static void clear() { BINDINGS.clear(); }
}
