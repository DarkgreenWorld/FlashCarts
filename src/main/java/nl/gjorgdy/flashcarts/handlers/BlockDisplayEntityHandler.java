package nl.gjorgdy.flashcarts.handlers;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;

public class BlockDisplayEntityHandler {

    private int oldTopId = Integer.MAX_VALUE -1;
    private int topId = Integer.MAX_VALUE -1;

    private final ServerPlayer player;

    public BlockDisplayEntityHandler(ServerPlayer player) {
        this.player = player;
    }

    public void reset() {
        player.connection.send(
            new ClientboundRemoveEntitiesPacket(
                IntStream.rangeClosed(topId, Integer.MAX_VALUE - 1).toArray()
            )
        );
        topId = Integer.MAX_VALUE -1;
        oldTopId = Integer.MAX_VALUE -1;
    }

    public void resetCounter() {
        oldTopId = topId;
        topId = Integer.MAX_VALUE -1;
    }

    public void removeOld() {
        if (oldTopId > topId) return;
        player.connection.send(
            new ClientboundRemoveEntitiesPacket(
                IntStream.rangeClosed(oldTopId, topId).toArray()
            )
        );
    }

    public int count() {
        return Integer.MAX_VALUE - topId - 1;
    }

    /**
     * Adds a block display entity for the given block state and position, with an optional glow color.
     * @param blockState the block state to display.
     * @param pos the position to display the block at.
     * @param color the glow color to use for the block display entity, or -1 to disable glow.
     * @return the entity ID of the added block display entity, or -1 if the entity could not be created.
     */
    public int add(BlockState blockState, BlockPos pos, int color) {
        var displayEntity = EntityTypes.BLOCK_DISPLAY.create(
                player.level(),
                EntitySpawnReason.COMMAND
        );
        if (displayEntity == null) return -1;

        displayEntity.setId(topId--);
        displayEntity.setUUID(UUID.randomUUID());
        displayEntity.setBlockState(blockState);

        displayEntity.setPos(new Vec3(pos));
        displayEntity.setGlowingTag(true);
        displayEntity.setGlowColorOverride(color);

        if (topId < oldTopId) {
            player.connection.send(
                new ClientboundAddEntityPacket(
                    displayEntity,
                    displayEntity.getId(),
                    pos
                )
            );
        } else {
            player.connection.send(
                new ClientboundEntityPositionSyncPacket(
                    displayEntity.getId(),
                    PositionMoveRotation.of(displayEntity),
                    true
                )
            );
        }
        player.connection.send(
            new ClientboundSetEntityDataPacket(
                displayEntity.getId(),
                Objects.requireNonNull(displayEntity.getEntityData().getNonDefaultValues())
            )
        );
        return displayEntity.getId();
    }

}
