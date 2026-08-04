package com.gufeng.mixin;

import com.gufeng.settings.ACMAXSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TheEndGatewayBlockEntity.class)
public class TheEndGatewayBlockEntityMixin {

    @Shadow
    private int teleportCooldown;

    /**
     * If endGatewayCooldown is enabled, always return false (no cooldown).
     */
    @Inject(method = "isCoolingDown", at = @At("HEAD"), cancellable = true)
    private void onIsCoolingDown(CallbackInfoReturnable<Boolean> cir) {
        if (ACMAXSettings.endGatewayCooldown) {
            cir.setReturnValue(false);
        }
    }

    /**
     * If endGatewayCooldown is enabled, prevent triggerCooldown from setting the cooldown.
     */
    @Inject(method = "triggerCooldown", at = @At("HEAD"), cancellable = true)
    private static void onTriggerCooldown(Level level, BlockPos pos, BlockState state,
                                           TheEndGatewayBlockEntity blockEntity, CallbackInfo ci) {
        if (ACMAXSettings.endGatewayCooldown) {
            ci.cancel();
        }
    }

    /**
     * In portalTick, if cooldown is enabled, reset teleportCooldown to 0
     * so that even if it was previously set, it's immediately cleared.
     */
    @Inject(method = "portalTick", at = @At("HEAD"))
    private static void onPortalTick(Level level, BlockPos pos, BlockState state,
                                     TheEndGatewayBlockEntity blockEntity, CallbackInfo ci) {
        if (ACMAXSettings.endGatewayCooldown) {
            ((TheEndGatewayBlockEntityMixin) (Object) blockEntity).teleportCooldown = 0;
        }
    }

    /**
     * In beamAnimationTick, if cooldown is enabled, reset teleportCooldown to 0.
     */
    @Inject(method = "beamAnimationTick", at = @At("HEAD"))
    private static void onBeamAnimationTick(Level level, BlockPos pos, BlockState state,
                                             TheEndGatewayBlockEntity blockEntity, CallbackInfo ci) {
        if (ACMAXSettings.endGatewayCooldown) {
            ((TheEndGatewayBlockEntityMixin) (Object) blockEntity).teleportCooldown = 0;
        }
    }
}
