package com.gufeng.mixin.protocol;

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
 * 铁轨行为控制 Mixin（railForceStatePlacement）
 *
 * 旧版用“永久标记 + 全局抑制”让被放置铁轨永久保持投影形态，这会导致悬浮铁轨
 * 永不掉落、形状永不变 —— 与需求“周围一更新就恢复”相反。
 *
 * 新版改为【事务级瞬态保护】：仅在轻松放置那一格铁轨的 BlockItem.place 事务期间，
 * 对【该格自身】放行 updateDir / shouldBeRemoved，使强制形状在放置瞬间不被原版
 * RailState 重算覆盖、也不因暂时无支撑而掉落。事务结束保护即消失，
 * 之后周边更新（邻居变化 / 支撑破坏）走原版逻辑，铁轨正常变形、掉落。
 *
 * disableRailShapeUpdate 是独立规则，本 Mixin 保留其原有逻辑不修改。
 */
@Mixin(BaseRailBlock.class)
public class BaseRailBlockNeighborMixin {

    /**
     * updateDir —— 铁轨形状计算的入口。
     */
    @Inject(
            method = "updateDir",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onUpdateDir(Level level, BlockPos pos, BlockState state, boolean movedByPiston,
                             CallbackInfoReturnable<BlockState> cir) {
        if (ACMAXSettings.disableRailShapeUpdate) {
            if (!level.isClientSide()) {
                cir.setReturnValue(state);
                cir.cancel();
            }
            return;
        }

        if (!ACMAXSettings.railForceStatePlacement) return;

        // 仅处于放置事务中的那一格铁轨，本次更新保持强制形状不被重算
        if (EasyPlaceContext.isPlacing(level, pos)) {
            if (!level.isClientSide()) {
                cir.setReturnValue(state);
                cir.cancel();
            }
        }
    }

    /**
     * shouldBeRemoved —— 支撑方块检查。
     * 仅放置事务期间放行，事务结束即恢复原版掉落逻辑。
     */
    @Inject(
            method = "shouldBeRemoved",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void onShouldBeRemoved(BlockPos pos, Level level, RailShape shape,
                                          CallbackInfoReturnable<Boolean> cir) {
        if (ACMAXSettings.disableRailShapeUpdate) {
            cir.setReturnValue(false);
            cir.cancel();
            return;
        }

        if (ACMAXSettings.railForceStatePlacement && EasyPlaceContext.isPlacing(level, pos)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
