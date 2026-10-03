package com.darkgreen_world.flashcarts.listeners;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import com.darkgreen_world.flashcarts.interfaces.ISelectionHolder;
import com.darkgreen_world.flashcarts.utils.PlayerUtils;
import org.jspecify.annotations.NonNull;
import net.minecraft.world.item.component.SwingAnimation;

public class UseItemCallbackListener implements UseItemCallback {

    @Override
    public @NonNull InteractionResult interact(@NonNull Player player, @NonNull Level world, @NonNull InteractionHand hand) {
        if (player instanceof ISelectionHolder selectionHolder) {
            if (selectionHolder.flashCarts$getStartPointPos() != null) {
                clear(player, selectionHolder);
                player.swing(hand, SwingAnimation.DEFAULT, true);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    private static void clear(Player player, ISelectionHolder selectionHolder) {
        selectionHolder.flashCarts$clearStartPoint();
        if (player instanceof ServerPlayer splayer) {
            splayer.sendOverlayMessage(Component.literal("§6Cleared selection"));
            PlayerUtils.playDirectSound(splayer, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS);
            splayer.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
        }
    }

}
