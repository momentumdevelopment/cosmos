package cope.cosmos.client.features.modules.miscellaneous;

import cope.cosmos.client.events.render.entity.RenderRotationsEvent;
import cope.cosmos.client.events.entity.player.RotationUpdateEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.holder.Rotation;
import cope.cosmos.util.holder.Rotation.Rotate;
import cope.cosmos.util.math.Timer;
import cope.cosmos.util.math.Timer.*;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

/**
 * @author linustouchtips
 * @since 07/24/2021
 */
public class AntiAimModule extends Module {
    public static AntiAimModule INSTANCE;

    public AntiAimModule() {
        super("AntiAim", Category.MISCELLANEOUS, "Makes you harder to hit");
        INSTANCE = this;
    }

    // **************************** general settings ****************************

    public static Setting<Yaw> yaw = new Setting<>("Yaw", Yaw.LINEAR)
            .setDescription("Changes how your yaw is rotated");

    public static Setting<Pitch> pitch = new Setting<>("Pitch", Pitch.NONE)
            .setDescription("Changes how your pitch is rotated");

    public static Setting<Rotate> rotate = new Setting<>("Rotation", Rotate.PACKET)
            .setAlias("Rotate")
            .setDescription("How to rotate")
            .setExclusion(Rotate.NONE);

    // rotation values
    private float aimYaw, aimPitch;

    // keeps track of rotations time
    private final Timer rotationTimer = new Timer();

    @Override
    public void onUpdate() {

        // randomized rotations
        Random aimRandom = new Random();

        switch (yaw.getValue()) {
            case LINEAR:
                // linear increase
                aimYaw += 5;
                break;
            case REVERSE:
                // linear decrease
                aimYaw -= 5;
                break;
            case RANDOM:
                // random yaw value
                aimYaw = aimRandom.nextInt() * 180;

                // randomize direction
                if (aimRandom.nextBoolean()) {
                    aimYaw *= -1;
                }

                break;
            case NONE:
                break;
        }

        switch (pitch.getValue()) {
            case RANDOM:

                // randomize pitch value
                aimPitch = aimRandom.nextInt() * 90;

                // randomize direction
                if (aimRandom.nextBoolean()) {
                    aimPitch *= -1;
                }

                break;
            case MIN_MAX:
                // toggle from min to max
                if (rotationTimer.passedTime(2, Format.TICKS)) {
                    // max
                    aimPitch = 90;
                    rotationTimer.resetTime();
                }

                else {
                    // min
                    aimPitch = -90;
                }

                break;
            case NONE:
                break;
        }
    }

    @SubscribeEvent
    public void onRotationUpdate(RotationUpdateEvent event) {

        // server-side update our rotations
        if (!rotate.getValue().equals(Rotate.NONE)) {

            // cancel vanilla rotations, we'll send our own
            event.setCanceled(true);

            // update rots
            if (rotate.getValue().equals(Rotate.CLIENT)) {
                mc.player.rotationYaw = aimYaw;
                mc.player.rotationYawHead = aimYaw;
                mc.player.rotationPitch = aimPitch;
            }

            // send rotations
            getCosmos().getRotationManager().setRotation(new Rotation(aimYaw, aimPitch));
        }
    }

    // anti aim renders should be priority for rotation rendering
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderRotations(RenderRotationsEvent event) {

        if (rotate.getValue().equals(Rotate.PACKET)) {

            // cancel the model rendering for rotations, we'll set it to our values
            event.setCanceled(true);

            // set our model angles; visual
            event.setYaw(aimYaw);
            event.setPitch(aimPitch);
        }
    }

    public enum Yaw {

        /**
         * Linear increase
         */
        LINEAR,

        /**
         * Linear decrease
         */
        REVERSE,

        /**
         * Randomize yaw values
         */
        RANDOM,

        /**
         * No yaw changes
         */
        NONE
    }

    public enum Pitch {

        /**
         * Randomize pitch values
         */
        RANDOM,

        /**
         * Toggle between min and max values for pitch
         */
        MIN_MAX,

        /**
         * No pitch changes
         */
        NONE
    }
}
