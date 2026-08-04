package com.gufeng.mixin.protocol;

import com.gufeng.protocol.ProtocolHelper;
import com.gufeng.settings.ACMAXSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 拦截 BlockItem.canPlace，跳过 canSurvive 检查（railForceStatePlacement 开启且有协议值时）
 */
@Mixin(BlockItem.class)
public class BlockItemCanPlaceMixin {

    @Inject(
            method = "canPlace",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onCanPlace(BlockPlaceContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (!ACMAXSettings.railForceStatePlacement) return;

        if (!(state.getBlock() instanceof BaseRailBlock)) return;

        Vec3 hitPos = context.getClickLocation();
        BlockPos blockPos = context.getClickedPos();

        if (!ProtocolHelper.hasProtocolValue(hitPos, blockPos)) return;

        // 有协议值 → 这是投影放置 → 跳过 canSurvive 检查
        // 但仍保留碰撞箱检查（isUnobstructed）
        cir.setReturnValue(
                context.getLevel().isUnobstructed(state, blockPos, net.minecraft.world.phys.shapes.CollisionContext.empty())
        );
    }
}
