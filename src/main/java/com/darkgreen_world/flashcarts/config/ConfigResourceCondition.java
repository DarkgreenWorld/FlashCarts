package com.darkgreen_world.flashcarts.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.StringRepresentable;
import com.darkgreen_world.flashcarts.Flashcarts;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record ConfigResourceCondition(Option option) implements ResourceCondition {

    // Conditions written before the option was introduced omit it and refer to the cheaper recipes.
    public static final MapCodec<ConfigResourceCondition> CODEC = Option.CODEC.optionalFieldOf("option", Option.CHEAPER_RECIPES)
            .xmap(ConfigResourceCondition::new, ConfigResourceCondition::option);
    public static final ResourceConditionType<ConfigResourceCondition> TYPE = ResourceConditionType.create(Identifier.parse("flash_carts:config"), CODEC);

    @Override
    public @NonNull ResourceConditionType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean test(RegistryOps.@Nullable RegistryInfoLookup registryInfoLookup) {
        return switch (option) {
            case CHEAPER_RECIPES -> Flashcarts.config.areCheaperRecipesEnabled();
            case EXPRESS_RECIPES -> Flashcarts.config.areExpressRecipesEnabled();
        };
    }

    public enum Option implements StringRepresentable {
        CHEAPER_RECIPES("cheaper_recipes"),
        EXPRESS_RECIPES("express_recipes");

        public static final Codec<Option> CODEC = StringRepresentable.fromEnum(Option::values);

        private final String name;

        Option(String name) {
            this.name = name;
        }

        @Override
        public @NonNull String getSerializedName() {
            return name;
        }
    }

}
