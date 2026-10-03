package com.darkgreen_world.flashcarts.objects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

import java.util.List;

public record RailPath(BlockPos start, BlockPos end, List<Vec3i> path, boolean isValid) {
}
