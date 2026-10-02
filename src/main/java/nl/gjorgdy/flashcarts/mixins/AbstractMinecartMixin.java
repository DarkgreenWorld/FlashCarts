package nl.gjorgdy.flashcarts.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.entity.vehicle.minecart.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import nl.gjorgdy.flashcarts.Flashcarts;
import nl.gjorgdy.flashcarts.utils.TitleUtils;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin extends VehicleEntity {

	@Unique
	private final AbstractMinecart self = (AbstractMinecart) (Object) this;

	@Unique
	private boolean standingStill = false;

	@Mutable
	@Final
	@Shadow
	private MinecartBehavior behavior;

	public AbstractMinecartMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@WrapOperation(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/minecart/AbstractMinecart;useExperimentalMovement(Lnet/minecraft/world/level/Level;)Z"))
	private boolean initWithCorrectBehavior(Level level, Operation<Boolean> original) {
		return true;
	}

	@Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V", at= @At("RETURN"))
	public void forceSync(EntityType<?> entityType, Level level, CallbackInfo ci) {
		needsSync = true;
	}

	@Inject(at = @At("HEAD"), method = "tick")
	public void tick(CallbackInfo ci) {
		var speed = getSpeedBlocksPerSecond();
		getPassengers().forEach(p -> {
			if (p instanceof ServerPlayer player && tickCount % 2 == 0) {
				var speedometer = Flashcarts.config.shouldShowSpeedometer();
				var speedBar = Flashcarts.config.shouldShowSpeedBar();
				var stationTitle = Flashcarts.config.shouldShowStationTitle();
				var stringBuilder = new StringBuilder();
				if (speed > Flashcarts.config.getHaltSpeedThreshold()) {
					if (speedBar) {
						int bars = (int) Math.floor(speed / Flashcarts.config.getPlayerMinecartConfig().getMaxSpeed() * 10);
						stringBuilder
							.append("§a").repeat("▮", Math.min(bars, 6))
							.append("§e").repeat("▮", Math.clamp(bars - 6, 0, 2))
							.append("§6").repeat("▮", Math.clamp(bars - 8, 0, 1))
							.append("§c").repeat("▮", Math.clamp(bars - 9, 0, 1))
							.append("§7").repeat("▮", 10 - bars);
					}
					if (speedometer) {
						stringBuilder.append(String.format(" %,6.2f b/s", speed));
					}
					if (stationTitle && standingStill) {
						TitleUtils.clearTitle(player, false);
						standingStill = false;
					}
				} else {
					if (speedBar) {
						stringBuilder.append("§7").repeat("▮", 10);
					}
					if (speedometer) {
						stringBuilder.append(String.format(" %,6.2f b/s", 0f));
					}
					if (stationTitle && self.level().getBlockEntity(self.blockPosition().below().below()) instanceof SignBlockEntity sign) {
						TitleUtils.sendTitle(player, sign, standingStill);
					}
					standingStill = true;
				}
				if (!stringBuilder.isEmpty()) {
					player.sendSystemMessage(Component.literal(stringBuilder.toString()), true);
				}
			}
		});
		if (tickCount % 5 != 0) return;
		var cartConfig = Flashcarts.config.getConfigForMinecart(self);
		if (cartConfig != null && cartConfig.shouldUseExperimentalPhysics()) {
			if (this.behavior instanceof OldMinecartBehavior) {
				this.behavior = new NewMinecartBehavior(self);
			}
		}
		else {
			if (this.behavior instanceof NewMinecartBehavior) {
				this.behavior = new OldMinecartBehavior(self);
			}
		}
	}

	@Unique
	private double getSpeedBlocksPerSecond() {
		var cartConfig = Flashcarts.config.getConfigForMinecart(self);
		if (cartConfig == null) {
			return self.getKnownSpeed().length() * 20;
		}
		return Math.min(this.getKnownSpeed().length() * 20, cartConfig.getMaxSpeed());
	}

// override experimental physics checks

	@WrapOperation(method = "getCurrentBlockPosOrRailBelow", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/minecart/AbstractMinecart;useExperimentalMovement(Lnet/minecraft/world/level/Level;)Z"))
	private boolean getCurrentBlockPosOrRailBelow(Level level, Operation<Boolean> original) {
		return behavior instanceof NewMinecartBehavior;
	}

	@WrapOperation(method = "pushOtherMinecart", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/minecart/AbstractMinecart;useExperimentalMovement(Lnet/minecraft/world/level/Level;)Z"))
	private boolean pushOtherMinecart(Level level, Operation<Boolean> original) {
		return behavior instanceof NewMinecartBehavior;
	}

	@WrapOperation(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/minecart/AbstractMinecart;useExperimentalMovement(Lnet/minecraft/world/level/Level;)Z"))
	private boolean move(Level level, Operation<Boolean> original) {
		return behavior instanceof NewMinecartBehavior;
	}

	@WrapOperation(method = "applyEffectsFromBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/minecart/AbstractMinecart;useExperimentalMovement(Lnet/minecraft/world/level/Level;)Z"))
	private boolean applyEffectsFromBlocks(Level level, Operation<Boolean> original) {
		return behavior instanceof NewMinecartBehavior;
	}

	// always true for anything else, no matter the world settings

	@WrapMethod(method = "useExperimentalMovement")
	private static boolean forceExperimentalMovement(Level level, Operation<Boolean> original) {
		return true;
	}

}
