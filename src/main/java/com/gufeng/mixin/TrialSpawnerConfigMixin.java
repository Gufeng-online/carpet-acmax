package com.gufeng.mixin;

import com.gufeng.settings.ACMAXSettings;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复试炼刷怪笼生成间隔（trialSpawnerIntervalFix）
 *
 * 试炼密室中 chamber_8 / encounter_4 结构使用的 slow_ranged 试炼刷怪笼配置，
 * 生成间隔为 160gt（其它刷怪笼为 20gt）。开启规则后，把 160gt 修正为 20gt。
 */
@Mixin(TrialSpawnerConfig.class)
public class TrialSpawnerConfigMixin {

    @Inject(method = "ticksBetweenSpawn", at = @At("RETURN"), cancellable = true)
    private void fixSpawnInterval(CallbackInfoReturnable<Integer> cir) {
        if (ACMAXSettings.trialSpawnerIntervalFix && cir.getReturnValueI() == 160) {
            cir.setReturnValue(20);
        }
    }
}