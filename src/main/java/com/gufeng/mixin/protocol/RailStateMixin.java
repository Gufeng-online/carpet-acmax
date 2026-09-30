package com.gufeng.mixin.protocol;

import com.gufeng.protocol.EasyPlaceContext;
import com.gufeng.settings.ACMAXSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RailState;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 铁轨图形计算（RailState）事务冻结 —— railForceStatePlacement
 *
 * RailState.place 会基于邻居铁轨重新计算形状，并可能直接改写相邻铁轨的方块状态。
 * 轻松放置事务期间冻结它，保证被放置铁轨保持投影形状、周围铁轨不被连带变形。
 * 事务结束即恢复原版行为。
 */
@Mixin(RailState.class)
public class RailStateMixin {

    @Shadow
    private Level level;

    @Shadow
    private BlockPos pos;

    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void freezeDuringPlacement(boolean powered, boolean movedByPiston, RailShape shape,
                                       CallbackInfoReturnable<RailState> cir) {
        if (ACMAXSettings.railForceStatePlacement && EasyPlaceContext.isTransactionActive(level)) {
            cir.setReturnValue((RailState) (Object) this);
        }
    }
}