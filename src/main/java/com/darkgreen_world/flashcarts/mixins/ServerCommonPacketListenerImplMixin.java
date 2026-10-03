package com.darkgreen_world.flashcarts.mixins;

import com.darkgreen_world.flashcarts.config.ConfigDialog;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplMixin {

    // Injected after the packet has been moved to the server thread.
    @Inject(method = "handleCustomClickAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;handleCustomClickAction(Lnet/minecraft/resources/Identifier;Ljava/util/Optional;)V"), cancellable = true)
    private void handleConfigDialog(ServerboundCustomClickActionPacket packet, CallbackInfo ci) {
        if ((Object) this instanceof ServerGamePacketListenerImpl listener && packet.id().equals(ConfigDialog.ID)) {
            ConfigDialog.handle(listener.player, packet.payload());
            ci.cancel();
        }
    }

}
