package com.darkgreen_world.flashcarts.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import com.darkgreen_world.flashcarts.Flashcarts;
import com.darkgreen_world.flashcarts.mixins.BaseRailBlockInvoker;
import com.darkgreen_world.flashcarts.objects.RailPath;
import org.jspecify.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public abstract class RailUtils {

    public static boolean place(ServerPlayer player, ItemStack railItem, BlockState blockState, BlockPos pos) {
        Level world = player.level();
        var shape = getRailShape(blockState);
        if (shape.isEmpty()) return false;
        var playerDirection = player.getDirection();
        boolean xAxisRails = isXAxis(shape.get());
        boolean xAxisPlayer = (playerDirection == Direction.EAST) || (playerDirection == Direction.WEST);
        return xAxisRails == xAxisPlayer &&
                forward(world, player, playerDirection.getUnitVec3i(), railItem, pos, xAxisRails, 8, false);
    }

    private static boolean forward(Level world, Player player, Vec3i vec, ItemStack railItem, BlockPos pos, boolean xAxisRails, int depth, boolean movedVertically) {
        if (depth == 0) return false;
        var _blockState = world.getBlockState(pos);
        // if there is place
        if (_blockState.isAir()) {
            var _blockStateDown = world.getBlockState(pos.below());
            if ((_blockStateDown.isAir() || _blockStateDown.getBlock() instanceof BaseRailBlock) && !movedVertically) {
                pos = pos.below();
                movedVertically = true;
                depth++;
            } else {
                return ItemUtils.place(railItem, player, pos, SoundEvents.METAL_PLACE);
            }
        }
        // if block in way
        if (!_blockState.isAir() && !(_blockState.getBlock() instanceof BaseRailBlock) && !movedVertically) {
            pos = pos.above();
            movedVertically = true;
            depth++;
        }
        // if rails and on same axis
        var shape = getRailShape(_blockState);
        if (_blockState.getBlock() instanceof BaseRailBlock && shape.isPresent() && isXAxis(shape.get()) == xAxisRails) {
            pos = pos.offset(vec);
            movedVertically = false;
        }
        return forward(world, player, vec, railItem, pos, xAxisRails, depth - 1, movedVertically);
    }

    public static RailPath getRailPath(Level level, BlockPos startPos, BlockPos endPos) {
        var dist = startPos.distChessboard(endPos);
        int maxDist = Flashcarts.config.getBuildConfig().getRailSelectionBuildingMaxDistance();
        if (startPos.equals(endPos)) return new RailPath(startPos, endPos, List.of(), false);

        Vec3i step;
        var delta = endPos.subtract(startPos);
        if (Math.abs(delta.getX()) >= Math.abs(delta.getZ())) {
            step = new Vec3i(Integer.signum(endPos.getX() - startPos.getX()), 0, 0);
        } else {
            step = new Vec3i(0, 0, Integer.signum(endPos.getZ() - startPos.getZ()));
        }
        var path = new LinkedList<Vec3i>();
        var currentPos = new BlockPos(startPos.getX(), startPos.getY(), startPos.getZ());
        var _step = new Vec3i(step.getX(), step.getY(), step.getZ());
        while (!currentPos.equals(endPos)) {
            currentPos = currentPos.offset(step);
            maxDist--;
            var currentDist = startPos.distChessboard(currentPos);
            if (currentDist > dist || currentDist > maxDist) return new RailPath(startPos, endPos, path, false);

            if (isRail(level, currentPos)) {
                _step = _step.offset(step);
                continue;
            }
            if (!canBePlaced(level, currentPos)) {
                if (canBePlaced(level, currentPos.above())) {
                    currentPos = currentPos.above();
                    path.add(_step.above());
                } else if (isRail(level, currentPos.above())) {
                    currentPos = currentPos.above();
                    _step = _step.offset(step).above();
                    continue;
                } else if (canBePlaced(level, currentPos.below())) {
                    currentPos = currentPos.below();
                    path.add(_step.below());
                } else if (isRail(level, currentPos.below())) {
                    currentPos = currentPos.below();
                    _step = _step.offset(step).below();
                    continue;
                } else return new RailPath(startPos, endPos, path, false);
            } else {
                path.add(_step);
            }
            if (maxDist < 0) return new RailPath(startPos, endPos, path, false);
            _step = new Vec3i(step.getX(), step.getY(), step.getZ());
        }

        return new RailPath(startPos, endPos, path, true);
    }

    public static RailShape getRailShape(Vec3i vec, @Nullable Vec3i nextVec) {
        // check nextVec for ascending rails,
        // this is needed because of optimizations in the pathfinding
        if (nextVec != null && nextVec.getY() > 0) {
            if (nextVec.getX() > 0) return RailShape.ASCENDING_EAST;
            if (nextVec.getX() < 0) return RailShape.ASCENDING_WEST;
            if (nextVec.getZ() > 0) return RailShape.ASCENDING_SOUTH;
            if (nextVec.getZ() < 0) return RailShape.ASCENDING_NORTH;
        }
        // descending rails can be checked on the current vector
        if (vec.getY() < 0) {
            if (vec.getX() < 0 && vec.getX() == vec.getY()) return RailShape.ASCENDING_EAST;
            if (vec.getX() > 0 && -vec.getX() == vec.getY()) return RailShape.ASCENDING_WEST;
            if (vec.getZ() < 0 && vec.getZ() == vec.getY()) return RailShape.ASCENDING_SOUTH;
            if (vec.getZ() > 0 && -vec.getZ() == vec.getY()) return RailShape.ASCENDING_NORTH;
        }
        if (vec.getX() != 0) {
            return RailShape.EAST_WEST;
        } else {
            return RailShape.NORTH_SOUTH;
        }
    }

    private static boolean isRail(Level level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof BaseRailBlock;
    }

    public static boolean isRail(BlockState blockState) {
        return blockState.getBlock() instanceof BaseRailBlock;
    }

    private static boolean canBePlaced(Level level, BlockPos pos) {
        if (Blocks.RAIL instanceof BaseRailBlockInvoker invoker) {
            return level.getBlockState(pos).canBeReplaced()
                    && invoker.flashCarts$canSurvive(Blocks.RAIL.defaultBlockState(), level, pos);
        }
        return false;
    }

    private static boolean isXAxis(RailShape shape) {
        return (shape == RailShape.EAST_WEST) || (shape == RailShape.ASCENDING_EAST) || (shape == RailShape.ASCENDING_WEST);
    }

    public static Optional<RailShape> getRailShape(BlockState state) {
        try {
            var poweredShape = state.getValue(PoweredRailBlock.SHAPE);
            return Optional.of(poweredShape);
        } catch(IllegalArgumentException _) {
            //ignored
        }
        try {
            var shape = state.getValue(RailBlock.SHAPE);
            return Optional.of(shape);
        } catch(IllegalArgumentException _) {
            //ignored
        }
        return Optional.empty();
    }

    public static BlockState getBlockState(int poweredRailFrequency, int i, Item heldItem, RailShape shape) {
        if (poweredRailFrequency != 0) {
            return  (i % poweredRailFrequency == 0)
                    ? Blocks.POWERED_RAIL.defaultBlockState().setValue(PoweredRailBlock.SHAPE, shape)
                    : Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, shape);
        }
        if (heldItem instanceof BlockItem blockItem) {
            var block = blockItem.getBlock();
            switch (block) {
                case RailBlock ignored -> {
                    return block.defaultBlockState().setValue(RailBlock.SHAPE, shape);
                }
                case PoweredRailBlock ignored -> {
                    return block.defaultBlockState().setValue(PoweredRailBlock.SHAPE, shape);
                }
                case DetectorRailBlock ignored -> {
                    return block.defaultBlockState().setValue(DetectorRailBlock.SHAPE, shape);
                }
                default -> {}
            }
        }
        return Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, shape);
    }

    public static Item getBlockItem(int poweredRailFrequency, int i, Item heldItem) {
        if (poweredRailFrequency != 0) {
            return  (i % poweredRailFrequency == 0)
                ? Items.POWERED_RAIL
                : Items.RAIL;
        }
        return heldItem;
    }

}
