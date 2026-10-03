package com.darkgreen_world.flashcarts;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerPickItemEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.loader.api.FabricLoader;
import com.darkgreen_world.flashcarts.config.ConfigResourceCondition;
import com.darkgreen_world.flashcarts.config.DefaultConfig;
import com.darkgreen_world.flashcarts.config.FzzyConfig;
import com.darkgreen_world.flashcarts.config.IConfig;
import com.darkgreen_world.flashcarts.listeners.EntityLoadCallbackListener;
import com.darkgreen_world.flashcarts.listeners.PickItemCallbackListener;
import com.darkgreen_world.flashcarts.listeners.ReloadCallbackListener;
import com.darkgreen_world.flashcarts.listeners.UseBlockCallbackListener;
import com.darkgreen_world.flashcarts.listeners.UseItemCallbackListener;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Flashcarts implements ModInitializer {

	public static final String MOD_ID = "flash_carts";

	public static String VERSION = "unknown";

	public static final Logger LOGGER = LogManager.getLogger("flash_carts");

	public static IConfig config;

	@Override
	public void onInitialize() {
		UseBlockCallback.EVENT.register(new UseBlockCallbackListener());
		UseItemCallback.EVENT.register(new UseItemCallbackListener());
		ServerLifecycleEvents.START_DATA_PACK_RELOAD.register(new ReloadCallbackListener());
		ResourceConditions.register(ConfigResourceCondition.TYPE);
		ServerEntityEvents.ENTITY_LOAD.register(new EntityLoadCallbackListener());
		PlayerPickItemEvents.ENTITY.register(new PickItemCallbackListener());

		FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(mod -> {
			VERSION = mod.getMetadata().getVersion().getFriendlyString();
		});

		loadConfig();
	}

	public static void loadConfig() {
		if (FabricLoader.getInstance().isModLoaded("fzzy_config")) {
			config = FzzyConfig.load();
		} else {
			config = DefaultConfig.load();
			LOGGER.log(Level.INFO, "Fzzy Config not found, using default settings.");
		}
	}

}
