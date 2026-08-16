package nl.gjorgdy.flashcarts.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import nl.gjorgdy.flashcarts.Flashcarts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(NewMinecartBehavior.class)
public abstract class NewMinecartBehaviorMixin extends MinecartBehavior {

	protected NewMinecartBehaviorMixin(AbstractMinecart abstractMinecart) {
		super(abstractMinecart);
	}

	@WrapMethod(method = "getMaxSpeed")
	public double overrideMaxSpeed(ServerLevel serverLevel, Operation<Double> original) {
		var cartConfig = Flashcarts.config.getConfigForMinecart(minecart);
		int maxSpeed = cartConfig != null && cartConfig.shouldUseExperimentalPhysics()
				? cartConfig.getMaxSpeed()
				: 8;
		return maxSpeed * (this.minecart.isInWater() ? 0.5 : 1.0) / 20.0;
	}

	@ModifyConstant(
			method = "calculateBoostTrackSpeed(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/phys/Vec3;",
			constant = @Constant(doubleValue = 0.06)
	)
	private double replaceBoostPercentage(double constant) {
		return Flashcarts.config.getPoweredRailBoostPercentage();
	}

	@ModifyConstant(
		method = "calculateHaltTrackSpeed(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/phys/Vec3;",
		constant = @Constant(doubleValue = 0.03)
	)
	private double replaceHaltThreshold(double constant) {
		return Flashcarts.config.getHaltSpeedThreshold();
	}

	@ModifyArg(
			method = "calculateHaltTrackSpeed(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/phys/Vec3;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;scale(D)Lnet/minecraft/world/phys/Vec3;"),
			index = 0
	)
	private double replaceHaltMultiplier(double d) {
		if (!Flashcarts.config.shouldSmartHalt()) return Flashcarts.config.getHaltSpeedMultiplier();

		var movement = this.minecart.getDeltaMovement();
		var velocity = movement.length();
		var position = this.minecart.position();

		Direction direction;
		if (movement.z < 0) {
			direction = Direction.NORTH;
		} else if (movement.z > 0) {
			direction = Direction.SOUTH;
		} else if (movement.x < 0) {
			direction = Direction.WEST;
		} else {
			direction = Direction.EAST;
		}

		var _pos = this.minecart.blockPosition();
		for (int i = 0; i < 8; i++) {
			var state = this.minecart.level().getBlockState(_pos);
			var below = _pos.below();
			var stateBelow = this.minecart.level().getBlockState(below);

			if (state.is(Blocks.POWERED_RAIL)) {
				continue;
			} else if (stateBelow.is(Blocks.POWERED_RAIL)) {
				_pos = below;
			} else {
				double distance;
				if (direction == Direction.NORTH || direction == Direction.SOUTH) {
					distance = Math.abs(_pos.getZ() - position.z) - 0.25;
				} else {
					distance = Math.abs(_pos.getX() - position.x) - 0.25;
				}
				if (movement.y != 0) {
					distance -= 0.25;
				}
				double velocityTarget = Flashcarts.config.getHaltSpeedThreshold();
				double deceleration = ((velocity * velocity) - (velocityTarget * velocityTarget)) / (2 * distance);
				return 1 - (deceleration / velocity);
			}
			_pos = _pos.relative(direction, 1);
		}

		return Flashcarts.config.getHaltSpeedMultiplier();
	}

}
