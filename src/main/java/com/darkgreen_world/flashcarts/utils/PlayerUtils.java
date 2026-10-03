package com.darkgreen_world.flashcarts.utils;

import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public abstract class PlayerUtils {

    public static void playDirectSound(ServerPlayer player, SoundEvent soundEvent, SoundSource soundCategory) {
        playDirectSound(player, soundEvent, soundCategory, 1.0f, 1.0f);
    }

    public static void playDirectSound(ServerPlayer player, SoundEvent soundEvent, SoundSource soundCategory, float volume, float pitch) {
        var packet = new ClientboundSoundEntityPacket(
                Holder.direct(soundEvent),
                soundCategory,
                player,
                volume,
                pitch,
                player.getRandom().nextLong()
        );
        player.connection.send(packet);
    }

    public static BlockHitResult rayCast(Player player, double distance) {
        Vec3 eyePosition = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getViewVector(1.0F);
        Vec3 end = eyePosition.add(lookVec.multiply(distance, distance, distance));
        ClipContext context = new ClipContext(
                eyePosition,
                end,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        );
        return player.level().clip(context);
    }

}
