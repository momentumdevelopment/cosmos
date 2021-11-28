package cope.cosmos.client.features.modules.movement;

import cope.cosmos.asm.mixins.accessor.IEntity;
import cope.cosmos.client.events.MotionEvent;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.MotionUtil;
import cope.cosmos.util.player.PlayerUtil;
import cope.cosmos.util.system.MathUtil;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@SuppressWarnings("unused")
public class Speed extends Module {
    public static Speed INSTANCE;

    public Speed() {
        super("Speed", Category.MOVEMENT, "Allows you to move faster", () -> Setting.formatEnum(mode.getValue()));
        INSTANCE = this;
    }

    public static Setting<Mode> mode = new Setting<>("Mode", "Mode for Speed", Mode.STRAFE);

    public static Setting<Boolean> timer = new Setting<>("Timer", "Uses timer to speed up strafe", true);
    public static Setting<Double> timerTick = new Setting<>("Ticks", "Timer speed", 1.0, 1.2, 2.0, 1).setParent(timer);

    public static Setting<Boolean> accelerate = new Setting<>("Accelerate", "Accelerates speed after jumping", false);
    public static Setting<Boolean> boost = new Setting<>("Boost", "Boosts speed when taking knockback", false);
    public static Setting<Boolean> liquid = new Setting<>("Liquid", "Allows speed to function in liquids", false);
    public static Setting<Boolean> webs = new Setting<>("Web", "Allows speed to function in webs", false);

    private StrafeStage strafeStage = StrafeStage.SPEED;

    // the move speed for the current mode
    private double moveSpeed;
    private double latestMoveSpeed;
    private double boostSpeed;

    private int strictTicks;
    private int timerTicks;
    private int boostTicks;

    @Override
    public void onDisable() {
        super.onDisable();
        resetProcess();
    }

    @Override
    public void onUpdate() {
        // our latest move speed
        latestMoveSpeed = Math.sqrt(Math.pow(mc.player.posX - mc.player.prevPosX, 2) + Math.pow(mc.player.posZ - mc.player.prevPosZ, 2));
    }

    @SubscribeEvent
    public void onMotion(MotionEvent event) {
        // make sure the player is not in a liquid
        if (PlayerUtil.isInLiquid() && !liquid.getValue()) {
            resetProcess();
            return;
        }

        // make sure the player is not in a web
        if (((IEntity) mc.player).getInWeb() && !webs.getValue()) {
            resetProcess();
            return;
        }

        // make sure the player can have speed applied
        if (mc.player.isOnLadder() || mc.player.capabilities.isFlying || mc.player.isElytraFlying()) {
            resetProcess();
            return;
        }

        // cancel vanilla movement, we'll send our own movements
        event.setCanceled(true);

        // timer
        if (timer.getValue()) {
            // update the timer ticks
            timerTicks++;

            // reset the timer every 5 ticks
            if (timerTicks >= 5) {
                getCosmos().getTickManager().setClientTicks(1);
                timerTicks = 0;
            }

            // set the timer if the player is moving
            else if (MotionUtil.isMoving()) {
                getCosmos().getTickManager().setClientTicks(timerTick.getValue().floatValue());
                event.setX(event.getX() * 1.02);
                event.setZ(event.getZ() * 1.02);
            }
        }

        // base move speed
        double baseSpeed = 0.2873;

        // scale move speed if Speed potion effect is active
        if (mc.player.isPotionActive(MobEffects.SPEED)) {
            double amplifier = mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier();
            baseSpeed *= 1 + (0.2 * (amplifier + 1));
        }

        // we are ready to start strafing
        if (MotionUtil.isMoving()) {
            if (mc.player.onGround) {
                strafeStage = StrafeStage.START;
            }

            if (mode.getValue().equals(Mode.STRAFE)) {
                // boost speed
                moveSpeed = (baseSpeed * 1.38) - 0.01;
            }
        }

        // we are falling
        if (mode.getValue().equals(Mode.STRAFE_STRICT)) {
            // check whether or not we are falling
            double yDifference = mc.player.posY - Math.floor(mc.player.posY);

            if (MathUtil.roundDouble(yDifference, 3) == MathUtil.roundDouble(0.138, 3)) {
                strafeStage = StrafeStage.FALL;

                // falling motion
                mc.player.motionY -= 0.08;

                // our pos should be slightly lower
                event.setY(event.getY() - 0.09316090325960147);
                mc.player.posY -= 0.09316090325960147;
            }
        }

        if (!strafeStage.equals(StrafeStage.COLLISION) || !MotionUtil.isMoving()) {
            // start jumping
            if (strafeStage.equals(StrafeStage.START)) {
                strafeStage = StrafeStage.JUMP;

                // the jump height
                double jumpSpeed = 0.399399995803833;

                if (mode.getValue().equals(Mode.STRAFE_LOW)) {
                    jumpSpeed = 0.27;
                }

                // scale jump speed if Jump Boost potion effect is active
                if (mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
                    jumpSpeed += (mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1;
                }

                // jump
                mc.player.motionY = jumpSpeed;
                event.setY(jumpSpeed);

                // acceleration jump factor
                double acceleration = 2.149;

                // acceleration due to jump
                if (accelerate.getValue()) {
                    switch (mode.getValue()) {
                        case STRAFE:
                        case STRAFE_LOW:
                            acceleration = Math.min(2.14 * baseSpeed, 2.547);
                            break;
                        case STRAFE_STRICT:
                            acceleration = Math.min(2.14 * baseSpeed, Math.min(baseSpeed * 1.78, latestMoveSpeed * 1.78));
                            break;
                    }
                }

                // since we just jumped, we can now move faster
                moveSpeed *= acceleration;
            }

            // final stage, we can now start speeding
            else if (strafeStage.equals(StrafeStage.JUMP)) {
                strafeStage = StrafeStage.SPEED;

                // take into account our last tick's move speed
                double scaledMoveSpeed = 0.66 * (latestMoveSpeed - baseSpeed);

                // scale the move speed
                moveSpeed = latestMoveSpeed - scaledMoveSpeed;
            }

            else {
                // if we collided then reset our stage
                if (mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0, mc.player.motionY, 0)).size() > 0 || mc.player.collidedVertically) {
                    strafeStage = StrafeStage.COLLISION;
                }

                // reset our move speed
                moveSpeed = latestMoveSpeed - (latestMoveSpeed / 159);
            }
        }

