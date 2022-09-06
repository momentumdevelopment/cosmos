package cope.cosmos.client.features.modules.movement;

import cope.cosmos.client.events.motion.movement.MotionEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.player.FreecamModule;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.MotionUtil;
import cope.cosmos.util.string.StringFormatter;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author aesthetical, Doogie13, linustouchtips
 * @since 12/27/2021
 *
 * Rewrote on 6/21/22
 */
public class LongJumpModule extends Module {
    public static LongJumpModule INSTANCE;

    public LongJumpModule() {
        super("LongJump", Category.MOVEMENT, "Allows you to jump farther", () -> StringFormatter.formatEnum(mode.getValue()));
        INSTANCE = this;
    }

    // **************************** general ****************************

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.NORMAL)
            .setDescription("Mode of jump");

    public static Setting<Double> boost = new Setting<>("Boost", 0.1D, 4.5D, 10.0D, 1)
            .setAlias("Speed")
            .setVisible(() -> mode.getValue().equals(Mode.NORMAL))
            .setDescription("The boost speed");

    public static Setting<Boolean> potionFactor = new Setting<>("PotionFactor", true)
            .setDescription("If to factor in potion effects for move speed");

    // speed
    private double moveSpeed;
    private double distance;

    // stage
    private LongJumpStage stage = LongJumpStage.START;

    @Override
    public void onDisable() {
        super.onDisable();

        moveSpeed = 0;
        distance = 0;
        stage = LongJumpStage.START;
    }

    @Override
    public void onUpdate() {

        // our latest move speed
        distance = Math.sqrt(StrictMath.pow(mc.player.posX - mc.player.prevPosX, 2) + StrictMath.pow(mc.player.posZ - mc.player.prevPosZ, 2));
    }

    @SubscribeEvent
    public void onMotion(MotionEvent event) {

        // incompatibilities
        if (PacketFlightModule.INSTANCE.isEnabled() || FlightModule.INSTANCE.isEnabled() || FreecamModule.INSTANCE.isEnabled()) {
            return;
        }

        // our base NCP speed
        double baseSpeed = 0.2873;

        // factor
        if (potionFactor.getValue()) {
            if (mc.player.isPotionActive(MobEffects.SPEED)) {
                double amplifier = mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier();
                baseSpeed *= 1 + (0.2 * (amplifier + 1));
            }

            if (mc.player.isPotionActive(MobEffects.SLOWNESS)) {
                double amplifier = mc.player.getActivePotionEffect(MobEffects.SLOWNESS).getAmplifier();
                baseSpeed /= 1 + (0.2 * (amplifier + 1));
            }
        }

        // starting stage
        if (stage.equals(LongJumpStage.START) && MotionUtil.isMoving()) {
            stage = LongJumpStage.JUMP;

            // set our base speed
            moveSpeed = boost.getValue() * baseSpeed - 0.01;
        }

        else if (stage.equals(LongJumpStage.JUMP)) {
            stage = LongJumpStage.SPEED;

            // jump up
            mc.player.motionY = 0.42;
            event.setY(0.42);

            // accelerate, TODO: setting?
            moveSpeed *= 2.149;
        }

        else if (stage.equals(LongJumpStage.SPEED)) {
            stage = LongJumpStage.COLLISION;

            // adjust our moveSpeed
            double adjusted = 0.66 * (distance - baseSpeed);
            moveSpeed = distance - adjusted;
        }

        else {
            // check for head space
            if (!mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, mc.player.motionY, 0.0)).isEmpty() && mc.player.collidedVertically) {
                stage = LongJumpStage.START;
            }

            moveSpeed = distance - distance / 159;
        }

        // we want to min at our baseSpeed
        moveSpeed = Math.max(moveSpeed, baseSpeed);

        event.setCanceled(true);

        // the current movement input values of the user
        float forward = mc.player.movementInput.moveForward;
        float strafe = mc.player.movementInput.moveStrafe;
        float yaw = mc.player.rotationYaw;

        if (!MotionUtil.isMoving()) {
            event.setX(0);
            event.setZ(0);
        }

        else if (forward != 0) {
            if (strafe >= 1) {
                yaw += (forward > 0 ? -45 : 45);
                strafe = 0;
            }

            else if (strafe <= -1) {
                yaw += (forward > 0 ? 45 : -45);
                strafe = 0;
            }

            if (forward > 0) {
                forward = 1;
            }

            else if (forward < 0) {
                forward = -1;
            }
        }

        // our facing values, according to movement not rotations
        double cos = Math.cos(Math.toRadians(yaw + 90));
        double sin = Math.sin(Math.toRadians(yaw + 90));

        // update the movements
        event.setX((forward * moveSpeed * cos) + (strafe * moveSpeed * sin));
        event.setZ((forward * moveSpeed * sin) - (strafe * moveSpeed * cos));

        // if we're not inputting any movements, then we shouldn't be adding any motion
        if (!MotionUtil.isMoving()) {
            event.setX(0);
            event.setZ(0);
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {

        // disable on rubberband or teleport
        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            disable(true);
        }
    }

    public enum LongJumpStage {

        /**
         * The starting stage, this is where we set our initial speed
         */
        START,

        /**
         * After the starting stage, we want to jump. We also accelerate like the vanilla game does, except it's much more of a boost
         */
        JUMP,

        /**
         * Speeds up
         */
        SPEED,

        /**
         * Checks for head space and slows down
         */
        COLLISION
    }

    public enum Mode {

        /**
         * Preset jump motion (Direkt Longjump)
         * TODO: implement
         */
        COWABUNGA,

        /**
         * Strafe boost long jump
         */
        NORMAL
    }
}