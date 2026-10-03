package com.darkgreen_world.flashcarts.config;

import com.darkgreen_world.flashcarts.Flashcarts;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FileConfig implements IConfig, IBuildConfig {

	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("flash_carts/config.toml");

	private static final Map<String, String> SECTIONS = new LinkedHashMap<>();
	static final Map<String, Entry> ENTRIES = new LinkedHashMap<>();

	static {
		add("", "version", "Don't change this! Version used to track needed updates.", 0, 0, Integer.MAX_VALUE);
		add("", "cheaperRecipes", "Whether to enable cheaper and alternative recipes for rails, vanilla: false", default_cheaperRecipes);
		add("", "showSpeedometer", "Whether to show the speedometer when in minecart (current speed in blocks per second), vanilla: false", default_showSpeedometer);
		add("", "showSpeedBar", "Whether to show the speed bar when in minecart (bars up to max speed), vanilla: false", default_showSpeedBar);
		add("", "showStationTitle", "Whether to show the station title (title when standing still at a block with a sign under it), vanilla: false", default_showStationTitle);
		add("", "poweredRailBoostPercentage", "Percentage of boost a powered rail should give, vanilla: 0.06 (6%)", default_poweredRailBoostPercentage, 0.01, 0.99);
		add("", "haltSpeedThreshold", "How slow a minecart has to be, to be considered halted, vanilla: 0.03", default_haltSpeedThreshold, 0.01, 0.99);
		add("", "haltSpeedMultiplier", "Multiplier applied to speed when minecart is considered halted, vanilla: 0.5", default_haltSpeedMultiplier, 0.1, 0.9);
		add("", "enableSmartHalt", "Whether to enable smart halting, which will make minecarts brake depending on the length of unpowered rails, vanilla: false", default_smartHaltEnabled);
		add("", "expressMinecartRecipes", "Whether express minecarts can be crafted. Express minecarts always use experimental physics, vanilla: false", default_expressRecipes);

		SECTIONS.put("buildTools", "Configuration for building tools");
		add("buildTools", "railSelectionBuilding", "Whether to enable rail selection building, default: " + default_railSelectionBuildingEnabled, default_railSelectionBuildingEnabled);
		add("buildTools", "railSelectionBuildingMaxDistance", "How far from the starting point selections should work, default: " + default_railSelectionBuildingMaxDistance, default_railSelectionBuildingMaxDistance, 8, 96);
		add("buildTools", "poweredRailFrequency", "How often a powered rail should be placed, set to 0 to disable, default: " + default_poweredRailFrequency, default_poweredRailFrequency, 0, 32);
		add("buildTools", "showSelection", "Whether to show the selection when using the rail selection building feature, default: " + default_showSelection, default_showSelection);
		add("buildTools", "railExtendBuilding", "Whether to enable rail extending building, default: " + default_railExtendBuildingEnabled, default_railExtendBuildingEnabled);
		add("buildTools", "railExtendBuildingMaxDistance", "How far from the player rail is able to be extended, default: " + default_railExtendBuildingMaxDistance, default_railExtendBuildingMaxDistance, 8, 96);

		cart("emptyMinecart", "Configuration for empty minecarts", default_emptyUseExperimentalPhysics, default_emptyMaxSpeed);
		cart("mobMinecart", "Configuration for minecarts with a mob in them", default_mobUseExperimentalPhysics, default_mobMaxSpeed);
		cart("playerMinecart", "Configuration for minecarts with a player in them", default_playerUseExperimentalPhysics, default_playerMaxSpeed);
		cart("tntMinecart", "Configuration for TNT minecarts", default_tntUseExperimentalPhysics, default_tntMaxSpeed);
		cart("chestMinecart", "Configuration for chest minecarts", default_redstoneCartsUseExperimentalPhysics, default_redstoneCartsMaxSpeed);
		cart("hopperMinecart", "Configuration for hopper minecarts", default_redstoneCartsUseExperimentalPhysics, default_redstoneCartsMaxSpeed);
		cart("furnaceMinecart", "Configuration for furnace minecarts", default_redstoneCartsUseExperimentalPhysics, default_redstoneCartsMaxSpeed);
		cart("commandBlockMinecart", "Configuration for command block minecarts", default_redstoneCartsUseExperimentalPhysics, default_redstoneCartsMaxSpeed);
	}

	private final Map<String, Object> values = new HashMap<>();
	private final Map<String, ICartConfig> carts = new HashMap<>();

	private FileConfig() {}

	public static FileConfig load() {
		var config = new FileConfig();
		boolean valid;
		boolean readable = true;
		try {
			valid = Files.exists(PATH) && config.read(Files.readAllLines(PATH), Map.of());
		} catch (IOException e) {
			Flashcarts.LOGGER.error("Failed to read the config file, using default values", e);
			valid = false;
			readable = false;
		}
		for (var entry : ENTRIES.values()) {
			if (config.values.putIfAbsent(entry.id(), entry.fallback()) == null) valid = false;
		}
		if (!valid && readable) write(config.values);
		for (var section : SECTIONS.keySet()) {
			if (ENTRIES.containsKey(section + ".useExperimentalPhysics")) {
				config.carts.put(section, new Cart(config.bool(section + ".useExperimentalPhysics"), config.integer(section + ".maxSpeedBlocksPerSecond")));
			}
		}
		return config;
	}

	// Returns false for syntax errors, invalid or duplicate values and unknown sections or keys.
	// The values of the given changes replace the old ones in the lines, leaving the rest of each line as it is.
	private boolean read(List<String> lines, Map<String, Object> changes) {
		boolean valid = true;
		String section = "";
		for (int i = 0; i < lines.size(); i++) {
			int comment = lines.get(i).indexOf('#');
			String content = comment < 0 ? lines.get(i) : lines.get(i).substring(0, comment);
			String line = content.strip();
			if (line.isEmpty()) continue;
			if (line.startsWith("[") && line.endsWith("]")) {
				section = line.substring(1, line.length() - 1).strip();
				if (!SECTIONS.containsKey(section)) valid = false;
				continue;
			}
			int separator = line.indexOf('=');
			Entry entry = separator < 0 ? null : ENTRIES.get((section.isEmpty() ? "" : section + ".") + line.substring(0, separator).strip());
			String text = line.substring(separator + 1).strip();
			Object value = entry == null ? null : parse(entry, text);
			if (value == null || values.putIfAbsent(entry.id(), value) != null) {
				valid = false;
			} else if (changes.containsKey(entry.id())) {
				int end = content.stripTrailing().length();
				lines.set(i, lines.get(i).substring(0, end - text.length()) + changes.get(entry.id()) + lines.get(i).substring(end));
			}
		}
		return valid;
	}

	static Object parse(Entry entry, String text) {
		try {
			Object value = switch (entry.fallback()) {
				case Boolean _ -> text.equals("true") || text.equals("false") ? Boolean.valueOf(text) : null;
				case Integer _ -> Integer.valueOf(text);
				case Float _ -> Float.valueOf(text);
				default -> Double.valueOf(text);
			};
			if (value instanceof Number number && !(number.doubleValue() >= entry.min() && number.doubleValue() <= entry.max())) return null;
			return value;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	// Changes only the lines of the given values, unless the file is invalid and has to be rewritten,
	// then loads the file again. Returns false if writing failed.
	boolean save(Map<String, Object> changes) {
		var updated = new HashMap<>(values);
		updated.putAll(changes);
		boolean written;
		try {
			String text = Files.readString(PATH);
			List<String> lines = new ArrayList<>(text.lines().toList());
			var current = new FileConfig();
			if (current.read(lines, changes) && current.values.keySet().containsAll(ENTRIES.keySet())) {
				String separator = text.contains("\r\n") ? "\r\n" : "\n";
				Files.writeString(PATH, String.join(separator, lines) + (text.endsWith("\n") ? separator : ""));
				Flashcarts.LOGGER.info("Updated {} in the config file {}", changes.keySet(), PATH);
				written = true;
			} else {
				written = write(updated);
			}
		} catch (IOException e) {
			written = write(updated);
		}
		Flashcarts.loadConfig();
		return written;
	}

	private static boolean write(Map<String, Object> values) {
		var text = new StringBuilder();
		String section = "";
		for (var entry : ENTRIES.values()) {
			if (!entry.section().equals(section)) {
				section = entry.section();
				text.append("\n# ").append(SECTIONS.get(section)).append("\n\n[").append(section).append("]\n");
			}
			text.append("# ").append(entry.comment()).append('\n').append(entry.key()).append(" = ").append(values.get(entry.id())).append('\n');
		}
		try {
			Files.createDirectories(PATH.getParent());
			Files.writeString(PATH, text);
			Flashcarts.LOGGER.info("Wrote the config file {}", PATH);
			return true;
		} catch (IOException e) {
			Flashcarts.LOGGER.error("Failed to write the config file", e);
			return false;
		}
	}

	private static void add(String section, String key, String comment, Object fallback) {
		add(section, key, comment, fallback, 0, 0);
	}

	private static void add(String section, String key, String comment, Object fallback, double min, double max) {
		var entry = new Entry(section, key, comment, fallback, min, max);
		ENTRIES.put(entry.id(), entry);
	}

	private static void cart(String section, String comment, boolean useExperimentalPhysics, int maxSpeed) {
		SECTIONS.put(section, comment);
		add(section, "useExperimentalPhysics", "Whether normal minecarts of this kind use experimental physics, vanilla: false", useExperimentalPhysics);
		add(section, "maxSpeedBlocksPerSecond", "Maximum speed for these minecarts in blocks per second, vanilla: 8", maxSpeed, 8, 256);
	}

	Object get(String id) {
		return values.get(id);
	}

	private boolean bool(String id) {
		return (Boolean) values.get(id);
	}

	private int integer(String id) {
		return (Integer) values.get(id);
	}

	@Override
	public boolean areCheaperRecipesEnabled() {
		return bool("cheaperRecipes");
	}

	@Override
	public boolean shouldShowSpeedometer() {
		return bool("showSpeedometer");
	}

	@Override
	public boolean shouldShowSpeedBar() {
		return bool("showSpeedBar");
	}

	@Override
	public boolean shouldShowStationTitle() {
		return bool("showStationTitle");
	}

	@Override
	public float getPoweredRailBoostPercentage() {
		return (Float) values.get("poweredRailBoostPercentage");
	}

	@Override
	public double getHaltSpeedThreshold() {
		return (Double) values.get("haltSpeedThreshold");
	}

	@Override
	public double getHaltSpeedMultiplier() {
		return (Double) values.get("haltSpeedMultiplier");
	}

	@Override
	public boolean shouldSmartHalt() {
		return bool("enableSmartHalt");
	}

	@Override
	public boolean areExpressRecipesEnabled() {
		return bool("expressMinecartRecipes");
	}

	@Override
	public IBuildConfig getBuildConfig() {
		return this;
	}

	@Override
	public boolean isRailSelectionBuildingEnabled() {
		return bool("buildTools.railSelectionBuilding");
	}

	@Override
	public int getRailSelectionBuildingMaxDistance() {
		return integer("buildTools.railSelectionBuildingMaxDistance");
	}

	@Override
	public int getPoweredRailFrequency() {
		return integer("buildTools.poweredRailFrequency");
	}

	@Override
	public boolean shouldShowSelection() {
		return bool("buildTools.showSelection");
	}

	@Override
	public boolean isRailExtendBuildingEnabled() {
		return bool("buildTools.railExtendBuilding");
	}

	@Override
	public int getRailExtendBuildingMaxDistance() {
		return integer("buildTools.railExtendBuildingMaxDistance");
	}

	@Override
	public ICartConfig getEmptyMinecartConfig() {
		return carts.get("emptyMinecart");
	}

	@Override
	public ICartConfig getMobMinecartConfig() {
		return carts.get("mobMinecart");
	}

	@Override
	public ICartConfig getPlayerMinecartConfig() {
		return carts.get("playerMinecart");
	}

	@Override
	public ICartConfig getTntMinecartConfig() {
		return carts.get("tntMinecart");
	}

	@Override
	public ICartConfig getChestMinecartConfig() {
		return carts.get("chestMinecart");
	}

	@Override
	public ICartConfig getHopperMinecartConfig() {
		return carts.get("hopperMinecart");
	}

	@Override
	public ICartConfig getFurnaceMinecartConfig() {
		return carts.get("furnaceMinecart");
	}

	@Override
	public ICartConfig getCommandBlockMinecartConfig() {
		return carts.get("commandBlockMinecart");
	}

	record Entry(String section, String key, String comment, Object fallback, double min, double max) {
		String id() {
			return section.isEmpty() ? key : section + "." + key;
		}
	}

	private record Cart(boolean useExperimentalPhysics, int maxSpeed) implements ICartConfig {
		@Override
		public boolean shouldUseExperimentalPhysics() {
			return useExperimentalPhysics;
		}

		@Override
		public int getMaxSpeed() {
			return maxSpeed;
		}
	}

}
