package com.gufeng.mixin;

import com.gufeng.light.VoidSkylightFix;
import com.gufeng.settings.ACMAXSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.lighting.SkyLightSectionStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkyLightSectionStorage.class)
public abstract class SkyLightSectionStorageMixin {
    @Inject(method = "getLightValue(JZ)I", at = @At("HEAD"), cancellable = true)
    private void carpetAcmax$removeTargetSkylight(
            long blockNode,
            boolean updating,
            CallbackInfoReturnable<Integer> cir
    ) {
        if (ACMAXSettings.voidSkylightFix
                && BlockPos.getY(blockNode) == VoidSkylightFix.TARGET_Y) {
            cir.setReturnValue(0);
        }
    }
}
