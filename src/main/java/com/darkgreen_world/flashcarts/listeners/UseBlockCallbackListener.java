package com.darkgreen_world.flashcarts.listeners;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.phys.BlockHitResult;
import com.darkgreen_world.flashcarts.Flashcarts;
import com.darkgreen_world.flashcarts.interfaces.ISelectionHolder;
import com.darkgreen_world.flashcarts.utils.ItemUtils;
import com.darkgreen_world.flashcarts.utils.PlayerUtils;
import com.darkgreen_world.flashcarts.utils.RailUtils;
import org.jspecify.annotations.NonNull;
import net.minecraft.world.item.component.SwingAnimation;

public class UseBlockCallbackListener implements UseBlockCallback {

    @Override
    public @NonNull InteractionResult interact(@NonNull Player player, @NonNull Level level, @NonNull InteractionHand interactionHand, @NonNull BlockHitResult blockHit) {
        if (level.isClientSide() || !ItemUtils.isRails(player.getItemInHand(interactionHand)) ) return InteractionResult.PASS;
        if (level.getBlockState(blockHit.getBlockPos()).getBlock() instanceof BaseRailBlock) {
            // rail selection
            if (Flashcarts.config.getBuildConfig().isRailSelectionBuildingEnabled() && !player.isCrouching()) {
                if (player instanceof ISelectionHolder selectionHolder) {
                    var cachedPos = selectionHolder.flashCarts$getStartPointPos();
                    if (cachedPos == null) {
                        select(player, blockHit.getBlockPos(), level);
                    } else if (cachedPos.equals(blockHit.getBlockPos())) {
                        clear(player, selectionHolder);
                    } else {
                        return buildSelection(player, selectionHolder, level, interactionHand, blockHit);
                    }
                }
                // rail extension
            } else if (Flashcarts.config.getBuildConfig().isRailExtendBuildingEnabled()) {
                var itemStack = player.getItemInHand(interactionHand);
                var blockPos = blockHit.getBlockPos();
                var blockState = level.getBlockState(blockPos);
                if (player instanceof ServerPlayer serverPlayer && RailUtils.place(serverPlayer, itemStack, blockState, blockPos)) {
                    player.swing(interactionHand, SwingAnimation.DEFAULT, true);
                    return InteractionResult.SUCCESS;
                }
            }
        } else {
            // rail selection
            if (!Flashcarts.config.getBuildConfig().isRailSelectionBuildingEnabled()) return InteractionResult.PASS;
            if (player instanceof ISelectionHolder selectionHolder && selectionHolder.flashCarts$isStartPointSet()) {
                if (player.isCrouching()) {
                    clear(player, selectionHolder);
                    player.swing(interactionHand, SwingAnimation.DEFAULT, true);
                    return InteractionResult.SUCCESS;
                }
                return buildSelection(player, selectionHolder, level, interactionHand, blockHit);
            }
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult buildSelection(@NonNull Player player, ISelectionHolder selectionHolder, @NonNull Level level, @NonNull InteractionHand interactionHand, @NonNull BlockHitResult blockHit) {
        if (selectionHolder.flashCarts$getStartPointLevel() != level) {
            player.sendOverlayMessage(Component.translatable("flash_carts.message.no_rail_path").withStyle(ChatFormatting.RED));
            player.swing(interactionHand, SwingAnimation.DEFAULT, true);
            return InteractionResult.FAIL;
        }
        var startPos = selectionHolder.flashCarts$getStartPointPos();
        assert startPos != null;
        var targetBlockState = level.getBlockState(blockHit.getBlockPos());
        var endPos = targetBlockState.canBeReplaced() || RailUtils.isRail(targetBlockState)
                ? blockHit.getBlockPos()
                : blockHit.getBlockPos().relative(blockHit.getDirection());
        var path = RailUtils.getRailPath(level, startPos, endPos);

        if (!path.isValid()) {
            player.sendOverlayMessage(Component.translatable("flash_carts.message.no_rail_path").withStyle(ChatFormatting.RED));
            player.swing(interactionHand, SwingAnimation.DEFAULT, true);
            return InteractionResult.FAIL;
        } else {
            int i = 0;
            int prf = Flashcarts.config.getBuildConfig().getPoweredRailFrequency();
            var pos = startPos;
            for (var vec : path.path()) {
                pos = pos.offset(vec);
                i += vec.getX() + vec.getZ();
                var rail = RailUtils.getBlockItem(
                        prf, i,
                        player.getItemInHand(interactionHand).getItem()
                );
                boolean placed = ItemUtils.place(rail, player, pos, SoundEvents.METAL_PLACE);
                if (!placed) break;
            }
        }

        if (player instanceof ServerPlayer splayer) {
            PlayerUtils.playDirectSound(splayer, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS);
            splayer.swing(interactionHand, SwingAnimation.DEFAULT, true);
        }
        selectionHolder.flashCarts$clearStartPoint();
        return InteractionResult.SUCCESS;
    }

    private static void select(Player player, BlockPos blockPos, Level level) {
        if (player instanceof ISelectionHolder selectionHolder) {
            selectionHolder.flashCarts$setStartPoint(blockPos, level);
            if (player instanceof ServerPlayer splayer) {
                splayer.sendOverlayMessage(Component.translatable("flash_carts.message.start_point_set").withStyle(ChatFormatting.GREEN));
                PlayerUtils.playDirectSound(splayer, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS);
                splayer.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
            }
        }
    }

    private static void clear(Player player, ISelectionHolder selectionHolder) {
        selectionHolder.flashCarts$clearStartPoint();
        if (player instanceof ServerPlayer splayer) {
            splayer.sendOverlayMessage(Component.translatable("flash_carts.message.selection_cleared").withStyle(ChatFormatting.GOLD));
            PlayerUtils.playDirectSound(splayer, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS);
            splayer.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
        }
    }

}
