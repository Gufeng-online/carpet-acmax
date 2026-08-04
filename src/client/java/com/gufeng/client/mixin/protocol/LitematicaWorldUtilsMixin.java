package com.gufeng.client.mixin.protocol;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DetectorRailBlock;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 客户端 Mixin：注入 Litematica 的 WorldUtils.applyCarpetProtocolHitVec
 * 当放置铁轨时，将投影中的 RailShape 编码到 hitVec
 * 
 * 使用 @Pseudo 实现软依赖（没有 Litematica 时静默跳过）
 */
@Pseudo
@Environment(EnvType.CLIENT)
@Mixin(targets = "fi.dy.masa.litematica.util.WorldUtils", remap = false)
public abstract class LitematicaWorldUtilsMixin {

    @Inject(
            method = "applyCarpetProtocolHitVec",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private static void onApplyCarpetProtocolHitVec(
            BlockPos pos, BlockState state, Vec3 hitVecIn,
            CallbackInfoReturnable<Vec3> cir) {

        Block block = state.getBlock();
        int ordinal;

        if (block instanceof RailBlock rb) {
            ordinal = state.getValue(RailBlock.SHAPE).ordinal();
            ordinal &= 0b0000_1111; // 10种形状
        } else if (block instanceof PoweredRailBlock prb) {
            ordinal = state.getValue(PoweredRailBlock.SHAPE).ordinal();
            ordinal &= 0b0000_0111; // 6种形状
        } else if (block instanceof DetectorRailBlock drb) {
            ordinal = state.getValue(DetectorRailBlock.SHAPE).ordinal();
            ordinal &= 0b0000_0111; // 6种形状
        } else {
            return; // 不是铁轨，不处理
        }

        // 编码到 hitVec
        Vec3 original = cir.getReturnValue();
        double newX = original.x + (double) ((ordinal << 1) + 2);
        cir.setReturnValue(new Vec3(newX, original.y, original.z));
    }
}
