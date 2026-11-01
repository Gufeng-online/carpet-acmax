package com.gufeng.mixin;

import com.gufeng.light.VoidSkylightFix;
import com.gufeng.settings.ACMAXSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndLightGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockAndLightGetter.class)
public interface BlockAndLightGetterMixin {
    @Inject(method = "canSeeSky", at = @At("HEAD"), cancellable = true)
    private void carpetAcmax$closeSkyStateAtY64(
            BlockPos pos,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (ACMAXSettings.voidSkylightFix
                && pos.getY() == VoidSkylightFix.TARGET_Y) {
            cir.setReturnValue(false);
        }
    }
}
