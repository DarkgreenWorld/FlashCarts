package com.darkgreen_world.flashcarts.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Adds a config button to Mod Menu. The configuration belongs to the server, so the button requests
 * the config dialog from it, which only operators can open.
 */
public class ModMenuIntegration implements ModMenuApi, ConfigScreenFactory<Screen> {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return this;
	}

	// Returns no screen, as the dialog replaces the current screen once the server sends it.
	@Override
	public Screen create(Screen parent) {
		var minecraft = Minecraft.getInstance();
		var connection = minecraft.getConnection();
		if (connection != null && connection.getCommands().getRoot().getChild("flashcarts") != null) {
			connection.sendCommand("flashcarts config");
		} else {
			SystemToast.add(minecraft.gui.toastManager(), SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
					Component.translatable("flash_carts.config.title_client"), Component.translatable("flash_carts.config.unavailable"));
		}
		return null;
	}

}
