package com.gufeng.mixin.protocol;

import com.gufeng.CarpetACMAX;
import com.gufeng.protocol.EasyPlaceContext;
import com.gufeng.settings.ACMAXSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 铁轨行为控制 Mixin（railForceStatePlacement，纯服务端）
 *
 * 仅在“轻松放置”（Litematica V3 协议放置）的事务期间生效：
 * - updateDir / RailState：冻结形状计算，被放置的铁轨保持投影形状，
 *   本次放置引发的邻居更新也不会让周围铁轨变形。
 * - shouldBeRemoved：事务期间全部放行，悬空/无支撑的铁轨（含相邻铁轨）
 *   不会在本次放置中被连带移除。
 *
 * 事务结束（BlockItem.place 返回）后全部恢复原版行为。
 */
@Mixin(BaseRailBlock.class)
public class BaseRailBlockNeighborMixin {

    @Inject(
            method = "updateDir",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onUpdateDir(Level level, BlockPos pos, BlockState state, boolean movedByPiston,
                             CallbackInfoReturnable<BlockState> cir) {
        if (!ACMAXSettings.railForceStatePlacement) return;

        if (EasyPlaceContext.isTransactionActive(level)) {
            if (!level.isClientSide()) {
                CarpetACMAX.LOGGER.info("[ACMAX] freeze updateDir at {} during easy-place transaction", pos);
                cir.setReturnValue(state);
                cir.cancel();
            }
        }
    }

    @Inject(
            method = "shouldBeRemoved",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void onShouldBeRemoved(BlockPos pos, Level level, RailShape shape,
                                          CallbackInfoReturnable<Boolean> cir) {
        if (ACMAXSettings.railForceStatePlacement && EasyPlaceContext.isTransactionActive(level)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}