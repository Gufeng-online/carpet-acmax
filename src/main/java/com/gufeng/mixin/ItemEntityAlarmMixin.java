package com.gufeng.mixin;

import com.gufeng.settings.ACMAXSettings;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * 物品实体积压警报（性能优化版）。
 *
 * 每 100 tick 检查一次，最密集区块的掉落物堆数达到阈值时全服通报。
 * 阈值由规则 itemEntityAlarm 控制：0 关闭；可设 1000/2000/4000/6000 或任意自定义值。
 *
 * 优化点：
 * - 阈值 0、服务器不存在、或没有在线玩家时直接跳过，几乎零开销；
 * - 复用计数表（clear 代替新建），避免每 5 秒产生 GC 压力；
 * - 用坐标位运算生成区块 long 键，避免为每个实体分配 ChunkPos 对象；
 * - 命中阈值后立即停止扫描（短路），警报场景下不再全量遍历。
 */
@Mixin(ServerLevel.class)
public abstract class ItemEntityAlarmMixin extends Level {

    protected ItemEntityAlarmMixin(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey,
                                   RegistryAccess registryAccess, Holder<DimensionType> dimensionType,
                                   boolean bl, boolean bl2, long l, int i) {
        super(writableLevelData, resourceKey, registryAccess, dimensionType, bl, bl2, l, i);
    }

    @Shadow
    public abstract MinecraftServer getServer();

    @Unique
    private static final int INTERVAL = 100;

    @Unique
    private int acmax_tickCounter = 0;

    /** 复用计数表，避免每次检查都新建 HashMap */
    @Unique
    private final Map<Long, Integer> acmax_chunkCounts = new HashMap<>();

    @Inject(method = "tick", at = @At("TAIL"))
    private void onItemEntityAlarmTick(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        int threshold = ACMAXSettings.itemEntityAlarm;
        if (threshold <= 0) return;

        if (++acmax_tickCounter < INTERVAL) return;
        acmax_tickCounter = 0;

        MinecraftServer server = getServer();
        if (server == null || server.getPlayerList().getPlayers().isEmpty()) return;

        Map<Long, Integer> counts = acmax_chunkCounts;
        counts.clear();

        long alarmChunk = 0L;
        int alarmCount = 0;

        for (Entity entity : getEntities().getAll()) {
            if (!(entity instanceof ItemEntity)) continue;

            long key = ChunkPos.pack(entity.getBlockX() >> 4, entity.getBlockZ() >> 4);
            int count = counts.merge(key, 1, Integer::sum);
            if (count > alarmCount) {
                alarmCount = count;
                alarmChunk = key;
                if (alarmCount >= threshold) {
                    // 已有一个区块达到阈值，无需继续扫描
                    break;
                }
            }
        }

        if (alarmCount >= threshold) {
            int chunkX = ChunkPos.getX(alarmChunk);
            int chunkZ = ChunkPos.getZ(alarmChunk);
            String dim = dimension().identifier().getPath();

            Component msg = Component.literal(
                "§6[掉落物警报] §e世界: " + dim
                    + " §c区块: (" + (chunkX * 16) + ", ~, " + (chunkZ * 16) + ")"
                    + " §4数量: " + alarmCount + " 堆 §7(阈值: " + threshold + ")"
            );

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                player.sendSystemMessage(msg);
            }
        }
    }
}