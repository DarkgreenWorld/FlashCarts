package com.darkgreen_world.flashcarts.handlers;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.IdentifiedUniqueEntityAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.UniqueIdentifiableAttachment;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Brightness;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior.MinecartStep;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import com.darkgreen_world.flashcarts.Flashcarts;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a redstone torch head on the rear wall of an express minecart.
 * <p>
 * The torch is a virtual block display that rides the minecart as a passenger, so clients move it along with the
 * minecart. Its rotation, however, is not inherited from the vehicle and has to be sent separately. Clients do not
 * apply a minecart rotation immediately; they interpolate along the steps of each movement packet over three ticks.
 * To keep the torch aligned with the minecart, this class reproduces that interpolation on the server and sends the
 * resulting rotation every tick.
 */
public class ExpressTorchHandler extends ElementHolder {

    private static final Identifier ID = Identifier.fromNamespaceAndPath(Flashcarts.MOD_ID, "express_torch");

    // Position of the torch base in minecart model space, where x points towards the rear and y points up.
    private static final Vector3f POSITION = new Vector3f(0.5625f, 0.25f, 0f);
    // Keeps the torch stem within the two pixel thick cart wall, so that only the head is visible.
    private static final Vector3f SCALE = new Vector3f(0.8f, 1f, 0.8f);
    // Display entities pitch around their x axis, whereas minecarts pitch around their z axis.
    private static final Quaternionf ROTATION = new Quaternionf().rotationY(Mth.HALF_PI);
    // Height above the minecart pivot at which passengers are placed.
    private static final float RIDE_HEIGHT = 0.1875f;
    // Number of ticks over which clients interpolate a minecart movement packet.
    private static final int LERP_TICKS = 3;

    private final BlockDisplayElement torch = new BlockDisplayElement(Blocks.REDSTONE_TORCH.defaultBlockState());

    // Copy of the client side interpolation state, see NewMinecartBehavior.
    private final List<MinecartStep> sentSteps = new ArrayList<>();
    private List<MinecartStep> lerpSteps = List.of();
    private float lerpWeight;
    private int lerpDelay;
    private float lerpStartYRot;
    private float lerpStartXRot;
    private float yRot;
    private float xRot;
    private boolean sentFlipped;
    private boolean flipped;

    private ExpressTorchHandler(AbstractMinecart minecart) {
        yRot = minecart.getYRot();
        xRot = minecart.getXRot();
        sentFlipped = flipped = minecart.isFlipped();
        torch.setScale(SCALE);
        torch.setLeftRotation(ROTATION);
        torch.setBrightness(new Brightness(15, 15));
        torch.setTeleportDuration(1);
        torch.ignorePositionUpdates();
        // Spawning the display at its riding position prevents a visible jump when it is mounted.
        torch.setOffset(new Vec3(0, RIDE_HEIGHT, 0));
        align();
        addElement(torch);
    }

    public static void attach(AbstractMinecart minecart) {
        var holder = new ExpressTorchHandler(minecart);
        IdentifiedUniqueEntityAttachment.ofTicking(ID, holder, minecart);
        VirtualEntityUtils.addVirtualPassenger(minecart, holder.torch.getEntityId());
    }

    private static ExpressTorchHandler get(AbstractMinecart minecart) {
        var attachment = UniqueIdentifiableAttachment.get(minecart, ID);
        return attachment != null && attachment.holder() instanceof ExpressTorchHandler holder ? holder : null;
    }

    /**
     * Records the movement steps that were sent to clients during the current tick.
     */
    public static void onStepsSent(AbstractMinecart minecart, List<MinecartStep> steps) {
        var holder = get(minecart);
        if (holder == null) return;
        holder.sentSteps.addAll(steps);
        holder.sentFlipped = minecart.isFlipped();
    }

    /**
     * Advances the reproduced interpolation by one tick and sends the resulting torch rotation.
     */
    public static void tick(AbstractMinecart minecart) {
        var holder = get(minecart);
        if (holder == null) return;
        holder.lerp();
        holder.align();
        holder.torch.tick();
    }

    // Reproduces NewMinecartBehavior#lerpClientPositionAndRotation and NewMinecartBehavior#getCurrentLerpStep.
    private void lerp() {
        if (--lerpDelay <= 0) {
            lerpStartYRot = yRot;
            lerpStartXRot = xRot;
            lerpSteps = List.copyOf(sentSteps);
            sentSteps.clear();
            lerpWeight = 0f;
            for (var step : lerpSteps) lerpWeight += step.weight();
            lerpDelay = lerpWeight == 0f ? 0 : LERP_TICKS;
            // When the minecart reverses, the torch moves to the other end without interpolation.
            torch.setInterpolationDuration(flipped == sentFlipped ? 1 : 0);
            flipped = sentFlipped;
        }
        if (lerpSteps.isEmpty()) return;

        float target = lerpWeight * (LERP_TICKS - lerpDelay + 1) / LERP_TICKS;
        int index = lerpSteps.size() - 1;
        float progress = 1f;
        float passed = 0f;
        for (int i = 0; i < lerpSteps.size(); i++) {
            float weight = lerpSteps.get(i).weight();
            if (weight <= 0f) continue;
            passed += weight;
            if (passed >= target) {
                index = i;
                progress = (target - (passed - weight)) / weight;
                break;
            }
        }
        var step = lerpSteps.get(index);
        yRot = Mth.rotLerp(progress, index > 0 ? lerpSteps.get(index - 1).yRot() : lerpStartYRot, step.yRot());
        xRot = Mth.rotLerp(progress, index > 0 ? lerpSteps.get(index - 1).xRot() : lerpStartXRot, step.xRot());
    }

    private void align() {
        torch.setYaw(90f - yRot);
        torch.setPitch(-xRot);
        float pitch = -xRot * Mth.DEG_TO_RAD;
        // The display rides above the minecart pivot, so the offset is corrected for the current pitch.
        torch.setTranslation(
            new Vector3f(flipped ? -POSITION.x : POSITION.x, POSITION.y, POSITION.z)
                .sub(0.5f * SCALE.x, 0f, 0.5f * SCALE.z)
                .rotate(ROTATION)
                .sub(0f, RIDE_HEIGHT * Mth.cos(pitch), -RIDE_HEIGHT * Mth.sin(pitch))
        );
        torch.startInterpolationIfDirty();
    }

}
