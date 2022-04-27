package cope.cosmos.client.features.modules.movement;

import cope.cosmos.asm.mixins.accessor.IEntity;
import cope.cosmos.client.events.motion.movement.MotionEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.MotionUtil;
import cope.cosmos.util.player.PlayerUtil;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author Doogie13, linustouchtips
 * @since 12/27/2021
 */
public class LongJumpModule extends Module {
    public static LongJumpModule INSTANCE;

    public LongJumpModule() {
        super("LongJump", Category.MOVEMENT, "Allows you to jump farther");
        INSTANCE = this;
    }

    // **************************** speeds ****************************

    public static Setting<Double> speed = new Setting<>("Speed", 0.0, 0.6, 1.0, 2)
            .setDescription("Jump speed");

    public static Setting<Double> accelerate = new Setting<>("Acceleration", 0.92, 0.92, 1.10, 2)
            .setDescription("Factor for acceleration speed");

    public static Setting<Double> glide = new Setting<>("Glide", 0.0, 0.65, 1.0, 2)
            .setDescription("Fall speed");

    // move speed
    private double moveSpeed;
    
    @SubscribeEvent
    public void onMotion(MotionEvent event) {
        // make sure the player is not in a liquid
        if (PlayerUtil.isInLiquid()) {
            moveSpeed = 0;
            return;
        }

        // make sure the player is not in a web
        if (((IEntity) mc.player).getInWeb()) {
            moveSpeed = 0;
            return;
        }

        // make sure the player can have speed applied
        if (mc.player.isOnLadder() || mc.player.capabilities.isFlying || mc.player.isElytraFlying() || mc.player.fallDistance > 2) {
            moveSpeed = 0;
            return;
        }

        // cancel vanilla movement
        event.setCanceled(true);

        // attempt jump if we are onGround and are actually trying to move
        if (mc.player.onGround && MotionUtil.isMoving()) {
            // set the event motion to 0.42
            event.setY(0.42);

            // only make the motion 0.42 if we want to do a normal hop
            mc.player.motionY = 0.42;

            // reset our current speed
            moveSpeed = speed.getValue();

            // apply speed effect if present
            if (mc.player.isPotionActive(MobEffects.SPEED)) {
                int amplifier = mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier();
                moveSpeed *= 1 + (0.2 * (amplifier + 1));
            }
        }

        else if (mc.player.motionY < 0) {
            // If we are falling, slow our fall
            event.setY(event.getY() * glide.getValue());
        }

        // the current movement input values of the user
        float forward = mc.player.movementInput.moveForward;
        float strafe = mc.player.movementInput.moveStrafe;
        float yaw = mc.player.rotationYaw;

        // find the rotations and inputs based on our current movements
        if (strafe > 0) {
            yaw += forward > 0 ? -45 : 45;
        }

        else if (strafe < 0) {
            yaw += forward > 0 ? 45 : -45;
        }

        strafe = 0;

        if (forward > 0) {
            forward = 1;
        }

        else if (forward < 0) {
            forward = -1;
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

        // multiply speed by acceleration (for next tick)
        moveSpeed *= accelerate.getValue();
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {
        // disable on rubberband or teleport
        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            moveSpeed = 0;
            disable(true);
        }
    }
}
