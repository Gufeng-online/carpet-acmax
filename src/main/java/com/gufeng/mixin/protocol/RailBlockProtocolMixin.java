package com.gufeng.mixin.protocol;

import com.gufeng.protocol.ProtocolHelper;
import com.gufeng.protocol.RailProtocol;
import com.gufeng.settings.ACMAXSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DetectorRailBlock;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

/**
 * 统一铁轨 Mixin —— 拦截 BaseRailBlock.getStateForPlacement
 * 覆盖普通铁轨、动力铁轨、探测铁轨三种类型
 *
 * 使用 @ModifyReturnValue 修改返回值的 shape 属性
 */
@Mixin(BaseRailBlock.class)
public class RailBlockProtocolMixin {

    @ModifyReturnValue(
            method = "getStateForPlacement",
            at = @At("RETURN")
    )
    private BlockState onGetStateForPlacement(BlockState original, BlockPlaceContext context) {
        if (!ACMAXSettings.railForceStatePlacement) {
            return original;
        }
        if (original == null) {
            return null;
        }

        Vec3 hitPos = context.getClickLocation();
        BlockPos blockPos = context.getClickedPos();

        if (!ProtocolHelper.hasProtocolValue(hitPos, blockPos)) {
            return original;
        }

        Block block = original.getBlock();
        if (!(block instanceof RailBlock) && !(block instanceof PoweredRailBlock) && !(block instanceof DetectorRailBlock)) {
            return original;
        }

        int protocolValue = ProtocolHelper.decodeProtocolValueFromHitX(hitPos, blockPos);
        RailShape shape;

        if (block instanceof RailBlock) {
            shape = RailProtocol.fromProtocolValue(protocolValue);
        } else {
            shape = RailProtocol.poweredFromProtocolValue(protocolValue);
        }

        BaseRailBlock railBlock = (BaseRailBlock) block;
        return original.setValue(railBlock.getShapeProperty(), shape);
    }
}
