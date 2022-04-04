package cope.cosmos.client.features.modules.movement;

import cope.cosmos.asm.mixins.accessor.ICPacketPlayer;
import cope.cosmos.asm.mixins.accessor.IEntity;
import cope.cosmos.asm.mixins.accessor.IEntityPlayerSP;
import cope.cosmos.client.events.motion.movement.MotionEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.math.MathUtil;
import cope.cosmos.util.player.MotionUtil;
import cope.cosmos.util.player.PlayerUtil;
import cope.cosmos.util.string.StringFormatter;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips, aesthetical
 * @since 11/27/2021
 */
public class SpeedModule extends Module {
    public static SpeedModule INSTANCE;

    public SpeedModule() {
        super("Speed", Category.MOVEMENT, "Allows you to move faster", () -> StringFormatter.formatEnum(mode.getValue()));
        INSTANCE = this;
    }

    // **************************** speeds ****************************

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.STRAFE)
            .setDescription("Mode for Speed");

    public static Setting<BaseSpeed> speed = new Setting<>("Speed", BaseSpeed.NORMAL)
            .setDescription("Base speed when moving");

    public static Setting<Friction> friction = new Setting<>("Friction", Friction.FAST)
            .setDescription("Friction for moving through objects");

    // **************************** anticheat ****************************

    public static Setting<Boolean> velocityFactor = new Setting<>("VelocityFactor", false)
            .setDescription("Boosts speed when taking knockback");

    public static Setting<Boolean> potionFactor = new Setting<>("PotionFactor", true)
            .setDescription("Applies potions effects to speed");

    public static Setting<Boolean> retain = new Setting<>("Retain", false)
            .setDescription("Quickly restarts strafe after collision");

    public static Setting<Boolean> airStrafe = new Setting<>("AirStrafe", false)
            .setDescription("Allows you to boost your speed and control movement in the air");

    public static Setting<Boolean> strictJump = new Setting<>("StrictJump", false)
            .setDescription("Use slightly higher and therefore slower jumps to bypass better");

    public static Setting<Boolean> strictCollision = new Setting<>("StrictCollision", false)
            .setDescription("Collision reset");

    public static Setting<Boolean> strictSprint = new Setting<>("StrictSprint", false)
            .setDescription("Maintains sprint while moving");

    // **************************** timer ****************************

    public static Setting<Boolean> timer = new Setting<>("Timer", true)
            .setDescription("Uses timer to speed up strafe");

    public static Setting<Double> timerTick = new Setting<>("Ticks", 1.0, 1.2, 2.0, 1)
            .setDescription("Timer speed")
            .setVisible(() -> timer.getValue());

    // **************************** stages ****************************

    // strafe stage
    private StrafeStage strafeStage = StrafeStage.SPEED;

    // on-ground stage
    private GroundStage groundStage = GroundStage.CHECK_SPACE;

    // **************************** speeds ****************************

    // the move speed for the current mode
    private double moveSpeed;
    private double latestMoveSpeed;

    // boost speed
    private double boostSpeed;

    // **************************** ticks ****************************

    // strict tick clamp
    private int strictTicks;

    // timer tick
    private int timerTicks;

    // ticks boosted
    private int boostTicks;

    // **************************** packets ****************************

    // packet manipulation
    private boolean offsetPackets;

    @Override
    public void onDisable() {
        super.onDisable();
        resetProcess();
    }

    @Override
    public void onUpdate() {
        // our latest move speed
        latestMoveSpeed = Math.sqrt(StrictMath.pow(mc.player.posX - mc.player.prevPosX, 2) + StrictMath.pow(mc.player.posZ - mc.player.prevPosZ, 2));
    }

    @SuppressWarnings("ConstantConditions")
    @SubscribeEvent
    public void onMotion(MotionEvent event) {
        if (friction.getValue().equals(Friction.STRICT)) {
            // make sure the player is not in a liquid
            if (PlayerUtil.isInLiquid()) {
                resetProcess();
                return;
            }

            // make sure the player is not in a web
            if (((IEntity) mc.player).getInWeb()) {
                resetProcess();
                return;
            }
        }

        // make sure the player can have speed applied
        if (mc.player.isOnLadder() || mc.player.capabilities.isFlying || mc.player.isElytraFlying() || mc.player.fallDistance > 2) {
            resetProcess();
            return;
        }

        // cancel vanilla movement, we'll send our own movements
        event.setCanceled(true);

        // base move speed
        double baseSpeed = 0.2873;

        if (speed.getValue().equals(BaseSpeed.VANILLA)) {
            baseSpeed = 0.272;
        }

        // scale move speed if Speed or Slowness potion effect is active
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

        // start sprinting
        if (strictSprint.getValue() && (!mc.player.isSprinting() || !((IEntityPlayerSP) mc.player).getServerSprintState())) {
            if (mc.getConnection() != null) {
                mc.getConnection().getNetworkManager().sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
            }
        }

        /*
         * OnGround, the idea behind this is that you are simulating a fake jump by modifying packets instead
         * of actually jumping (i.e. Strafe), this allows you to gain lots of Speed on NCP servers without
         * actually jumping
         */
        if (mode.getValue().equals(Mode.ON_GROUND)) {
            if (mc.player.onGround && MotionUtil.isMoving()) {
                // fake jump by offsetting packets
                if (groundStage.equals(GroundStage.FAKE_JUMP)) {
                    // offset our y-packets to simulate a jump
                    offsetPackets = true;

                    // acceleration jump factor
                    double acceleration = 2.149;

                    // since we just jumped, we can now move faster
                    moveSpeed *= acceleration;

                    // we can start speeding
                    groundStage = GroundStage.SPEED;
                }

                else if (groundStage.equals(GroundStage.SPEED)) {
                    // take into account our last tick's move speed
                    double scaledMoveSpeed = 0.66 * (latestMoveSpeed - baseSpeed);

                    // scale the move speed
                    moveSpeed = latestMoveSpeed - scaledMoveSpeed;

                    // we need to "jump" again now
                    groundStage = GroundStage.FAKE_JUMP;
                }

                // we will not be able to jump
                if (mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0, 0.21, 0)).size() > 0 || mc.player.collidedVertically) {
                    groundStage = GroundStage.FAKE_JUMP;

                    double collisionSpeed = latestMoveSpeed - (latestMoveSpeed / 159);

                    // reset to base speed
                    if (strictCollision.getValue()) {
                        collisionSpeed = baseSpeed;
                        latestMoveSpeed = 0;
                    }

                    // reset our move speed
                    moveSpeed = collisionSpeed;
                }
            }

            // allow speed boost in air
            if (airStrafe.getValue()) {
                mc.player.jumpMovementFactor = 0.029F;
            }

            // the current movement input values of the user
            float forward = mc.player.movementInput.moveForward;
            float strafe = mc.player.movementInput.moveStrafe;
            float yaw = mc.player.rotationYaw;

            // if we're not inputting any movements, then we shouldn't be adding any motion
            if (MotionUtil.isMoving()) {
                event.setX(0);
                event.setZ(0);
            }

            // our facing values, according to movement not rotations
            double cos = Math.cos(Math.toRadians(yaw + 90));
            double sin = Math.sin(Math.toRadians(yaw + 90));

            // update the movements
            event.setX((forward * moveSpeed * cos) + (strafe * moveSpeed * -sin));
            event.setZ((forward * moveSpeed * -sin) - (strafe * moveSpeed * cos));
        }

        else {
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

                    // slight boost
                    event.setX(event.getX() * 1.02);
                    event.setZ(event.getZ() * 1.02);
                }
            }

            else {
                // compatibility with Timer module
                getCosmos().getTickManager().setClientTicks(1);
            }

            // we are ready to start strafing
            if (MotionUtil.isMoving()) {
                if (mc.player.onGround) {
                    strafeStage = StrafeStage.START;
                }

                if (mode.getValue().equals(Mode.STRAFE) || mode.getValue().equals(Mode.STRAFE_LOW)) {
                    // check if we are inside a burrow
                    if (mc.world.getBlockState(mc.player.getPosition()).getMaterial().isReplaceable()) {
                        // boost speed
                        moveSpeed = baseSpeed * 1.38;
                    }
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
                    double jumpSpeed = 0.3999999463558197;

                    // jump slightly higher (i.e. slower, this uses vanilla jump height)
                    if (strictJump.getValue()) {
                        jumpSpeed = 0.42;
                    }

                    if (speed.getValue().equals(BaseSpeed.VANILLA)) {
                        if (mode.getValue().equals(Mode.STRAFE_LOW)) {
                            jumpSpeed = 0.31;
                        }

                        else {
                            jumpSpeed = 0.42;
                        }
                    }

                    else if (mode.getValue().equals(Mode.STRAFE_LOW)) {
                        jumpSpeed = 0.27;
                    }

                    // scale jump speed if Jump Boost potion effect is active
                    if (potionFactor.getValue() && mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
                        jumpSpeed += (mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1;
                    }

                    // jump
                    mc.player.motionY = jumpSpeed;
                    event.setY(jumpSpeed);

                    // acceleration jump factor
                    double acceleration = 2.149;

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

                        // restart, disregard slowdown
                        if (retain.getValue()) {
                            strafeStage = StrafeStage.START;
                        }
                    }

                    double collisionSpeed = latestMoveSpeed - (latestMoveSpeed / 159);

                    // reset to base speed
                    if (strictCollision.getValue()) {
                        collisionSpeed = baseSpeed;
                        latestMoveSpeed = 0;
                    }

                    // reset our move speed
                    moveSpeed = collisionSpeed;
                }
            }

            // reset momentum
            else if (mode.getValue().equals(Mode.STRAFE_STRICT)) {
                if (mc.player.onGround) {
                    strafeStage = StrafeStage.START;
                }

                // check if we are inside a burrow
                if (mc.world.getBlockState(mc.player.getPosition()).getMaterial().isReplaceable()) {
                    // final move speed
                    moveSpeed = baseSpeed * 1.38;
                }
            }

            // the final move speed, finds the higher speed
            moveSpeed = Math.max(moveSpeed, baseSpeed);

            // boost the move speed for 10 ticks
            if (velocityFactor.getValue() && boostTicks <= 10) {
                moveSpeed = Math.max(moveSpeed, boostSpeed);
            }

            if (mode.getValue().equals(Mode.STRAFE_STRICT)) {
                // clamp the value based on the number of ticks passed
                moveSpeed = Math.min(moveSpeed, strictTicks > 25 ? 0.465 : 0.44);
            }

            // update & reset our tick count
            strictTicks++;

            // update boost ticks
            if (moveSpeed >= boostSpeed && boostSpeed > 0) {
                boostTicks++;
            }

            // reset strict ticks every 50 ticks
            if (strictTicks > 50) {
                strictTicks = 0;
            }

            // bypass friction check
            if (friction.getValue().equals(Friction.FACTOR)) {
                float friction = 1;

                if (mc.player.isInWater()) {
                    friction = 0.89F;
                }

                else if (mc.player.isInLava()) {
                    friction = 0.535F;
                }

                moveSpeed *= friction;
            }

            // the current movement input values of the user
            float forward = mc.player.movementInput.moveForward;
            float strafe = mc.player.movementInput.moveStrafe;
            float yaw = mc.player.rotationYaw;

            // find the rotations and inputs based on our current movements
            if (mode.getValue().equals(Mode.STRAFE_STRICT)) {

                // if we're not inputting any movements, then we shouldn't be adding any motion
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
            } else {
                // if we're not inputting any movements, then we shouldn't be adding any motion
                if (!MotionUtil.isMoving()) {
                    event.setX(0);
                    event.setZ(0);
                }

                else if (forward != 0) {
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
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketEntityAction) {
            // slowdown movement
            if (((CPacketEntityAction) event.getPacket()).getAction().equals(CPacketEntityAction.Action.STOP_SPRINTING) || ((CPacketEntityAction) event.getPacket()).getAction().equals(CPacketEntityAction.Action.START_SNEAKING)) {

                // keep sprint
                if (strictSprint.getValue()) {
                    event.setCanceled(true);
                }
            }
        }

        if (event.getPacket() instanceof CPacketPlayer) {
            if (((ICPacketPlayer) event.getPacket()).isMoving() && offsetPackets) {
                // offset packets
                ((ICPacketPlayer) event.getPacket()).setY(((CPacketPlayer) event.getPacket()).getY(0) + 4);
                offsetPackets = false;
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {
        // reset our process on a rubberband
        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            resetProcess();
        }

        // boost our speed when taking explosion damage
        if (event.getPacket() instanceof SPacketExplosion) {

            // velocity from explosion
            double boostMotionX = StrictMath.pow(((SPacketExplosion) event.getPacket()).getMotionX(), 2);
            double boostMotionZ = StrictMath.pow(((SPacketExplosion) event.getPacket()).getMotionX(), 2);

            // boost our speed
            boostSpeed = Math.sqrt(boostMotionX + boostMotionZ);

            // start our timer
            boostTicks = 0;
        }

        // boost our speed when taking knockback damage
        if (event.getPacket() instanceof SPacketEntityVelocity) {

            // velocity from knockback
            double boostMotionX = StrictMath.pow(((SPacketEntityVelocity) event.getPacket()).getMotionX(), 2);
            double boostMotionZ = StrictMath.pow(((SPacketEntityVelocity) event.getPacket()).getMotionX(), 2);

            // boost our speed
            boostSpeed = Math.sqrt(boostMotionX + boostMotionZ);

            // start our timer
            boostTicks = 0;
        }
    }

    /**
     * Resets the Speed process and sets all values back to defaults
     */
    public void resetProcess() {

    }

    public enum Mode {

        /**
         * Speed that automatically jumps to simulate BHops
         */
        STRAFE,

        /**
         * Strafe but with a slower initial and max speed, along with {@link NoSlowModule} NoSlow built into the Speed
         */
        STRAFE_STRICT,

        /**
         * Strafe with a lower jump height
         */
        STRAFE_LOW,

        /**
         * Speeds your movement while on the ground
         */
        STRAFE_GROUND,

        /**
         * Speeds your movement while on the ground, spoofs jump state
         */
        ON_GROUND
    }

    public enum BaseSpeed {

        /**
         * Base speed for NCP
         */
        NORMAL,

        /**
         * Base speed for Vanilla
         */
        VANILLA
    }

    public enum Friction {

        /**
         * Factors in material friction but otherwise retains all functionality
         */
        FACTOR,

        /**
         * Ignores friction
         */
        FAST,

        /**
         * Stop all speed when experiencing friction
         */
        STRICT
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

    public enum GroundStage {

        /**
         * Stage when the player is speeding up
         */
        SPEED,

        /**
         * Stage when the player is fake jumping
         */
        FAKE_JUMP,

        /**
         * Stage when the player has collided into a block or entity
         */
        CHECK_SPACE
    }
}