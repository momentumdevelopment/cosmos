package cope.cosmos.client.features.modules.movement;

import cope.cosmos.asm.mixins.accessor.ICPacketPlayer;
import cope.cosmos.asm.mixins.accessor.INetworkManager;
import cope.cosmos.client.events.motion.movement.MotionEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.visual.FreecamModule;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.MotionUtil;
import cope.cosmos.util.string.StringFormatter;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 04/08/2022
 */
public class FlightModule extends Module {
    public static FlightModule INSTANCE;

    public FlightModule() {
        super("Flight", new String[] {"Fly"}, Category.MOVEMENT, "Allows player to fly", () -> StringFormatter.formatEnum(mode.getValue()));
        INSTANCE = this;
    }

    // **************************** anticheat ****************************

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.CREATIVE)
            .setDescription("Mode for flight");

    public static Setting<Friction> friction = new Setting<>("Friction", Friction.FAST)
            .setDescription("Mode for speed friction factor");

    public static Setting<Boolean> damage = new Setting<>("Damage", false)
            .setDescription("Attempt to damage player for smoother flight");

    public static Setting<Boolean> antiKick = new Setting<>("AntiKick", true)
            .setDescription("Prevents vanilla anticheat from kicking you");

    public static Setting<Boolean> ground = new Setting<>("Ground", false)
            .setDescription("Forces packets to be on the ground");

    // **************************** speeds ****************************

    public static Setting<Double> speed = new Setting<>("Speed", 0.1D, 1.0D, 10.0D, 1)
            .setDescription("Speed for horizontal movement");


    // previous fly capabilities
    private boolean previousFly;

    @Override
    public void onEnable() {
        super.onEnable();

        // previous fly state
        previousFly = mc.player.capabilities.isFlying;

        // allow vanilla flight
        if (mode.getValue().equals(Mode.VANILLA)) {

            // update vanilla flying
            if (!mc.player.capabilities.isFlying || !mc.player.capabilities.allowFlying) {
                mc.player.capabilities.isFlying = true;
                mc.player.capabilities.allowFlying = true;
            }
        }

        // damage when starting flight
        if (damage.getValue()) {
            if (mc.getConnection() != null) {

                // attempt to damage via fall damage
                ((INetworkManager) mc.getConnection().getNetworkManager()).hookDispatchPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.getEntityBoundingBox().minY + 3.5, mc.player.posZ, false), null);
                ((INetworkManager) mc.getConnection().getNetworkManager()).hookDispatchPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.getEntityBoundingBox().minY, mc.player.posZ, false), null);
                ((INetworkManager) mc.getConnection().getNetworkManager()).hookDispatchPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.getEntityBoundingBox().minY, mc.player.posZ, true), null);
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // reset fly states
        mc.player.capabilities.isFlying = previousFly;
        mc.player.capabilities.allowFlying = previousFly;
    }

    @SubscribeEvent
    public void onMotion(MotionEvent event) {

        // incompatible
        if (FreecamModule.INSTANCE.isEnabled()) {
            return;
        }

        // cancel vanilla movement
        event.setCanceled(true);

        // current speed
        float flightSpeed = getSpeedWithFriction(speed.getValue().floatValue());

        // allow total control of movement
        if (mode.getValue().equals(Mode.CREATIVE)) {

            // fall every 18 ticks
            if (mc.player.ticksExisted % 18 == 0 && antiKick.getValue()) {
                event.setY(-0.04);
            }

            else {
                event.setY(0);
            }

            // up/down movement
            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                event.setY(flightSpeed);
            }

            else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                event.setY(-flightSpeed);
            }

            // the current movement input values of the user
            float forward = mc.player.movementInput.moveForward;
            float strafe = mc.player.movementInput.moveStrafe;
            float yaw = mc.player.rotationYaw;

            // if we're not inputting any movements, then we shouldn't be adding any motion
            if (!MotionUtil.isMoving()) {
                event.setX(0);
                event.setZ(0);
            }

            else {

                // our facing values, according to movement not rotations
                double cos = Math.cos(Math.toRadians(yaw + 90));
                double sin = Math.sin(Math.toRadians(yaw + 90));

                // update the movements
                event.setX((forward * flightSpeed * cos) + (strafe * flightSpeed * sin));
                event.setZ((forward * flightSpeed * sin) - (strafe * flightSpeed * cos));

                // if we're not inputting any movements, then we shouldn't be adding any motion
                if (!MotionUtil.isMoving()) {
                    event.setX(0);
                    event.setZ(0);
                }
            }
        }

        // vanilla fly
        else if (mode.getValue().equals(Mode.VANILLA)) {

            // fall every 18 ticks
            if (mc.player.ticksExisted % 18 == 0 && antiKick.getValue()) {
                event.setY(-0.04);
            }

            // update vanilla flying
            if (!mc.player.capabilities.isFlying || !mc.player.capabilities.allowFlying) {
                mc.player.capabilities.isFlying = true;
                mc.player.capabilities.allowFlying = true;
            }

            // update vanilla fly speed
            mc.player.capabilities.setFlySpeed(flightSpeed);
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {

        // packet for moving and rotating
        if (event.getPacket() instanceof CPacketPlayer) {

            // force ground
            if (ground.getValue()) {
                ((ICPacketPlayer) event.getPacket()).setOnGround(true);
            }
        }
    }

    /**
     * Gets the speed of flight with friction factored in
     * @param speed The unscaled flight speed
     * @return The speed of flight with friction factored in
     */
    @SuppressWarnings("all")
    public float getSpeedWithFriction(float speed) {

        // base move speed
        float baseSpeed = 0.2873F;

        // scale move speed if Speed or Slowness potion effect is active
        if (mc.player.isPotionActive(MobEffects.SPEED)) {
            double amplifier = mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier();
            baseSpeed *= 1 + (0.2 * (amplifier + 1));
        }

        if (mc.player.isPotionActive(MobEffects.SLOWNESS)) {
            double amplifier = mc.player.getActivePotionEffect(MobEffects.SLOWNESS).getAmplifier();
            baseSpeed /= 1 + (0.2 * (amplifier + 1));
        }

        // scaled speed
        float scaledSpeed;

        // scale by friction
        switch (friction.getValue()) {
            case FAST:
            default:
                scaledSpeed = speed;
                break;
            case STRICT:
                scaledSpeed = speed - (speed / 159F); // NCP
                break;
            case FACTOR:
                scaledSpeed = speed - 1E-9F;
                break;
        }

        // higher speed
        return Math.max(scaledSpeed, baseSpeed);
    }

    public enum Mode {

        /**
         * Gives player vanilla flight capabilities
         */
        VANILLA,

        /**
         * Gives player full control over movement
         */
        CREATIVE
    }

    public enum Friction {

        /**
         * Silently scales speed
         */
        FACTOR,

        /**
         * Doesn't scale speed
         */
        FAST,

        /**
         * Scale speed for NCP-Updated
         */
        STRICT
    }
}
