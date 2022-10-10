package cope.cosmos.client.features.modules.movement;

import cope.cosmos.asm.mixins.accessor.ICPacketPlayer;
import cope.cosmos.asm.mixins.accessor.IEntity;
import cope.cosmos.asm.mixins.accessor.IEntityPlayerSP;
import cope.cosmos.client.events.motion.movement.MotionEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.exploits.PacketFlightModule;
import cope.cosmos.client.features.modules.visual.FreecamModule;
import cope.cosmos.client.features.setting.Setting;
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
        super("Speed", new String[] {"Strafe"}, Category.MOVEMENT, "Allows you to move faster", () -> StringFormatter.formatEnum(mode.getValue()));
        INSTANCE = this;
    }

    // **************************** speeds ****************************

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.STRAFE)
            .setDescription("Mode for Speed");

    public static Setting<BaseSpeed> speed = new Setting<>("Speed", BaseSpeed.NORMAL)
            .setDescription("Base speed when moving");

    public static Setting<Friction> friction = new Setting<>("Friction", Friction.CUTOFF)
            .setDescription("Friction for moving through objects");

    // **************************** anticheat ****************************

    public static Setting<Boolean> potionFactor = new Setting<>("PotionFactor", true)
            .setAlias("Potions", "SpeedFactor")
            .setDescription("Applies potions effects to speed");

    public static Setting<Boolean> strictJump = new Setting<>("StrictJump", false)
            .setVisible(() -> mode.getValue().equals(Mode.STRAFE_STRICT))
            .setDescription("Use slightly higher and therefore slower jumps to bypass better");

    public static Setting<Boolean> strictSprint = new Setting<>("StrictSprint", false)
            .setAlias("AutoSprint")
            .setVisible(() -> mode.getValue().equals(Mode.STRAFE_STRICT))
            .setDescription("Maintains sprint while moving");

    // **************************** timer ****************************

    public static Setting<Boolean> timer = new Setting<>("Timer", true)
            .setAlias("UseTimer")
            .setDescription("Uses timer to speed up strafe");

    // **************************** stages ****************************

    // strafe stage
    private int strafeStage = 4;

    // on-ground stage
    private int groundStage = 2;

    // **************************** speeds ****************************

    // the move speed for the current mode
    private double moveSpeed;
    private double distance;

    // boost speed
    private double boostSpeed;

    // speed accelerate tick
    private boolean accelerate;

    // **************************** ticks ****************************

    // strict tick clamp
    private int strictTicks;

    // ticks boosted
    private int boostTicks;

    // **************************** packets ****************************

    // packet manipulation
    private boolean offsetPackets;

    @Override
    public void onEnable() {
        super.onEnable();

        // awesome
        strafeStage = 4;
        groundStage = 2;
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // reset all vars
        resetProcess();
    }

    @Override
    public void onUpdate() {

        // our latest move speed
        distance = Math.sqrt(StrictMath.pow(mc.player.posX - mc.player.prevPosX, 2) + StrictMath.pow(mc.player.posZ - mc.player.prevPosZ, 2));
    }

    @SuppressWarnings("ConstantConditions")
    @SubscribeEvent
    public void onMotion(MotionEvent event) {

        if (friction.getValue().equals(Friction.CUTOFF)) {

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

        // incompatibilities
        if (FlightModule.INSTANCE.isEnabled() || PacketFlightModule.INSTANCE.isEnabled() || LongJumpModule.INSTANCE.isEnabled() || FreecamModule.INSTANCE.isEnabled()) {
            return;
        }

        // pause if sneaking
        if (mc.player.isSneaking()) {
            return;
        }

        // cancel vanilla movement, we'll send our own movements
        event.setCanceled(true);
        getCosmos().getTickManager().setClientTicks(1);

        // base move speed
        double baseSpeed = 0.2873;

        if (speed.getValue().equals(BaseSpeed.OLD)) {
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

        switch (mode.getValue()) {

            /*
             * OnGround, the idea behind this is that you are simulating a fake jump by modifying packets instead
             * of actually jumping (i.e. Strafe), this allows you to gain lots of Speed on NCP servers without
             * actually jumping
             */
            case ON_GROUND: {

                // only function when we are on the ground
                if (mc.player.onGround && MotionUtil.isMoving()) {

                    // fake jump by offsetting packets
                    if (groundStage == 2) {

                        // offset our y-packets to simulate a jump
                        offsetPackets = true;

                        // acceleration jump factor
                        double acceleration = 2.149;

                        // since we just jumped, we can now move faster
                        moveSpeed *= acceleration;

                        // we can start speeding
                        groundStage = 3;
                    }

                    else if (groundStage == 3) {

                        // take into account our last tick's move speed
                        double scaledMoveSpeed = 0.66 * (distance - baseSpeed);

                        // scale the move speed
                        moveSpeed = distance - scaledMoveSpeed;

                        // we need to "jump" again now
                        groundStage = 2;
                    }

                    // we will not be able to jump
                    if (mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0, 0.21, 0)).size() > 0 || mc.player.collidedVertically) {
                        groundStage = 1;
                    }
                }

                // do not allow movements slower than base speed
                moveSpeed = Math.max(moveSpeed, baseSpeed);

                // the current movement input values of the user
                float forward = mc.player.movementInput.moveForward;
                float strafe = mc.player.movementInput.moveStrafe;
                float yaw = mc.player.prevRotationYaw + (mc.player.rotationYaw - mc.player.prevRotationYaw) * mc.getRenderPartialTicks();

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

                // our facing values, according to movement not rotations
                double cos = Math.cos(Math.toRadians(yaw));
                double sin = -Math.sin(Math.toRadians(yaw));

                // update the movements
                event.setX((forward * moveSpeed * sin) + (strafe * moveSpeed * cos));
                event.setZ((forward * moveSpeed * cos) - (strafe * moveSpeed * sin));
                break;
            }

            /*
             * Incredibly similar to sprint jumping, bypasses lots of anticheats as the movement is similar
             * to sprint jumping. Max speed: ~29 kmh
             */
            case STRAFE: {

                // only attempt to modify speed if we are inputting movement
                if (MotionUtil.isMoving()) {

                    // use timer
                    if (timer.getValue()) {
                        getCosmos().getTickManager().setClientTicks(1.088F);
                    }

                    // start the motion
                    if (strafeStage == 1) {

                        // starting speed
                        moveSpeed = 1.35 * baseSpeed - 0.01;
                    }

                    // start jumping
                    else if (strafeStage == 2) {

                        // the jump height
                        double jumpSpeed = 0.3999999463558197;

                        // scale jump speed if Jump Boost potion effect is active
                        if (potionFactor.getValue()) {

                            // not really too useful for Speed like the other potion effects
                            if (mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
                                double amplifier = mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier();
                                jumpSpeed += (amplifier + 1) * 0.1;
                            }
                        }

                        // jump
                        mc.player.motionY = jumpSpeed;
                        event.setY(jumpSpeed);

                        // alternate acceleration ticks
                        double acceleration = 1.395;

                        // if can accelerate, increase speed
                        if (accelerate) {
                            acceleration = 1.6835;
                        }

                        // since we just jumped, we can now move faster
                        moveSpeed *= acceleration;
                    }

                    // start actually speeding when falling
                    else if (strafeStage == 3) {

                        // take into account our last tick's move speed
                        double scaledMoveSpeed = 0.66 * (distance - baseSpeed);

                        // scale the move speed
                        moveSpeed = distance - scaledMoveSpeed;

                        // we've just slowed down and need to alternate acceleration
                        accelerate = !accelerate;
                    }

                    else {
                        if ((mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0, mc.player.motionY, 0)).size() > 0 || mc.player.collidedVertically) && strafeStage > 0) {

                            // reset strafe stage
                            strafeStage = MotionUtil.isMoving() ? 1 : 0;
                        }

                        // collision speed
                        moveSpeed = distance - (distance / 159);
                    }

                    // do not allow movements slower than base speed
                    moveSpeed = Math.max(moveSpeed, baseSpeed);

                    // the current movement input values of the user
                    float forward = mc.player.movementInput.moveForward;
                    float strafe = mc.player.movementInput.moveStrafe;
                    float yaw = mc.player.prevRotationYaw + (mc.player.rotationYaw - mc.player.prevRotationYaw) * mc.getRenderPartialTicks();

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

                    // our facing values, according to movement not rotations
                    double cos = Math.cos(Math.toRadians(yaw));
                    double sin = -Math.sin(Math.toRadians(yaw));

                    // update the movements
                    event.setX((forward * moveSpeed * sin) + (strafe * moveSpeed * cos));
                    event.setZ((forward * moveSpeed * cos) - (strafe * moveSpeed * sin));

                    // update
                    strafeStage++;
                }

                break;
            }

            /*
             * Mode: Strafe for NCP Updated
             * Max speed: ~26 or 27 kmh
             */
            case STRAFE_STRICT: {

                // only attempt to modify speed if we are inputting movement
                if (MotionUtil.isMoving()) {

                    // use timer
                    if (timer.getValue()) {
                        getCosmos().getTickManager().setClientTicks(1.088F);
                    }

                    // start the motion
                    if (strafeStage == 1) {

                        // starting speed
                        moveSpeed = 1.35 * baseSpeed - 0.01;
                    }

                    // start jumping
                    else if (strafeStage == 2) {

                        // the jump height
                        double jumpSpeed = 0.3999999463558197;

                        // jump slightly higher (i.e. slower, this uses vanilla jump height)
                        if (strictJump.getValue()) {
                            jumpSpeed = 0.41999998688697815;
                        }

                        // scale jump speed if Jump Boost potion effect is active
                        if (potionFactor.getValue()) {

                            // not really too useful for Speed like the other potion effects
                            if (mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
                                double amplifier = mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier();
                                jumpSpeed += (amplifier + 1) * 0.1;
                            }
                        }

                        // jump
                        mc.player.motionY = jumpSpeed;
                        event.setY(jumpSpeed);

                        // acceleration jump factor
                        double acceleration = 2.149;

                        // since we just jumped, we can now move faster
                        moveSpeed *= acceleration;
                    }

                    // start actually speeding when falling
                    else if (strafeStage == 3) {

                        // take into account our last tick's move speed
                        double scaledMoveSpeed = 0.66 * (distance - baseSpeed);

                        // scale the move speed
                        moveSpeed = distance - scaledMoveSpeed;
                    }

                    else {
                        if ((mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0, mc.player.motionY, 0)).size() > 0 || mc.player.collidedVertically) && strafeStage > 0) {

                            // reset strafe stage
                            strafeStage = MotionUtil.isMoving() ? 1 : 0;
                        }

                        // collision speed
                        moveSpeed = distance - (distance / 159);
                    }

                    // do not allow movements slower than base speed
                    moveSpeed = Math.max(moveSpeed, baseSpeed);

                    // base speeds
                    double baseStrictSpeed = 0.465;
                    double baseRestrictedSpeed = 0.44;

                    // scale move speed if Speed or Slowness potion effect is active
                    if (potionFactor.getValue()) {
                        if (mc.player.isPotionActive(MobEffects.SPEED)) {
                            double amplifier = mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier();
                            baseStrictSpeed *= 1 + (0.2 * (amplifier + 1));
                            baseRestrictedSpeed *= 1 + (0.2 * (amplifier + 1));
                        }

                        if (mc.player.isPotionActive(MobEffects.SLOWNESS)) {
                            double amplifier = mc.player.getActivePotionEffect(MobEffects.SLOWNESS).getAmplifier();
                            baseStrictSpeed /= 1 + (0.2 * (amplifier + 1));
                            baseRestrictedSpeed /= 1 + (0.2 * (amplifier + 1));
                        }
                    }

                    // clamp the value based on the number of ticks passed
                    moveSpeed = Math.min(moveSpeed, strictTicks > 25 ? baseStrictSpeed : baseRestrictedSpeed);

                    // update & reset our tick count
                    strictTicks++;

                    // reset strict ticks every 50 ticks
                    if (strictTicks > 50) {
                        strictTicks = 0;
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
                    double cos = Math.cos(Math.toRadians(yaw));
                    double sin = -Math.sin(Math.toRadians(yaw));

                    // update the movements
                    event.setX((forward * moveSpeed * sin) + (strafe * moveSpeed * cos));
                    event.setZ((forward * moveSpeed * cos) - (strafe * moveSpeed * sin));

                    // update
                    strafeStage++;
                }

                break;
            }

            /*
             * Maintains speed at 22.4 kmh on ground
             * Similar to Sprint
             */
            case STRAFE_GROUND: {

                // instant max speed
                moveSpeed = baseSpeed;

                // walking speed = 0.7692307692 * sprint speed
                if (!mc.player.isSprinting()) {
                    moveSpeed *= 0.7692307692;
                }

                // sneak scale = 0.3 * sprint speed
                else if (mc.player.isSneaking()) {
                    moveSpeed *= 0.3;
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
                break;
            }

            /*
             * Similar to Mode: Strafe with a lower jump height in order to reach higher speeds
             * Max speed: ~31 kmh
             */
            case STRAFE_LOW:
                break;
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
                ((ICPacketPlayer) event.getPacket()).setY(((CPacketPlayer) event.getPacket()).getY(0) + (mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0, 0.21, 0)).size() > 0 ? 2 : 4));
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
            double boostMotionX = StrictMath.pow(((SPacketExplosion) event.getPacket()).getMotionX() / 8000F, 2);
            double boostMotionZ = StrictMath.pow(((SPacketExplosion) event.getPacket()).getMotionX() / 8000F, 2);

            // boost our speed
            boostSpeed = Math.sqrt(boostMotionX + boostMotionZ);

            // start our timer
            boostTicks = 0;
        }

        // boost our speed when taking knockback damage
        if (event.getPacket() instanceof SPacketEntityVelocity) {

            // check if velocity is applied to player
            if (((SPacketEntityVelocity) event.getPacket()).getEntityID() == mc.player.getEntityId()) {

                // velocity from knockback
                double boostMotionX = StrictMath.pow(((SPacketEntityVelocity) event.getPacket()).getMotionX() / 8000F, 2);
                double boostMotionZ = StrictMath.pow(((SPacketEntityVelocity) event.getPacket()).getMotionX() / 8000F, 2);

                // boost our speed
                boostSpeed = Math.sqrt(boostMotionX + boostMotionZ);

                // start our timer
                boostTicks = 0;
            }
        }
    }

    /**
     * Resets the Speed process and sets all values back to defaults
     */
    public void resetProcess() {
        strafeStage = 4;
        groundStage = 2;
        moveSpeed = 0;
        distance = 0;
        boostSpeed = 0;
        strictTicks = 0;
        boostTicks = 0;
        accelerate = false;
        offsetPackets = false;
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
         * Base speed for old NCP
         */
        OLD
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
        CUTOFF
    }
}