        // reset momentum
        else if (mode.getValue().equals(Mode.STRAFE_STRICT)) {
            if (mc.player.onGround) {
                strafeStage = StrafeStage.START;
            }

            // final move speed
            moveSpeed = (baseSpeed * 1.38) - 0.01;
        }

        // the final move speed, finds the higher speed
        moveSpeed = Math.max(moveSpeed, baseSpeed);

        if (mode.getValue().equals(Mode.STRAFE)) {
            moveSpeed = Math.min(moveSpeed, mc.player.isPotionActive(MobEffects.SPEED) ? 0.718 : 0.547);
        }

        if (boost.getValue() && boostTicks <= 5) {
            moveSpeed = Math.max(moveSpeed, boostSpeed);
        }

        if (mode.getValue().equals(Mode.STRAFE_STRICT)) {
            // clamp the value based on the number of ticks passed
            moveSpeed = Math.min(moveSpeed, strictTicks > 25 ? 0.465 : 0.44);
        }

        // update & reset our tick count
        strictTicks++;

        if (moveSpeed >= boostSpeed && boostSpeed > 0) {
            boostTicks++;
        }

        if (strictTicks > 50) {
            strictTicks = 0;
        }

        // the current movement input values of the user
        float forward = mc.player.movementInput.moveForward;
        float strafe = mc.player.movementInput.moveStrafe;
        float yaw = mc.player.rotationYaw;

        // if we're not inputting any movements, then we shouldn't be adding any motion
        if (!MotionUtil.isMoving()) {
            event.setX(0.0);
            event.setZ(0.0);
        }

        // find the rotations and inputs based on our current movements
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
            event.setX(0.0);
            event.setZ(0.0);
        }
    }

    @SubscribeEvent
    public void onPacketRecieve(PacketEvent.PacketReceiveEvent event) {
        // reset our process on a rubberband
        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            resetProcess();
        }

        // boost our speed when taking knockback damage
        if (event.getPacket() instanceof SPacketExplosion) {
            boostSpeed = Math.sqrt(Math.pow(((SPacketExplosion) event.getPacket()).getMotionX(), 2) + Math.pow(((SPacketExplosion) event.getPacket()).getMotionZ(), 2));
            boostTicks = 0;
        }
    }

    /**
     * Resets the Speed process and sets all values back to defaults
     */
    public void resetProcess() {
        strafeStage = StrafeStage.COLLISION;
        moveSpeed = 0;
        latestMoveSpeed = 0;
        boostSpeed = 0;
        strictTicks = 0;
        timerTicks = 0;
        boostTicks = 0;
    }

    public enum Mode {
        /**
         * Speed that automatically jumps to simulate BHops
         */
        STRAFE,

        /**
         * Strafe but with a slower initial and max speed, along with {@link NoSlow} NoSlow built into the Speed
         */
        STRAFE_STRICT,

        /**
         * Strafe with a lower jump height
         */
        STRAFE_LOW
    }

    public enum StrafeStage {
        /**
         * Stage when the player has collided into a block or entity
         */
        COLLISION,

        /**
         * Stage when the player is on the ground and ready to jump
         */
        START,

        /**
         * Stage when the player is jumping
         */
        JUMP,

        /**
         * Stage when the player is falling to the ground
         */
        FALL,

        /**
         * Stage when the player is speeding up
         */
        SPEED
    }
}
