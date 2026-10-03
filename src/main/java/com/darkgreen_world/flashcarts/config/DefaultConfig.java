package com.darkgreen_world.flashcarts.config;

public class DefaultConfig implements IConfig {

	private DefaultConfig() {}

	public static DefaultConfig load() {
		return new DefaultConfig();
	}

	private static final IBuildConfig default_buildConfig = new IBuildConfig() {
		@Override
		public boolean isRailSelectionBuildingEnabled() {
			return default_railSelectionBuildingEnabled;
		}

		@Override
		public int getRailSelectionBuildingMaxDistance() {
			return default_railSelectionBuildingMaxDistance;
		}

		@Override
		public int getPoweredRailFrequency() {return default_poweredRailFrequency;}

		@Override
		public boolean shouldShowSelection() {
			return default_showSelection;
		}

		@Override
		public boolean isRailExtendBuildingEnabled() {
			return default_railExtendBuildingEnabled;
		}

		@Override
		public int getRailExtendBuildingMaxDistance() {
			return default_railExtendBuildingMaxDistance;
		}
	};

	@Override
	public IBuildConfig getBuildConfig() {
		return default_buildConfig;
	}

	private static final ICartConfig default_emptyMinecartConfig = new ICartConfig() {
		@Override
		public boolean shouldUseExperimentalPhysics() {
			return default_emptyUseExperimentalPhysics;
		}

		@Override
		public int getMaxSpeed() {
			return default_emptyMaxSpeed;
		}
	};

	private static final ICartConfig default_mobMinecartConfig = new ICartConfig() {
		@Override
		public boolean shouldUseExperimentalPhysics() {
			return default_mobUseExperimentalPhysics;
		}

		@Override
		public int getMaxSpeed() {
			return default_mobMaxSpeed;
		}
	};

	private static final ICartConfig default_playerMinecartConfig = new ICartConfig() {
		@Override
		public boolean shouldUseExperimentalPhysics() {
			return default_playerUseExperimentalPhysics;
		}

		@Override
		public int getMaxSpeed() {
			return default_playerMaxSpeed;
		}
	};

	private static final ICartConfig default_tntMinecartConfig = new ICartConfig() {
		@Override
		public boolean shouldUseExperimentalPhysics() {
			return default_tntUseExperimentalPhysics;
		}

		@Override
		public int getMaxSpeed() {
			return default_tntMaxSpeed;
		}
	};

	private static final ICartConfig default_redstoneCartsConfig = new ICartConfig() {
		@Override
		public boolean shouldUseExperimentalPhysics() {
			return default_redstoneCartsUseExperimentalPhysics;
		}

		@Override
		public int getMaxSpeed() {
			return default_redstoneCartsMaxSpeed;
		}
	};

	@Override
	public ICartConfig getEmptyMinecartConfig() {
		return default_emptyMinecartConfig;
	}

	@Override
	public ICartConfig getMobMinecartConfig() {
		return default_mobMinecartConfig;
	}

	@Override
	public ICartConfig getPlayerMinecartConfig() {
		return default_playerMinecartConfig;
	}

	@Override
	public ICartConfig getTntMinecartConfig() {
		return default_tntMinecartConfig;
	}

	@Override
	public ICartConfig getChestMinecartConfig() {
		return default_redstoneCartsConfig;
	}

	@Override
	public ICartConfig getHopperMinecartConfig() {
		return default_redstoneCartsConfig;
	}

	@Override
	public ICartConfig getFurnaceMinecartConfig() {
		return default_redstoneCartsConfig;
	}

	@Override
	public ICartConfig getCommandBlockMinecartConfig() {
		return default_redstoneCartsConfig;
	}

	@Override
	public boolean areCheaperRecipesEnabled() {
		return default_cheaperRecipes;
	}

	@Override
	public boolean shouldShowSpeedometer() {
		return default_showSpeedometer;
	}

	@Override
	public boolean shouldShowSpeedBar() {
		return default_showSpeedBar;
	}

	@Override
	public boolean shouldShowStationTitle() {
		return default_showStationTitle;
	}

	@Override
	public float getPoweredRailBoostPercentage() {
		return default_poweredRailBoostPercentage;
	}

	@Override
	public double getHaltSpeedThreshold() {
		return default_haltSpeedThreshold;
	}

	@Override
	public double getHaltSpeedMultiplier() {
		return default_haltSpeedMultiplier;
	}

	@Override
	public boolean shouldSmartHalt() {
		return default_smartHaltEnabled;
	}

	@Override
	public boolean areExpressRecipesEnabled() {
		return default_expressRecipes;
	}
}
