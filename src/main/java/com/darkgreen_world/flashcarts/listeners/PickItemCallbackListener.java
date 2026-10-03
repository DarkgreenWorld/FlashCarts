package com.darkgreen_world.flashcarts.listeners;

import net.fabricmc.fabric.api.event.player.PlayerPickItemEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import com.darkgreen_world.flashcarts.utils.ExpressUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class PickItemCallbackListener implements PlayerPickItemEvents.PickItemFromEntity {

    @Override
    public @Nullable ItemStack onPickItemFromEntity(@NonNull ServerPlayer player, @NonNull Entity entity, boolean requestIncludeData) {
        if (!(entity instanceof AbstractMinecart minecart) || !ExpressUtils.isExpress(minecart)) return null;
        var item = minecart.getPickResult();
        ExpressUtils.makeExpress(item, minecart.getType());
        return item;
    }

}
