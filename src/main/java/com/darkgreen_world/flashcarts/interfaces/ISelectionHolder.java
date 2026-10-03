package com.darkgreen_world.flashcarts.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public interface ISelectionHolder {

    @Nullable
    BlockPos flashCarts$getStartPointPos();

    @Nullable
    Level flashCarts$getStartPointLevel();

    boolean flashCarts$isStartPointSet();

    void flashCarts$setStartPoint(BlockPos pos, Level level);

    void flashCarts$clearStartPoint();

}
