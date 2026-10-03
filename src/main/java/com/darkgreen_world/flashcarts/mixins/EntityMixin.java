package com.darkgreen_world.flashcarts.mixins;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.level.Level;
import com.darkgreen_world.flashcarts.Flashcarts;
import com.darkgreen_world.flashcarts.utils.TitleUtils;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.world.entity.EntitySelector;
//import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
//import net.minecraft.world.phys.Vec3;
//import java.util.List;

@Mixin(Entity.class)
public abstract class EntityMixin {

	@Shadow
	public abstract Level level();

	@Shadow
	public abstract @Nullable Entity getVehicle();

	@Unique
	public Object self = this;

	@Inject(method = "removeVehicle", at = @At("HEAD"))
	public void onDismount(CallbackInfo ci) {
		if (level().isClientSide()) return;
		if (this.getVehicle() instanceof Minecart && self instanceof ServerPlayer player) {
			if (Flashcarts.config.shouldShowSpeedometer() || Flashcarts.config.shouldShowSpeedBar()) {
				player.sendSystemMessage(Component.empty(), true);
			}
			if (Flashcarts.config.shouldShowStationTitle()) {
				TitleUtils.clearTitle(player, true);
			}
		}
	}

/*
	@Inject(method = "setOnGroundWithMovement(ZZLnet/minecraft/world/phys/Vec3;)V", at = @At("HEAD"))
	public void onPush(boolean bl, boolean horizontalCollision, Vec3 vec3, CallbackInfo ci) {
		if (level().isClientSide() || !horizontalCollision) return;
		if (self instanceof AbstractMinecart minecart) {
			if (minecart.getPassengers().isEmpty()) return;
			// calculation math
			var collisionBox = minecart.getBoundingBox().inflate(1.0E-7);
			List<Entity> list = minecart.level().getEntities(minecart, collisionBox, EntitySelector.pushableBy(minecart));
			if (list.isEmpty()) return;
			// handle collisions
			for (Entity entity : list) {
				if (entity == minecart.getFirstPassenger()) continue;
				if (entity instanceof AbstractMinecart colliderMinecart) {
					var colliderSpeed = colliderMinecart.getKnownMovement().length();
					var selfSpeed = minecart.getKnownMovement().length();
					// Ignore same speed and passengers with carts
					if (colliderSpeed == selfSpeed || !colliderMinecart.getPassengers().isEmpty()) return;
					// Destroy empty cart
					if (colliderSpeed < selfSpeed && colliderMinecart instanceof VehicleEntityInvoker collider) {
						collider.flash_cart$destroy((ServerLevel) colliderMinecart.level(), collider.flash_cart$getDropItem());
					} else if (minecart instanceof VehicleEntityInvoker selfMinecart) {
						selfMinecart.flash_cart$destroy((ServerLevel) minecart.level(), selfMinecart.flash_cart$getDropItem());
					}
				}
			}
		}
	}
*/
}
