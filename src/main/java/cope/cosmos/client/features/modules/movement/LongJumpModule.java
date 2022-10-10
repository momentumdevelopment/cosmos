package cope.cosmos.client.features.modules.movement;

import cope.cosmos.asm.mixins.accessor.IEntity;
import cope.cosmos.asm.mixins.accessor.INetHandlerPlayClient;
import cope.cosmos.client.events.entity.player.UpdateWalkingPlayerEvent;
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
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author aesthetical, Doogie13, linustouchtips, Direkt client
 * @since 06/21/2022
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
            .setDescription("The boost speed")
            .setVisible(() -> mode.getValue().equals(Mode.NORMAL));

    public static Setting<Boolean> potionFactor = new Setting<>("PotionFactor", true)
            .setDescription("If to factor in potion effects for move speed")
            .setVisible(() -> mode.getValue().equals(Mode.NORMAL));

    // speed
    private double moveSpeed;
    private double distance;

    // cowabunga ticks
    private int airTicks;
    private int groundTicks;

    // stage
    private LongJumpStage stage = LongJumpStage.START;
    
    // cowabunga speed factor
    private final double[] speedFactor = new double[] {
            0.420606,
            0.417924,
            0.415258,
            0.412609,
            0.409977,
            0.407361,
            0.404761,
            0.402178,
            0.399611,
            0.39706,
            0.394525,
            0.392,
            0.3894,
            0.38644,
            0.383655,
            0.381105,
            0.37867,
            0.37625,
            0.37384,
            0.37145,
            0.369,
            0.3666,
            0.3642,
            0.3618,
            0.35945,
            0.357,
            0.354,
            0.351,
            0.348,
            0.345,
            0.342,
            0.339,
            0.336,
            0.333,
            0.33,
            0.327,
            0.324,
            0.321,
            0.318,
            0.315,
            0.312,
            0.309,
            0.307,
            0.305,
            0.303,
            0.3,
            0.297,
            0.295,
            0.293,
            0.291,
            0.289,
            0.287,
            0.285,
            0.283,
            0.281,
            0.279,
            0.277,
            0.275,
            0.273,
            0.271,
            0.269,
            0.267,
            0.265,
            0.263,
            0.261,
            0.259,
            0.257,
            0.255,
            0.253,
            0.251,
            0.249,
            0.247,
            0.245,
            0.243,
            0.241,
            0.239,
            0.237
    };

    @Override
    public void onDisable() {
        super.onDisable();

        // reset
        moveSpeed = 0;
        distance = 0;
        groundTicks = 0;
        stage = LongJumpStage.START;
    }

    @Override
    public void onUpdate() {

        // our latest move speed
        distance = Math.sqrt(StrictMath.pow(mc.player.posX - mc.player.prevPosX, 2) + StrictMath.pow(mc.player.posZ - mc.player.prevPosZ, 2));
    }

    @SuppressWarnings("ConstantConditions")
    @SubscribeEvent
    public void onMotion(MotionEvent event) {

        // incompatibilities
        if (PacketFlightModule.INSTANCE.isEnabled() || FlightModule.INSTANCE.isEnabled() || FreecamModule.INSTANCE.isEnabled()) {
            return;
        }

        // make sure the player is not in a liquid
        if (PlayerUtil.isInLiquid() || JesusModule.INSTANCE.isStandingOnLiquid()) {
            return;
        }

        // make sure the player is not in a web
        if (((IEntity) mc.player).getInWeb()) {
            return;
        }

        // strafe boost
        if (mode.getValue().equals(Mode.NORMAL)) {

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

                moveSpeed = distance - distance / 159.0;
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
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {

        // incompatibilities
        if (PacketFlightModule.INSTANCE.isEnabled() || FlightModule.INSTANCE.isEnabled() || FreecamModule.INSTANCE.isEnabled()) {
            return;
        }

        // make sure the player is not in a liquid
        if (PlayerUtil.isInLiquid() || JesusModule.INSTANCE.isStandingOnLiquid()) {
            return;
        }

        // make sure the player is not in a web
        if (((IEntity) mc.player).getInWeb()) {
            return;
        }

        // Direkt client longjump
        if (mode.getValue().equals(Mode.COWABUNGA)) {

            // reset move distance
            if (mc.player.onGround) {
                distance = 0;
            }

            // check if the player is moving
            if (MotionUtil.isMoving()) {

                // rotation
                float yaw = mc.player.rotationYaw + (mc.player.moveForward < 0 ? 180 : 0) + (mc.player.moveStrafing > 0 ? -90 * (mc.player.moveForward < 0 ? -0.5F : (mc.player.moveForward > 0 ? 0.5F : 1)) : 0) - (mc.player.moveStrafing < 0 ? -90 * (mc.player.moveForward < 0 ? -0.5F : (mc.player.moveForward > 0 ? 0.5F : 1)) : 0);

                // direction based on rotations
                double yawScaled = (yaw + 90) * 0.017453292;
                double cos = Math.cos(yawScaled);
                double sin = Math.sin(yawScaled);

                if (!mc.player.collidedVertically) {

                    // update ticks we've been in the air
                    airTicks++;

                    // ??? wtf is this for
                    if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(0, 2.147483647E9, 0, false));
                    }

                    // in air, so reset
                    groundTicks = 0;

                    // vertical motion
                    if (!mc.player.collidedVertically) {

                        // ok real talk idk how the fuck Direkt devs got these values, but they work
                        if (mc.player.motionY == -0.07190068807140403) {
                            mc.player.motionY *= 0.3499999940395355;
                        }

                        else if (mc.player.motionY == -0.10306193759436909) {
                            mc.player.motionY *= 0.550000011920929;
                        }

                        else if (mc.player.motionY == -0.13395038817442878) {
                            mc.player.motionY *= 0.6700000166893005;
                        }

                        else if (mc.player.motionY == -0.16635183030382) {
                            mc.player.motionY *= 0.6899999976158142;
                        }

                        else if (mc.player.motionY == -0.19088711097794803) {
                            mc.player.motionY *= 0.7099999785423279;
                        }

                        else if (mc.player.motionY == -0.21121925191528862) {
                            mc.player.motionY *= 0.20000000298023224;
                        }

                        else if (mc.player.motionY == -0.11979897632390576) {
                            mc.player.motionY *= 0.9300000071525574;
                        }

                        else if (mc.player.motionY == -0.18758479151225355) {
                            mc.player.motionY *= 0.7200000286102295;
                        }

                        else if (mc.player.motionY == -0.21075983825251726) {
                            mc.player.motionY *= 0.7599999904632568;
                        }

                        if (mc.player.motionY < -0.2 && mc.player.motionY > -0.24) {
                            mc.player.motionY *= 0.7;
                        }

                        if (mc.player.motionY < -0.25 && mc.player.motionY > -0.32) {
                            mc.player.motionY *= 0.8;
                        }

                        if (mc.player.motionY < -0.35 && mc.player.motionY > -0.8) {
                            mc.player.motionY *= 0.98;
                        }

                        if (mc.player.motionY < -0.8 && mc.player.motionY > -1.6) {
                            mc.player.motionY *= 0.99;
                        }
                    }

                    // slowdown timer, helps bypass
                    getCosmos().getTickManager().setClientTicks(0.8F);

                    // attempt jump
                    if (mc.gameSettings.keyBindForward.isKeyDown()) {
                        try {

                            // update horizontal motion
                            mc.player.motionX = cos * speedFactor[airTicks - 1] * 3;
                            mc.player.motionZ = sin * speedFactor[airTicks - 1] * 3;
                        } catch (ArrayIndexOutOfBoundsException ignored) {

                        }
                    }

                    // no movement
                    else {
                        mc.player.motionX = 0;
                        mc.player.motionZ = 0;
                    }
                }

                else {

                    // reset timer
                    getCosmos().getTickManager().setClientTicks(1);

                    // update ticks
                    airTicks = 0;
                    groundTicks++;

                    // drag
                    mc.player.motionX /= 13;
                    mc.player.motionZ /= 13;

                    // ?? not sure what this does
                    if (groundTicks == 1) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, mc.player.onGround));
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + 0.0624, mc.player.posY, mc.player.posZ, mc.player.onGround));
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.419, mc.player.posZ, mc.player.onGround));
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + 0.0624, mc.player.posY, mc.player.posZ, mc.player.onGround));
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.419, mc.player.posZ, mc.player.onGround));
                    }

                    // reset
                    else if (groundTicks > 2) {
                        groundTicks = 0;
                        mc.player.motionX = cos * 0.3;
                        mc.player.motionY = 0.42399999499320984;
                        mc.player.motionZ = sin * 0.3;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {

        if (nullCheck()) {

            // if the client is not done loading the surrounding terrain, DO NOT CANCEL MOVEMENT PACKETS!!!!
            if (!((INetHandlerPlayClient) mc.player.connection).isDoneLoadingTerrain()) {
                return;
            }

            // disable on rubberband or teleport
            if (event.getPacket() instanceof SPacketPlayerPosLook) {
                disable(true);
            }
        }
    }

    public enum Mode {

        /**
         * Preset jump motion (Direkt Longjump)
         */
        COWABUNGA,

        /**
         * Strafe boost long jump
         */
        NORMAL
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
}