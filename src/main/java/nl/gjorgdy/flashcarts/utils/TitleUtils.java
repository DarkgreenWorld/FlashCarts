package nl.gjorgdy.flashcarts.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.SignBlockEntity;

import java.util.Arrays;
import net.minecraft.world.level.block.entity.SignTextSlot;

public abstract class TitleUtils {

    public static void clearTitle(ServerPlayer player, boolean instant) {
        player.connection.send(new ClientboundSetTitlesAnimationPacket(0, 0, instant ? 0 : 10));
    }

    public static void sendTitle(ServerPlayer player, SignBlockEntity sign, boolean reapply) {
        var lines = sign.getText(SignTextSlot.FRONT).getMessages(false).stream()
                .map(Component::getString)
                .filter(s -> !s.isBlank())
                .toList();;
        var signColor = sign.getText(SignTextSlot.FRONT).getColor();
        var color = signColor == DyeColor.BLACK ? DyeColor.WHITE.getTextColor() : signColor.getTextColor();
        if (!lines.isEmpty()) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(reapply ? 0 : 20, 60, 20));
            player.connection.send(
                    new ClientboundSetTitleTextPacket(
                            Component.literal(lines.getFirst()).withColor(color)
                    )
            );
        }
        if (lines.size() > 1) {
            player.connection.send(
                    new ClientboundSetSubtitleTextPacket(
                            Component.literal(String.join(" ", lines.subList(1, lines.size())))
                                    .withColor(color)
                    )
            );
        }
    }

}
