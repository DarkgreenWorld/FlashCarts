package nl.gjorgdy.flashcarts.mixins;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.OldMinecartBehavior;
import net.minecraft.world.phys.Vec3;
import nl.gjorgdy.flashcarts.interfaces.IMinecartLerpContainer;
import nl.gjorgdy.flashcarts.utils.RailUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedList;
import java.util.List;

@Mixin(OldMinecartBehavior.class)
public abstract class OldMinecartBehaviorMixin extends MinecartBehavior implements IMinecartLerpContainer {

	@Unique
	private final List<NewMinecartBehavior.MinecartStep> steps = new LinkedList<>();

	@Unique
	private NewMinecartBehavior.MinecartStep lastStep;

	@Unique
	private boolean stopped = false;

	protected OldMinecartBehaviorMixin(AbstractMinecart abstractMinecart) {
		super(abstractMinecart);
		addStep();
		lastStep = steps.getLast();
	}

	@Inject(method = "moveAlongTrack", at = @At("RETURN"))
	public void onMoveAlongTrack(CallbackInfo ci) {
		addStep();
	}

	@Unique
	private void resetSteps() {
		var lastStep = this.steps.isEmpty() ? null : this.steps.getLast();
		this.steps.clear();
		if (lastStep != null) {
			this.steps.add(
				new NewMinecartBehavior.MinecartStep(lastStep.position(), stopped ? Vec3.ZERO : lastStep.movement(), lastStep.yRot(), lastStep.xRot(), 0.0f)
			);
			this.lastStep = lastStep;
		}
	}

	@Override
	public List<NewMinecartBehavior.MinecartStep> flashCarts$popSteps() {
		if (this.steps.isEmpty()) addStep();
		var stepsToReturn = ImmutableList.copyOf(this.steps);
		if (this.steps.size() > 1) resetSteps();
		return stepsToReturn;
	}

	@Unique
	private void addStep() {
		var movement = this.steps.isEmpty() ? Vec3.ZERO : minecart.position().subtract(this.steps.getLast().position());
		// movement since first step
		if (movement.length() < 0.01) {
			if (!stopped) {
				addStep(calculateStillStep());
				stopped = true;
			}
			return;
		}
		stopped = false;
		addStep(calculateMovingStep(movement));
	}

	@Unique
	private void addStep(NewMinecartBehavior.MinecartStep step) {
		if (step == null) return;
		this.steps.add(step);
		this.lastStep = step;
	}

	@Unique
	private NewMinecartBehavior.MinecartStep calculateMovingStep(Vec3 movement) {
		var distance = movement.length();

		// horizontal rotation
		float yRot = minecart.getYRot() * -1;

		// vertical rotation
		float xRot = 0f;
		if (movement.y > 0.01) {
			xRot = 45f;
		} else if (movement.y < -0.01) {
			xRot = -45f;
		}
		xRot *= minecart.isFlipped() ? -1.0F : 1.0F;

		return new NewMinecartBehavior.MinecartStep(
			minecart.position(),
			distance < 0.1 ? Vec3.ZERO : movement,
			yRot, xRot,
			(float) distance
		);
	}

	@Unique
	private NewMinecartBehavior.MinecartStep calculateStillStep() {
		var block = level().getBlockState(minecart.blockPosition());
		var railShapeOpt = RailUtils.getRailShape(block);
		if (railShapeOpt.isEmpty()) return this.lastStep;
		var railShape = railShapeOpt.get();

		// horizontal rotation
		float yRot = switch (railShape) {
			case NORTH_EAST, SOUTH_WEST -> -135F;
			case ASCENDING_NORTH, ASCENDING_SOUTH, NORTH_SOUTH -> 90F;
			case SOUTH_EAST, NORTH_WEST -> 45F;
			case ASCENDING_EAST, ASCENDING_WEST, EAST_WEST -> 0.0F;
		};
		yRot *= minecart.isFlipped() ? -1.0F : 1.0F;
		
		// vertical rotation
		float xRot = switch (railShape) {
			case ASCENDING_WEST, ASCENDING_SOUTH -> 45F;
			case ASCENDING_NORTH, ASCENDING_EAST -> -45F;
			default -> 0F;
		};
		if (this.lastStep != null) {
			var deltaYRot = this.lastStep.yRot() - yRot;
			if (deltaYRot % 180 == 0) yRot = this.lastStep.yRot();
		}
		if (this.lastStep != null && this.lastStep.xRot() != 0f && this.lastStep.xRot() != xRot) {
			xRot = 0f;
		}

		return new NewMinecartBehavior.MinecartStep(
			minecart.position(),
			Vec3.ZERO,
			yRot, xRot,
			1.0F
		);
	}

}
