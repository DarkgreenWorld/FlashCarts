package com.darkgreen_world.flashcarts.utils;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;

/**
 * An express minecart is an ordinary minecart that carries the entity tag {@link #TAG}. Its item form stores the tag in
 * the {@code entity_data} component, which vanilla applies to the entity when the item is placed.
 * <p>
 * The item components set by {@link #makeExpress} are repeated in the following data files and must be kept identical:
 * <ul>
 *   <li>{@code data/flash_carts/recipe/express_*.json}, which craft express minecarts from a redstone torch or from another express minecart;</li>
 *   <li>{@code data/minecraft/recipe/{chest,furnace,hopper,tnt}_minecart.json}, which override the vanilla recipes so that they
 *   reject express minecarts and only the express variants above match.</li>
 * </ul>
 */
public abstract class ExpressUtils {

    public static final String TAG = "flash_carts.express";

    public static boolean isExpress(Entity entity) {
        return entity.entityTags().contains(TAG);
    }

    public static void makeExpress(ItemStack item, EntityType<?> type) {
        var tags = new ListTag();
        tags.add(StringTag.valueOf(TAG));
        var entityData = new CompoundTag();
        entityData.put("Tags", tags);

        item.set(DataComponents.ENTITY_DATA, TypedEntityData.of(type, entityData));
        item.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        item.set(DataComponents.ITEM_NAME, Component.translatable(
                "item.flash_carts.express",
                Component.translatable(item.getItem().getDescriptionId())
        ));
    }

}
