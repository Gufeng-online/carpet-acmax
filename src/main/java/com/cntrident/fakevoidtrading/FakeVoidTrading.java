package com.cntrident.fakevoidtrading;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class FakeVoidTrading implements ModInitializer {
    static final Logger LOGGER = LoggerFactory.getLogger("fake_player_void_trading");
    private static final Map<UUID, TradeSession> SESSIONS = new HashMap<>();
    static boolean isRunning(ServerPlayer bot) { return SESSIONS.containsKey(bot.getUUID()); }
    public static boolean keepsMenu(net.minecraft.world.entity.player.Player player,
            net.minecraft.world.inventory.MerchantMenu menu, net.minecraft.world.item.trading.Merchant merchant) {
        if (!(player instanceof ServerPlayer bot) || !isFake(bot)) return false;
        var session = SESSIONS.get(bot.getUUID());
        return session != null && session.keepsMenu(bot, menu, merchant);
    }
    static boolean isFake(ServerPlayer player) {
        for (Class<?> type = player.getClass(); type != null; type = type.getSuperclass())
            if (type.getName().equals("carpet.patches.EntityPlayerMPFake")) return true;
        return false;
    }
    @Override public void onInitialize() {
        ServerConfig.load();
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
            var start = Commands.literal("start").requires(s -> com.gufeng.settings.ACMAXSettings.fakePlayerVoidTrading)
                    .then(tradeArguments(true));
            dispatcher.register(Commands.literal("voidtrade")
                    .requires(source -> commandPermissionAllowed(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS).test(source)))
                    .then(Commands.argument("bot", EntityArgument.player())
                            .then(start)
                            .then(offerArguments(false).requires(s -> com.gufeng.settings.ACMAXSettings.fakePlayerVoidTrading))
                            .then(tradeArguments(false).requires(s -> com.gufeng.settings.ACMAXSettings.fakePlayerVoidTrading))
                            .then(Commands.literal("bind").requires(s -> com.gufeng.settings.ACMAXSettings.fakePlayerVoidTrading)
                                    .executes(c -> bind(c, false)))
                            .then(Commands.literal("trades").requires(s -> com.gufeng.settings.ACMAXSettings.fakePlayerVoidTrading)
                                    .executes(c -> bind(c, true)))
                            .then(Commands.literal("status").executes(c -> {
                                var bot = EntityArgument.getPlayer(c, "bot");
                                var session = SESSIONS.get(bot.getUUID());
                                c.getSource().sendSuccess(() -> Component.literal(bot.getName().getString() + ": "
                                        + (session == null ? "空闲" : session.status())
                                        + (VillagerBindings.get(bot) == null ? "；未绑定村民" : "；绑定村民 " + VillagerBindings.get(bot).villager())), false); return 1;
                            }))
                            .then(Commands.literal("stop").executes(c -> {
                                var bot = EntityArgument.getPlayer(c, "bot");
                                var session = SESSIONS.remove(bot.getUUID());
                                if (session != null) session.stop(bot, "玩家手动停止");
                                c.getSource().sendSuccess(() -> Component.literal("已停止 " + bot.getName().getString()), false);
                                return 1;
                            }))));
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> VillagerBindings.disconnect(handler.player));
        ServerTickEvents.END_SERVER_TICK.register(FakeVoidTrading::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            SESSIONS.forEach((id, session) -> session.stop(server.getPlayerList().getPlayer(id), "服务器关闭"));
            SESSIONS.clear();
            VillagerBindings.clear();
        });
    }
    private static RequiredArgumentBuilder<CommandSourceStack, Identifier> tradeArguments(boolean legacy) {
        var item = Commands.argument("item", IdentifierArgument.id())
                .suggests((context, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggest(
                        VillagerBindings.itemSuggestions(EntityArgument.getPlayer(context, "bot")), builder));
        var cycles = Commands.argument("cycles", IntegerArgumentType.integer(1, 1000000))
                .executes(c -> start(c, null, -1, !legacy));
        if (legacy) cycles.then(Commands.argument("enchantment", IdentifierArgument.id())
                .executes(c -> start(c, IdentifierArgument.getId(c, "enchantment").toString(), -1, false)));
        else item.then(offerArguments(true));
        return item.then(cycles);
    }
    static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> offerArguments(boolean hasItem) {
        var index = Commands.argument("offer", IntegerArgumentType.integer(1));
        for (boolean count : new boolean[]{false, true}) index.then(Commands.literal(count ? "count" : "cycle")
                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 1000000))
                        .executes(c -> start(c, null, IntegerArgumentType.getInteger(c, "offer") - 1,
                                true, !hasItem, count, IntegerArgumentType.getInteger(c, "amount")))));
        // Previous clickable commands remain valid.
        index.then(Commands.argument("cycles", IntegerArgumentType.integer(1, 1000000))
                .executes(c -> start(c, null, IntegerArgumentType.getInteger(c, "offer") - 1,
                        true, !hasItem, false, IntegerArgumentType.getInteger(c, "cycles"))));
        return Commands.literal("offer").then(index);
    }
    private static int bind(CommandContext<CommandSourceStack> c, boolean list) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var bot = EntityArgument.getPlayer(c, "bot");
        if (!isFake(bot)) return fail(c, "目标必须是 Carpet 假人");
        if (!bot.isAlive()) return fail(c, "假人已死亡");
        if (SESSIONS.containsKey(bot.getUUID())) return fail(c, "假人已在运行，请先 /voidtrade " + bot.getName().getString() + " stop");
        return list ? VillagerBindings.showBound(bot, c.getSource()) : VillagerBindings.bindFacing(bot, c.getSource());
    }
    private static int start(CommandContext<CommandSourceStack> c, String enchantment, int offerIndex, boolean requireBinding) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        return start(c, enchantment, offerIndex, requireBinding, false, false, IntegerArgumentType.getInteger(c, "cycles"));
    }
    private static int start(CommandContext<CommandSourceStack> c, String enchantment, int offerIndex, boolean requireBinding,
                             boolean deriveItem, boolean countMode, int amount) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        if (!com.gufeng.settings.ACMAXSettings.fakePlayerVoidTrading)
            return fail(c, "Carpet 规则 fakePlayerVoidTrading 已关闭");
        var bot = EntityArgument.getPlayer(c, "bot");
        if (!isFake(bot)) return fail(c, "目标必须是 Carpet 假人");
        if (!bot.isAlive()) return fail(c, "假人已死亡");
        if (SESSIONS.containsKey(bot.getUUID())) return fail(c, "假人已在运行，请先 /voidtrade " + bot.getName().getString() + " stop");
        if (bot.containerMenu != bot.inventoryMenu || !bot.inventoryMenu.getCarried().isEmpty())
            return fail(c, "请先关闭假人的容器并清空光标物品");
        var binding = VillagerBindings.get(bot);
        if (requireBinding && binding == null) return fail(c, "请先绑定村民：/voidtrade " + bot.getName().getString() + " bind");
        if (binding != null && !binding.dimension().equals(bot.level().dimension())) return fail(c, "绑定村民不在假人当前维度，请重新 bind");
        OfferSelection selection = null;
        if (offerIndex >= 0) {
            if (binding == null || offerIndex >= binding.offers().size()) return fail(c, "报价编号无效，请重新执行 trades 查看选项");
            selection = binding.offers().get(offerIndex);
        }
        Identifier itemId = deriveItem && selection != null ? BuiltInRegistries.ITEM.getKey(selection.result().getItem())
                : IdentifierArgument.getId(c, "item");
        if (itemId == null || !BuiltInRegistries.ITEM.containsKey(itemId)) return fail(c, "物品 ID 无效");
        var item = BuiltInRegistries.ITEM.getValue(itemId);
        if (item == Items.AIR) return fail(c, "不能交易空气");
        if (selection != null && !selection.result().is(item)) return fail(c, "物品 ID 与所选报价不符，请重新点击交易选项");
        Identifier enchantmentId = enchantment == null ? null : Identifier.tryParse(enchantment);
        if (item == Items.ENCHANTED_BOOK && selection == null && (enchantmentId == null || !bot.level().registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT).containsKey(enchantmentId))) return fail(c, "附魔书必须指定有效附魔 ID");
        if (item != Items.ENCHANTED_BOOK && enchantment != null) return fail(c, "只有附魔书使用附魔参数");
        SESSIONS.put(bot.getUUID(), new TradeSession(bot, c.getSource(), item, enchantmentId, amount,
                binding == null ? null : binding.villager(), selection, countMode));
        c.getSource().sendSuccess(() -> Component.literal("已启动 " + bot.getName().getString() + "，目标 " + amount
                + (countMode ? " 件新成交产物（最后一笔完整成交可能超过目标）" : " 轮")), false);
        return 1;
    }
    private static int fail(CommandContext<CommandSourceStack> c, String message) {
        c.getSource().sendFailure(Component.literal(message)); return 0;
    }
    private static void tick(MinecraftServer server) {
        VillagerBindings.tick(server);
        boolean enabled = com.gufeng.settings.ACMAXSettings.fakePlayerVoidTrading;
        boolean allowNonOp = com.gufeng.settings.ACMAXSettings.fakePlayerVoidTradingAllowNonOp;
        if (enabled != lastEnabled || allowNonOp != lastAllowNonOp) {
            server.getPlayerList().getPlayers().forEach(server.getCommands()::sendCommands);
            lastEnabled = enabled;
            lastAllowNonOp = allowNonOp;
        }
        var iterator = SESSIONS.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next(); var session = entry.getValue();
            var bot = server.getPlayerList().getPlayer(entry.getKey());
            try {
                if (!com.gufeng.settings.ACMAXSettings.fakePlayerVoidTrading) {
                    session.stop(bot, "Carpet 规则 fakePlayerVoidTrading 已关闭"); iterator.remove();
                } else if (bot == null || !bot.isAlive() || !isFake(bot)) {
                    session.stop(bot, "假人离线、死亡或已被真人接管"); iterator.remove();
                } else if (!session.tick(bot)) iterator.remove();
            } catch (RuntimeException exception) {
                LOGGER.error("Fake player void trading failed", exception);
                session.stop(bot, "运行异常，详见服务器日志"); iterator.remove();
            }
        }
    }
    private static boolean lastEnabled;
    private static boolean lastAllowNonOp;
    static boolean commandPermissionAllowed(boolean hasOpPermission) {
        return com.gufeng.settings.ACMAXSettings.fakePlayerVoidTradingAllowNonOp || hasOpPermission;
    }
}
