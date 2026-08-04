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
 * 物品实体积压警报。
 * 每 100 tick 检查一次，最密集区块 ≥ 2000 ItemEntity 时全服通报。
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
    private static final int THRESHOLD = 2000;

    @Unique
    private int acmax_tickCounter = 0;

    @Inject(method = "tick", at = @At("TAIL"))
    private void onItemEntityAlarmTick(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        if (!ACMAXSettings.itemEntityAlarm) return;

        acmax_tickCounter++;
        if (acmax_tickCounter < INTERVAL) return;
        acmax_tickCounter = 0;

        String dim = dimension().identifier().getPath();

        // 按 ItemEntity 实例数（堆数）统计
        Map<Long, Integer> chunkMap = new HashMap<>();
        int maxX = 0, maxZ = 0;
        int maxCount = 0;

        for (Entity entity : getEntities().getAll()) {
            if (entity instanceof ItemEntity) {
                long key = entity.chunkPosition().pack();
                int count = chunkMap.merge(key, 1, Integer::sum);
                if (count > maxCount) {
                    maxCount = count;
                    maxX = entity.chunkPosition().x();
                    maxZ = entity.chunkPosition().z();
                }
            }
        }

        if (maxCount >= THRESHOLD) {
            MinecraftServer server = getServer();
            if (server == null) return;

            Component msg = Component.literal(
                "§6[掉落物警报] §e世界: " + dim
                    + " §c区块: (" + (maxX * 16) + ", ~, " + (maxZ * 16) + ")"
                    + " §4数量: " + maxCount + " 堆"
            );

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                player.sendSystemMessage(msg);
            }
        }
    }
}
