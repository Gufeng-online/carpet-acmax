package com.gufeng.mixin.cactus;

import carpet.CarpetSettings;
import com.gufeng.settings.ACMAXSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 仙人掌扳手铁轨增强 —— 配合 carpet flippinCactus 使用。
 * 开启后，手持仙人掌右键铁轨依次切换所有形态，不触发任何方块/邻居更新。
 */
@Mixin(ServerPlayerGameMode.class)
public class CactusWrenchRailMixin {

    @Inject(
            method = "useItemOn",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onUseItemOn(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand,
                             BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (!ACMAXSettings.cactusWrenchRailEnhancement) return;
        if (!CarpetSettings.flippinCactus) return;
        if (!stack.is(Items.CACTUS)) return;
        if (!player.mayBuild()) return;

        BlockPos pos = hitResult.getBlockPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (!(block instanceof BaseRailBlock railBlock)) return;

        // 遍历铁轨所有合法形状
        RailShape current = state.getValue(railBlock.getShapeProperty());
        int[] validShapes = (block instanceof RailBlock)
                ? RAIL_SHAPES : POWERED_RAIL_SHAPES;
        int nextIdx = 0;
        for (int i = 0; i < validShapes.length; i++) {
            if (validShapes[i] == current.ordinal()) {
                nextIdx = (i + 1) % validShapes.length;
                break;
            }
        }
        RailShape next = RailShape.values()[validShapes[nextIdx]];
        BlockState newState = state.setValue(railBlock.getShapeProperty(), next);

        // UPDATE_CLIENTS: 仅同步渲染，不触发邻居/方块更新
        level.setBlock(pos, newState, Block.UPDATE_CLIENTS);
        player.swing(hand);

        cir.setReturnValue(InteractionResult.SUCCESS);
    }

    /** 普通铁轨：10 种形状 */
    private static final int[] RAIL_SHAPES  = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

    /** 动力/探测铁轨：6 种形状（不含弯道） */
    private static final int[] POWERED_RAIL_SHAPES = {0, 1, 2, 3, 4, 5};
}
