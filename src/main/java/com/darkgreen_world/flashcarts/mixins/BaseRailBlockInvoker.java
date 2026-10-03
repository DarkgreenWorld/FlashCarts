package com.darkgreen_world.flashcarts.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BaseRailBlock.class)
public interface BaseRailBlockInvoker {

    @Invoker("canSurvive")
    boolean flashCarts$canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos);

}
