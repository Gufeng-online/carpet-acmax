package com.gufeng.mixin.protocol;

import com.gufeng.protocol.EasyPlaceContext;
import com.gufeng.settings.ACMAXSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 拦截 BaseRailBlock.canSurvive
 *
 * railForceStatePlacement 开启时，仅在【该位置正在被轻松放置】（EasyPlaceContext 事务内）
 * 放行 canSurvive，让投影中的悬浮靠放铁轨能放得下去。
 * 放置事务一旦结束，保护即失效 —— 之后支撑被移除等更新会让铁轨正常掉落。
 */
@Mixin(BaseRailBlock.class)
public class BaseRailBlockCanSurviveMixin {

    @Inject(
            method = "canSurvive",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onCanSurvive(BlockState state, LevelReader level, BlockPos pos,
                              CallbackInfoReturnable<Boolean> cir) {
        if (!ACMAXSettings.railForceStatePlacement) return;

        // 仅当前正在被强制放置的那一格放行，事务结束即失效
        if (EasyPlaceContext.isPlacing(level, pos)) {
            cir.setReturnValue(true);
        }
    }
}
