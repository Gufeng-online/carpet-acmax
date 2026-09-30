package com.gufeng.mixin.protocol;

import com.gufeng.protocol.EasyPlaceContext;
import com.gufeng.protocol.ProtocolHelper;
import com.gufeng.settings.ACMAXSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 事务级保护：仅在 BlockItem.place 执行期间，把【正在放置的那一格铁轨】标记为
 * 正在被强制放置。railForceStatePlacement 开启且本次放置携带协议值时生效。
 *
 * - 放置开始（HEAD）→ EasyPlaceContext.beginPlacement(该格)
 * - 放置结束（RETURN）→ EasyPlaceContext.endPlacement(该格)
 *
 * 保护严格限定在本次放置事务内，事务结束立即失效（周围一更新即恢复）。
 * 标记带超时兜底，异常路径也不会残留。
 */
@Mixin(BlockItem.class)
public class BlockItemPlaceMixin {

    @Inject(method = "place", at = @At("HEAD"))
    private void beforePlace(BlockPlaceContext context, CallbackInfoReturnable<?> cir) {
        if (!ACMAXSettings.railForceStatePlacement) return;

        Vec3 hitPos = context.getClickLocation();
        BlockPos blockPos = context.getClickedPos();
        if (ProtocolHelper.hasProtocolValue(hitPos, blockPos)) {
            EasyPlaceContext.beginPlacement(context.getLevel(), blockPos);
        }
    }

    @Inject(method = "place", at = @At("RETURN"))
    private void afterPlace(BlockPlaceContext context, CallbackInfoReturnable<?> cir) {
        if (!ACMAXSettings.railForceStatePlacement) return;
        BlockPos blockPos = context.getClickedPos();
        EasyPlaceContext.endPlacement(context.getLevel(), blockPos);
    }
}
