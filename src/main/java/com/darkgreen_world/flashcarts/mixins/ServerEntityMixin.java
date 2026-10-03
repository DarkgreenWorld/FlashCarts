package com.darkgreen_world.flashcarts.mixins;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import com.darkgreen_world.flashcarts.handlers.ExpressTorchHandler;
import com.darkgreen_world.flashcarts.interfaces.IMinecartLerpContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
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

	@Shadow
	protected abstract void sendDirtyEntityData();

	@Definition(id = "AbstractMinecart", type = AbstractMinecart.class)
	@Expression("? instanceof AbstractMinecart")
	@Inject(method = "sendChanges()V", at = @At("MIXINEXTRAS:EXPRESSION"), cancellable = true)
	private void onNewPackets(CallbackInfo ci) {
		if (this.entity instanceof AbstractMinecart minecart) {
			if (minecart.getBehavior() instanceof IMinecartLerpContainer lerpContainer) {
				// Cancelling skips the vanilla entity data synchronization, so it is performed here instead.
				this.sendDirtyEntityData();

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

	@ModifyArg(method = "handleMinecartPosRot", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerEntity$Synchronizer;sendToTrackingPlayers(Lnet/minecraft/network/protocol/Packet;)V"))
	private Packet<? super ClientGamePacketListener> onMinecartPacket(Packet<? super ClientGamePacketListener> packet) {
		if (packet instanceof ClientboundMoveMinecartPacket move && this.entity instanceof AbstractMinecart minecart) {
			ExpressTorchHandler.onStepsSent(minecart, move.lerpSteps());
		}
		return packet;
	}

	@Inject(method = "sendChanges()V", at = @At("TAIL"))
	private void afterPackets(CallbackInfo ci) {
		if (this.entity instanceof AbstractMinecart minecart) {
			ExpressTorchHandler.tick(minecart);
		}
	}

}
