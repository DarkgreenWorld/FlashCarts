package nl.gjorgdy.flashcarts.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
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
		var velocity = Math.abs(movement.x) + Math.abs(movement.z);
		var position = this.minecart.position();

		if (movement.y > 0) return 0;

		Direction direction;
		if (Math.abs(movement.x) >= Math.abs(movement.z)) {
			direction = movement.x < 0 ? Direction.WEST : Direction.EAST;
		} else {
			direction = movement.z < 0 ? Direction.NORTH : Direction.SOUTH;
		}

		var _pos = this.minecart.blockPosition();
		var _endPos = this.minecart.blockPosition();
		for (int i = 0; i < 8; i++) {
			var state = this.minecart.level().getBlockState(_pos);
			var stateBelow = this.minecart.level().getBlockState(_pos.below());
			if (state.is(Blocks.POWERED_RAIL)) {
				_endPos = _pos;
				if (state.getValue(PoweredRailBlock.SHAPE).isSlope()) {
					break;
				}
			} else if (stateBelow.is(Blocks.POWERED_RAIL)) {
				_endPos = _pos;
				if (stateBelow.getValue(PoweredRailBlock.SHAPE).isSlope()) {
					break;
				}
			} else break;
			_pos = _pos.relative(direction, 1);
		}

		double remainingDistance;
		if (direction == Direction.NORTH || direction == Direction.SOUTH) {
			remainingDistance = Math.abs((_endPos.getZ() + 0.5) - position.z);
		} else {
			remainingDistance = Math.abs((_endPos.getX() + 0.5) - position.x);
		}

		if (remainingDistance <= 0.01) return 0;

		var time = remainingDistance / velocity;
		var deceleration = velocity  / (2 * remainingDistance);
		var multiplier = time < 1 ? time : 1 - (deceleration);
		return Math.clamp(multiplier, 0.001, 0.99);
	}

}
