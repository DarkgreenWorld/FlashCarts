package nl.gjorgdy.flashcarts.config;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.*;
import org.jspecify.annotations.Nullable;

public interface IConfig {

	boolean default_cheaperRecipes = true;

	boolean default_showSpeedometer = true;
	boolean default_showSpeedBar = true;
	boolean default_showStationTitle = true;

	boolean default_railSelectionBuildingEnabled = true;
	int default_railSelectionBuildingMaxDistance = 64;
	int default_poweredRailFrequency = 8;
	boolean default_showSelection = true;
	boolean default_railExtendBuildingEnabled = false;
	int default_railExtendBuildingMaxDistance = 16;

	float default_poweredRailBoostPercentage = 0.12f;
	double default_haltSpeedThreshold = 0.05;
	double default_haltSpeedMultiplier = 0.3;
	boolean default_smartHaltEnabled = true;

	boolean default_emptyUseExperimentalPhysics = true;
	int default_emptyMaxSpeed = 8;

	boolean default_mobUseExperimentalPhysics = true;
	int default_mobMaxSpeed = 8;

	boolean default_playerUseExperimentalPhysics = true;
	int default_playerMaxSpeed = 64;

	boolean default_tntUseExperimentalPhysics = true;
	int default_tntMaxSpeed = 32;

	boolean default_redstoneCartsUseExperimentalPhysics = false;
	int default_redstoneCartsMaxSpeed = 8;

	/**
	 * Whether to enable cheaper recipes for rails and minecarts.
	 * @return whether to enable cheaper recipes.
	 */
	boolean areCheaperRecipesEnabled();

	/**
	 * Whether to show the speedometer (the text that shows the current speed in blocks per second).
	 * @return whether to show the speedometer.
	 */
	boolean shouldShowSpeedometer();

	/**
	 * Whether to show the speed bar (the segment bar that represents the current speed as a percentage of max speed).
	 * @return whether to show the speed bar.
	 */
	boolean shouldShowSpeedBar();

	/**
	 * Whether to show the station title (the title that shows the name of the station when a minecart stops).
	 * @return whether to show the station title.
	 */
	boolean shouldShowStationTitle();

	/**
	 * Get the percentage of boost a powered rail should give.
	 * @return the percentage of boost a powered rail should give.
	 */
	float getPoweredRailBoostPercentage();

	/**
	 * Get the speed threshold below which a minecart is considered halted.
	 * @return the speed threshold below which a minecart is considered halted.
	 */
	double getHaltSpeedThreshold();

	/**
	 * Get the multiplier applied to speed when a minecart is being halted.
	 * @return the multiplier applied to speed when a minecart is being halted.
	 */
	double getHaltSpeedMultiplier();

	/**
	 * Whether to enable smart halting (minecarts halt based on the length of powered rails).
	 * @return if smart halt enabled
	 */
	boolean shouldSmartHalt();

	/**
	 * Get the configuration for building tools.
	 * @return the configuration for building tools.
	 */
	IBuildConfig getBuildConfig();

	/**
	 * Get the configuration for empty minecarts.
	 * @return the configuration for empty minecarts.
	 */
	ICartConfig getEmptyMinecartConfig();

	/**
	 * Get the configuration for minecarts with mobs in them (excluding players).
	 * @return the configuration for mob minecarts.
	 */
	ICartConfig getMobMinecartConfig();

	/**
	 * Get the configuration for minecarts with players in them.
	 * @return the configuration for player minecarts.
	 */
	ICartConfig getPlayerMinecartConfig();

	/**
	 * Get the configuration for TNT minecarts.
	 * @return the configuration for TNT minecarts.
	 */
	ICartConfig getTntMinecartConfig();

	/**
	 * Get the configuration for chest minecarts.
	 * @return the configuration for chest minecarts.
	 */
	ICartConfig getChestMinecartConfig();

	/**
	 * Get the configuration for hopper minecarts.
	 * @return the configuration for hopper minecarts.
	 */
	ICartConfig getHopperMinecartConfig();

	/**
	 * Get the configuration for furnace minecarts.
	 * @return the configuration for furnace minecarts.
	 */
	ICartConfig getFurnaceMinecartConfig();

	/**
	 * Get the configuration for command block minecarts.
	 * @return the configuration for command block minecarts.
	 */
	ICartConfig getCommandBlockMinecartConfig();

	@Nullable
	default ICartConfig getConfigForMinecart(AbstractMinecart minecart) {
		if (minecart instanceof MinecartTNT) return getTntMinecartConfig();
		if (minecart instanceof Minecart) {
			if (minecart.getPassengers().isEmpty()) return getEmptyMinecartConfig();
			if (minecart.getFirstPassenger() instanceof Player) return getPlayerMinecartConfig();
			if (!minecart.getPassengers().isEmpty()) return getMobMinecartConfig();
		}
		if (minecart instanceof MinecartChest) return getChestMinecartConfig();
		if (minecart instanceof MinecartHopper) return getHopperMinecartConfig();
		if (minecart instanceof MinecartFurnace) return getFurnaceMinecartConfig();
		if (minecart instanceof MinecartCommandBlock) return getCommandBlockMinecartConfig();
		return null;
	}

}
