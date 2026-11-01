package com.gufeng.mixin;

import com.gufeng.light.VoidSkylightFix;
import com.gufeng.settings.ACMAXSettings;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.world.level.chunk.DataLayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;
import java.util.List;

@Mixin(ClientboundLightUpdatePacketData.class)
public abstract class ClientboundLightUpdatePacketDataMixin {
    @Shadow @Final private BitSet skyYMask;
    @Shadow @Final private List<byte[]> skyUpdates;

    @Inject(method = "write", at = @At("HEAD"))
    private void carpetAcmax$removeTargetYBeforeWrite(FriendlyByteBuf output, CallbackInfo ci) {
        if (!ACMAXSettings.voidSkylightFix) {
            return;
        }

        int localY = SectionPos.sectionRelative(VoidSkylightFix.TARGET_Y);
        int updateIndex = 0;
        for (int sectionIndex = this.skyYMask.nextSetBit(0);
             sectionIndex >= 0;
             sectionIndex = this.skyYMask.nextSetBit(sectionIndex + 1)) {
            if (updateIndex >= this.skyUpdates.size()) {
                break;
            }
            if (sectionIndex == 1) {
                DataLayer corrected = new DataLayer(this.skyUpdates.get(updateIndex).clone());
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        corrected.set(x, localY, z, 0);
                    }
                }
                this.skyUpdates.set(updateIndex, corrected.getData());
            }
            updateIndex++;
        }
    }
}
