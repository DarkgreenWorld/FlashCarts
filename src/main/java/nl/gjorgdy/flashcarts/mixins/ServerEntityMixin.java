package nl.gjorgdy.flashcarts.mixins;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import nl.gjorgdy.flashcarts.interfaces.IMinecartLerpContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerEntity.class)
public abstract class ServerEntityMixin {

	@Final
	@Shadow
	private ServerEntity.Synchronizer synchronizer;

	@Shadow
	@Final
	private Entity entity;

	@Shadow
	@Final
	private VecDeltaCodec positionCodec;

	@Shadow
	private byte lastSentYRot;

	@Shadow
	private byte lastSentXRot;

	@Definition(id = "AbstractMinecart", type = AbstractMinecart.class)
	@Expression("? instanceof AbstractMinecart")
	@Inject(method = "sendChanges()V", at = @At("MIXINEXTRAS:EXPRESSION"), cancellable = true)
	private void onNewPackets(CallbackInfo ci) {
		if (this.entity instanceof AbstractMinecart minecart) {
			if (minecart.getBehavior() instanceof IMinecartLerpContainer lerpContainer) {
				this.synchronizer.sendToTrackingPlayers(
					new ClientboundMoveMinecartPacket(
						minecart.getId(),
						lerpContainer.flashCarts$popSteps()
					)
				);

				this.lastSentYRot = Mth.packDegrees(this.entity.getYRot());
				this.lastSentXRot = Mth.packDegrees(this.entity.getXRot());
				this.positionCodec.setBase(this.entity.position());
				ci.cancel();
			}
		}
	}

}
