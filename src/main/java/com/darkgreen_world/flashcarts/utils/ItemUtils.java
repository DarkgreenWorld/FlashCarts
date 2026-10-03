package com.darkgreen_world.flashcarts.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public abstract class ItemUtils {

    public static boolean isRails(ItemStack item) {
        return item.is(ItemTags.RAILS);
    }

    public static boolean place(ItemStack item, Player player, BlockPos pos, SoundEvent soundEvent) {
        if (item.getItem() instanceof BlockItem blockItem) {
            var result = blockItem.place(new BlockPlaceContext(
                    player,
                    InteractionHand.MAIN_HAND,
                    item,
                    BlockHitResult.miss(Vec3.ZERO, Direction.DOWN, pos)
            ));
            boolean placed = result == InteractionResult.SUCCESS;
            if (placed && player instanceof ServerPlayer splayer && soundEvent != null) {
                PlayerUtils.playDirectSound(splayer, soundEvent, SoundSource.BLOCKS);
            }
            return placed;
        } else return false;
    }

    public static boolean place(Item item, Player player, BlockPos pos, SoundEvent soundEvent) {
        if (player.isCreative()) return place(new ItemStack(item), player, pos, soundEvent);
        var index = player.getInventory().findSlotMatchingItem(item.getDefaultInstance());
        if (index == -1) return false;
        var stack = player.getInventory().getItem(index);
        if (stack.isEmpty()) return false;
        return place(stack, player, pos, soundEvent);
    }

}
