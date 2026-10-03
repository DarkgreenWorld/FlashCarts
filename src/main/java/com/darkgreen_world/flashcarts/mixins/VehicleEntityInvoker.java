package com.darkgreen_world.flashcarts.mixins;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(VehicleEntity.class)
public interface VehicleEntityInvoker {

	@Invoker("destroy")
	void flash_cart$destroy(ServerLevel serverLevel, Item item);

	@Invoker("getDropItem")
	Item flash_cart$getDropItem();

}
