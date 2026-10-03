package com.darkgreen_world.flashcarts.listeners;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import com.darkgreen_world.flashcarts.handlers.ExpressTorchHandler;
import com.darkgreen_world.flashcarts.utils.ExpressUtils;
import org.jspecify.annotations.NonNull;

public class EntityLoadCallbackListener implements ServerEntityEvents.Load {

    @Override
    public void onLoad(@NonNull Entity entity, @NonNull ServerLevel level) {
        if (entity instanceof AbstractMinecart minecart && ExpressUtils.isExpress(minecart)) {
            ExpressTorchHandler.attach(minecart);
        }
    }

}
