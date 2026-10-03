package com.darkgreen_world.flashcarts.config;

import com.darkgreen_world.flashcarts.Flashcarts;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dialog.ActionButton;
import net.minecraft.server.dialog.CommonButtonData;
import net.minecraft.server.dialog.CommonDialogData;
import net.minecraft.server.dialog.ConfirmationDialog;
import net.minecraft.server.dialog.DialogAction;
import net.minecraft.server.dialog.Input;
import net.minecraft.server.dialog.MultiActionDialog;
import net.minecraft.server.dialog.action.Action;
import net.minecraft.server.dialog.action.CustomAll;
import net.minecraft.server.dialog.action.StaticAction;
import net.minecraft.server.dialog.input.BooleanInput;
import net.minecraft.server.dialog.input.InputControl;
import net.minecraft.server.dialog.input.NumberRangeInput;
import net.minecraft.server.dialog.input.TextInput;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Presents the configuration to operators as vanilla dialogs, so that editing it requires no client mod.
 * The menu holds one page per config section. A page sends its values back as a custom click action
 * with this class's ID, which the server answers by saving the values and showing the menu again.
 */
public class ConfigDialog {

	public static final Identifier ID = Identifier.fromNamespaceAndPath(Flashcarts.MOD_ID, "config");

	public static void open(ServerPlayer player) {
		Map<String, List<Input>> pages = new LinkedHashMap<>();
		for (var entry : FileConfig.ENTRIES.values()) {
			if (entry.key().equals("version")) continue;
			pages.putIfAbsent(entry.section(), new ArrayList<>());
			pages.get(entry.section()).add(input(entry));
		}
		var buttons = new ArrayList<ActionButton>();
		for (var page : pages.entrySet()) {
			var title = Component.translatable("flash_carts.config." + (page.getKey().isEmpty() ? "general" : page.getKey()));
			var dialog = new ConfirmationDialog(
					new CommonDialogData(title, Optional.empty(), true, true, DialogAction.WAIT_FOR_RESPONSE, List.of(), page.getValue()),
					button(CommonComponents.GUI_DONE, new CustomAll(ID, Optional.empty())),
					button(CommonComponents.GUI_CANCEL, new StaticAction(new ClickEvent.Custom(ID, Optional.empty())))
			);
			buttons.add(button(title, new StaticAction(new ClickEvent.ShowDialog(Holder.direct(dialog)))));
		}
		var title = Component.translatable("flash_carts.config.title");
		var menu = new MultiActionDialog(
				new CommonDialogData(title, Optional.empty(), true, true, DialogAction.CLOSE, List.of(), List.of()),
				buttons, Optional.of(button(CommonComponents.GUI_DONE, null)), 2
		);
		player.openDialog(Holder.direct(menu));
	}

	// Applies the values sent by a page, if any, and shows the menu again.
	public static void handle(ServerPlayer player, Optional<Tag> payload) {
		if (!Commands.LEVEL_GAMEMASTERS.check(player.permissions())) return;
		if (payload.orElse(null) instanceof CompoundTag values && !values.isEmpty()) save(player, values);
		open(player);
	}

	private static void save(ServerPlayer player, CompoundTag values) {
		var config = Flashcarts.config;
		Map<String, Object> changes = new HashMap<>();
		for (var entry : FileConfig.ENTRIES.values()) {
			String text = switch (values.get(key(entry))) {
				case ByteTag tag -> String.valueOf(tag.value() != 0);
				case FloatTag tag -> String.valueOf(Math.round(tag.value()));
				case StringTag tag -> tag.value().strip();
				case null, default -> null;
			};
			if (text == null) continue;
			Object value = FileConfig.parse(entry, text);
			if (value == null) {
				player.sendSystemMessage(Component.translatable("flash_carts.config.invalid", label(entry)).withStyle(ChatFormatting.RED));
			} else if (!value.equals(config.get(entry.id()))) {
				changes.put(entry.id(), value);
			}
		}
		if (changes.isEmpty()) return;
		if (!config.save(changes)) {
			player.sendSystemMessage(Component.translatable("flash_carts.config.failed").withStyle(ChatFormatting.RED));
			return;
		}
		player.sendSystemMessage(Component.translatable("flash_carts.config.saved").withStyle(ChatFormatting.GREEN));
		// Recipes depend on these options and only change on a data pack reload.
		if (config.areCheaperRecipesEnabled() != Flashcarts.config.areCheaperRecipesEnabled()
				|| config.areExpressRecipesEnabled() != Flashcarts.config.areExpressRecipesEnabled()) {
			MinecraftServer server = player.level().getServer();
			player.sendSystemMessage(Component.translatable("commands.reload.success"));
			server.reloadResources(server.getPackRepository().getSelectedIds());
		}
	}

	private static Input input(FileConfig.Entry entry) {
		Object value = Flashcarts.config.get(entry.id());
		InputControl control = switch (value) {
			case Boolean bool -> new BooleanInput(label(entry), bool, "true", "false");
			case Integer integer -> new NumberRangeInput(200, label(entry), "options.generic_value",
					new NumberRangeInput.RangeInfo((float) entry.min(), (float) entry.max(), Optional.of((float) integer), Optional.of(1f)));
			// Decimal values use text fields, as sliders would show rounding errors such as 0.049999997.
			default -> new TextInput(200, label(entry), true, value.toString(), 10, Optional.empty());
		};
		return new Input(key(entry), control);
	}

	// Input keys may not contain dots.
	private static String key(FileConfig.Entry entry) {
		return entry.id().replace('.', '_');
	}

	private static Component label(FileConfig.Entry entry) {
		return Component.translatable("flash_carts.config." + entry.key());
	}

	private static ActionButton button(Component label, Action action) {
		return new ActionButton(new CommonButtonData(label, CommonButtonData.DEFAULT_WIDTH), Optional.ofNullable(action));
	}

}
