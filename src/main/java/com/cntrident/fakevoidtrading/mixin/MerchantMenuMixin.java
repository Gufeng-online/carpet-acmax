package com.cntrident.fakevoidtrading.mixin;

import com.cntrident.fakevoidtrading.FakeVoidTrading;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MerchantMenu.class)
public abstract class MerchantMenuMixin {
    @Shadow @Final private Merchant trader;
    @Inject(method = "stillValid", at = @At("HEAD"), cancellable = true)
    private void keepAutomationMenu(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (FakeVoidTrading.keepsMenu(player, (MerchantMenu) (Object) this, trader)) cir.setReturnValue(true);
    }
}
