package cope.cosmos.client.features.modules.movement;

import cope.cosmos.asm.mixins.accessor.IEntity;
import cope.cosmos.client.events.entity.LivingUpdateEvent;
import cope.cosmos.client.events.input.UpdateMoveStateEvent;
import cope.cosmos.client.events.motion.movement.MotionEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.exploits.PacketFlightModule;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.MotionUtil;
import cope.cosmos.util.player.PlayerUtil;
import net.minecraft.init.MobEffects;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 06/08/2021
 */
public class SprintModule extends Module {
    public static SprintModule INSTANCE;

    public SprintModule() {
        super("Sprint", new String[] {"AutoSprint"}, Category.MOVEMENT, "Sprints continuously");
        INSTANCE = this;
    }

    // **************************** general ****************************

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.DIRECTIONAL)
            .setDescription("Mode for sprint");

    public static Setting<Boolean> safe = new Setting<>("Safe", true)
            .setAlias("Hunger")
            .setDescription("Stops sprinting when you don't have the required hunger");

    public static Setting<Boolean> strict = new Setting<>("Strict", false)
            .setAlias("Slowdown")
            .setDescription("Stops sprinting when sneaking and using items");

    // speed to move at
    double moveSpeed;

    @Override
    public void onUpdate() {

        // reset sprint state
        mc.player.setSprinting(false);

        // verify if the player's food level allows sprinting
        if (mc.player.getFoodStats().getFoodLevel() <= 6 && safe.getValue()) {
            return;
        }

        // verify whether or not the player can actually sprint
        if ((mc.player.isHandActive() || mc.player.isSneaking()) && strict.getValue()) {
            return;
        }

        // update player sprint state
        if (MotionUtil.isMoving()) {
            switch (mode.getValue()) {
                case DIRECTIONAL:
                case INSTANT:
                    mc.player.setSprinting(true);
                    break;
                case NORMAL:
                    mc.player.setSprinting(!mc.player.collidedHorizontally && mc.gameSettings.keyBindForward.isKeyDown());
                    break;
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    @SubscribeEvent
    public void onMotion(MotionEvent event) {

        // instant sprint
        if (mode.getValue().equals(Mode.INSTANT)) {

            // make sure the player is not in a liquid
            if (PlayerUtil.isInLiquid()) {
                return;
            }

            // make sure the player is not in a web
            if (((IEntity) mc.player).getInWeb()) {
                return;
            }

            // make sure the player can have speed applied
            if (mc.player.isOnLadder() || mc.player.capabilities.isFlying || mc.player.isElytraFlying() || mc.player.fallDistance > 2) {
                return;
            }

            // incompatibilities
            if (FlightModule.INSTANCE.isEnabled() || PacketFlightModule.INSTANCE.isEnabled() || LongJumpModule.INSTANCE.isEnabled() || SpeedModule.INSTANCE.isEnabled()) {
                return;
            }

            // cancel vanilla movement, we'll send our own movements
            event.setCanceled(true);

            // base move speed
            double baseSpeed = 0.2873;

            // scale move speed if Speed or Slowness potion effect is active
            if (mc.player.isPotionActive(MobEffects.SPEED)) {
                double amplifier = mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier();
                baseSpeed *= 1 + (0.2 * (amplifier + 1));
            }

            if (mc.player.isPotionActive(MobEffects.SLOWNESS)) {
                double amplifier = mc.player.getActivePotionEffect(MobEffects.SLOWNESS).getAmplifier();
                baseSpeed /= 1 + (0.2 * (amplifier + 1));
            }

            // instant max speed
            moveSpeed = baseSpeed;

            // sneak scale = 0.3 * non-sprint speed
            if (mc.player.isSneaking()) {
                moveSpeed = baseSpeed * 0.3;
            }

            // the current movement input values of the user
            float forward = mc.player.movementInput.moveForward;
            float strafe = mc.player.movementInput.moveStrafe;
            float yaw = mc.player.prevRotationYaw + (mc.player.rotationYaw - mc.player.prevRotationYaw) * mc.getRenderPartialTicks();

            // if we're not inputting any movements, then we shouldn't be adding any motion
            if (!MotionUtil.isMoving()) {
                event.setX(0);
                event.setZ(0);
            }

            if (forward != 0) {
                if (strafe > 0) {
                    yaw += ((forward > 0) ? -45 : 45);
                }

                else if (strafe < 0) {
                    yaw += ((forward > 0) ? 45 : -45);
                }

                strafe = 0;
                if (forward > 0) {
                    forward = 1;
                }

                else if (forward < 0) {
                    forward = -1;
                }
            }

            // our facing values, according to movement not rotations
            double cos = Math.cos(Math.toRadians(yaw));
            double sin = -Math.sin(Math.toRadians(yaw));

            // update the movements
            event.setX((forward * moveSpeed * sin) + (strafe * moveSpeed * cos));
            event.setZ((forward * moveSpeed * cos) - (strafe * moveSpeed * sin));
        }
    }

    @SubscribeEvent
    public void onUpdateMoveState(UpdateMoveStateEvent event) {

        // make sure the player is not in a liquid
        if (PlayerUtil.isInLiquid()) {
            return;
        }

        // make sure the player is not in a web
        if (((IEntity) mc.player).getInWeb()) {
            return;
        }

        // make sure the player can have speed applied
        if (mc.player.isOnLadder() || mc.player.capabilities.isFlying || mc.player.isElytraFlying() || mc.player.fallDistance > 2) {
            return;
        }

        // incompatibilities
        if (FlightModule.INSTANCE.isEnabled() || PacketFlightModule.INSTANCE.isEnabled() || LongJumpModule.INSTANCE.isEnabled() || SpeedModule.INSTANCE.isEnabled()) {
            return;
        }

        // custom sneaking
        if (mode.getValue().equals(Mode.INSTANT)) {

            // prevent sneak state from changing
            if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                mc.player.movementInput.moveForward *= (1 / 0.3F);
                mc.player.movementInput.moveStrafe *= (1 / 0.3F);
            }
        }
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingUpdateEvent event) {

        // rage sprint
        if (MotionUtil.isMoving() && (mode.getValue().equals(Mode.DIRECTIONAL) || mode.getValue().equals(Mode.INSTANT))) {

            // verify if the player's food level allows sprinting
            if (mc.player.getFoodStats().getFoodLevel() <= 6 && safe.getValue()) {
                return;
            }

            // verify whether or not the player can actually sprint
            if (mc.player.isSneaking() && strict.getValue()) {
                return;
            }

            // cancel vanilla sprint direction
            event.setCanceled(true);
        }
    }

    public enum Mode {

        /**
         * Applies StrafeGround {@link SpeedModule.Mode}
         */
        INSTANT,

        /**
         * Allows you to sprint in all directions
         */
        DIRECTIONAL,

        /**
         * Allows sprinting when moving forward
         */
        NORMAL
    }
}