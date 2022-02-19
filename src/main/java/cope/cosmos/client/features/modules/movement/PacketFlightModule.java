package cope.cosmos.client.features.modules.movement;

import cope.cosmos.asm.mixins.accessor.ICPacketPlayer;
import cope.cosmos.asm.mixins.accessor.ISPacketPlayerPosLook;
import cope.cosmos.client.events.entity.EntityWorldEvent;
import cope.cosmos.client.events.entity.player.RotationUpdateEvent;
import cope.cosmos.client.events.motion.movement.MotionUpdateEvent;
import cope.cosmos.client.events.motion.movement.PushOutOfBlocksEvent;
import cope.cosmos.client.events.network.DisconnectEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.MotionUtil;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author bon55, linustouchtips
 * @since 04/17/2021
 */
public class PacketFlightModule extends Module {
    public static PacketFlightModule INSTANCE;

    public PacketFlightModule() {
        super("PacketFlight", Category.MOVEMENT, "Allows you to fly with silent packet movements");
        INSTANCE = this;
    }

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.FAST).setDescription("Mode for how to control packet rates");
    public static Setting<Type> type = new Setting<>("Type", Type.LIMIT_JITTER).setDescription("Mode for confirming packets");
    public static Setting<Bounds> bounds = new Setting<>("Bounds", Bounds.UP).setDescription("The packet bounds");
    public static Setting<Phase> phase = new Setting<>("Phase", Phase.FULL).setDescription("How to phase through blocks");
    public static Setting<Friction> friction = new Setting<>("Friction", Friction.FAST).setDescription("Applies block friction while phasing");
    public static Setting<Double> factor = new Setting<>("Factor", 0.0, 1.0, 5.0, 1).setDescription("Speed factor").setVisible(() -> mode.getValue().equals(Mode.FACTOR));
    public static Setting<Boolean> strict = new Setting<>("Strict", false).setDescription("Accepts server positions when receiving packets");
    public static Setting<Boolean> overshoot = new Setting<>("Overshoot", false).setDescription("Slightly overshoots the packet positions");
    public static Setting<Boolean> antiKick = new Setting<>("AntiKick", true).setDescription("Applies gravity to prevent detection by the vanilla anticheat");

    // packet map
    private final Map<Integer, Vec3d> packetMap = new ConcurrentHashMap<>();

    // packet teleport id
    private int teleportID;

    @SubscribeEvent
    public void onMotionUpdate(MotionUpdateEvent event) {
        // vanilla packet data
        event.setX(mc.player.posX);
        event.setY(mc.player.getEntityBoundingBox().minY);
        event.setZ(mc.player.posZ);
        event.setYaw(mc.player.rotationYaw);
        event.setPitch(mc.player.rotationPitch);
        event.setOnGround(false);

        // vertical movement
        double motionY = 0;

        // non-phase movement, allowed to move faster and more freely
        if (!isPhased()) {
            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                motionY = 0.031; // float up

                // fall
                if (mc.player.ticksExisted % 18 == 0 && antiKick.getValue()) {
                    motionY = -0.04;
                }
            }

            // float down
            if (mc.gameSettings.keyBindSneak.isKeyDown() && !mc.gameSettings.keyBindJump.isKeyDown()) {
                motionY = -0.031;
            }
        }

        // phase movement, no need to apply vanilla anticheat gravity here
        else {
            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                motionY = 0.017; // float up, slower
            }

            if (mc.gameSettings.keyBindSneak.isKeyDown() && !mc.gameSettings.keyBindJump.isKeyDown()) {
                motionY = -0.017; // float down, slower
            }
        }

        // frozen movement
        if (motionY == 0 && !isPhased()) {

            // fall
            if (mc.player.ticksExisted % 4 == 0 && antiKick.getValue()) {
                motionY = -0.0325;
            }
        }

        // the current movement input values of the user
        float forward = mc.player.movementInput.moveForward;
        float strafe = mc.player.movementInput.moveStrafe;
        float yaw = mc.player.rotationYaw;

        // if we're not inputting any movements, then we shouldn't be adding any motion
        if (!MotionUtil.isMoving()) {
            mc.player.motionX = 0;
            mc.player.motionZ = 0;
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

        double moveSpeed;
        if (motionY != 0) {
            moveSpeed = 0.026;
        }

        // we can move faster while not moving up
        else {
            moveSpeed = 0.040;

            if (mode.getValue().equals(Mode.FACTOR)) {
                moveSpeed *= factor.getValue();
            }
        }

        // horizontal motion
        double motionX = (forward * moveSpeed * cos) + (strafe * moveSpeed * sin);
        double motionZ = (forward * moveSpeed * sin) - (strafe * moveSpeed * cos);

        if (!isPhased()) {
            if (motionY != 0 && motionY != -0.0325) {
                motionX = 0;
                motionZ = 0;
            }

            else {
                motionX *= 3.59125;
                motionZ *= 3.59125;
            }
        }

        // apply block friction when moving through blocks
        else {
            if (friction.getValue().equals(Friction.STRICT)) {
                motionX *= 0.75;
                motionY *= 0.75;
            }
        }

        // update the movements
        mc.player.motionX = motionX;
        mc.player.motionY = motionY;
        mc.player.motionZ = motionZ;

        // if we're not inputting any movements, then we shouldn't be adding any motion
        if (!MotionUtil.isMoving()) {
            mc.player.motionX = 0;
            mc.player.motionZ = 0;
        }

        // allow the player to clip through blocks
        mc.player.noClip = true;

        // vectors
        Vec3d motionVector = new Vec3d(motionX, motionY, motionZ);
        Vec3d playerVector = new Vec3d(mc.player.posX, mc.player.getEntityBoundingBox().minY, mc.player.posZ);

        // slightly overshoot positions
        if (overshoot.getValue()) {
            playerVector.addVector(ThreadLocalRandom.current().nextDouble(-0.5, 0.5), ThreadLocalRandom.current().nextDouble(-0.5, 0.5), ThreadLocalRandom.current().nextDouble(-0.5, 0.5));
        }

        // bounds
        Vec3d boundVector = motionVector.add(playerVector.add(motionVector)).add(bounds.getValue().getAddition());

        // process packets
        if (mc.getConnection() != null) {

            // send packets
            if (mode.getValue().equals(Mode.FACTOR)) {
                if (motionY == 0) {

                    // percent chance to round
                    double factorize = factor.getValue() - StrictMath.floor(factor.getValue());

                    // (factorize)% chance of factorizing
                    double factorScaled = StrictMath.floor(factor.getValue());
                    if (StrictMath.random() <= factorize) {
                        factorScaled++;
                    }

                    // send factored packets
                    for (int i = 0; i < factorScaled; i++) {
                        double motionFactorX = (motionX / factorScaled) * (i + 1);
                        double motionFactorZ = (motionZ / factorScaled)* (i + 1);

                        //
                        mc.getConnection().getNetworkManager().sendPacket(new CPacketPlayer.Position(playerVector.x + motionFactorX, playerVector.add(motionVector).y, playerVector.z + motionFactorZ, false));
                    }
                }

                else {
                    // we can just move instantly
                    mc.getConnection().getNetworkManager().sendPacket(new CPacketPlayer.Position(playerVector.add(motionVector).x, playerVector.add(motionVector).y, playerVector.add(motionVector).z, false));
                }
            }

            else {
                // we can just move instantly
                mc.getConnection().getNetworkManager().sendPacket(new CPacketPlayer.Position(playerVector.add(motionVector).x, playerVector.add(motionVector).y, playerVector.add(motionVector).z, false));
            }

            mc.getConnection().getNetworkManager().sendPacket(new CPacketPlayer.Position(boundVector.x, boundVector.y, boundVector.z, false));

            // predict teleport packet
            if (type.getValue().equals(Type.JITTER) || type.getValue().equals(Type.LIMIT_JITTER)) {
                if (!packetMap.containsKey(teleportID)) {
                    teleportID++;

                    // confirm predicted teleport
                    mc.player.connection.sendPacket(new CPacketConfirmTeleport(teleportID));
                    packetMap.put(teleportID, playerVector);

                    // teleport player since we know where they are going
                    mc.player.setPosition(playerVector.x, playerVector.y, playerVector.z);
                }
            }
        }
    }

    @SubscribeEvent
    public void onRotationUpdate(RotationUpdateEvent event) {
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketPlayer) {
            // cancel the packet if we are moving
            if (((ICPacketPlayer) event.getPacket()).isMoving()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {
        if (event.getPacket() instanceof SPacketPlayerPosLook) {

            // packet vector associated with this rubberband
            Vec3d packetVector = packetMap.remove(((SPacketPlayerPosLook) event.getPacket()).getTeleportId());

            // server data
            double serverX = ((SPacketPlayerPosLook) event.getPacket()).getX();
            double serverY = ((SPacketPlayerPosLook) event.getPacket()).getY();
            double serverZ = ((SPacketPlayerPosLook) event.getPacket()).getZ();
            float serverYaw = ((SPacketPlayerPosLook) event.getPacket()).getYaw();
            float serverPitch = ((SPacketPlayerPosLook) event.getPacket()).getPitch();

            if (((SPacketPlayerPosLook) event.getPacket()).getFlags().contains(SPacketPlayerPosLook.EnumFlags.X)) {
                serverX += mc.player.posX;
            }

            if (((SPacketPlayerPosLook) event.getPacket()).getFlags().contains(SPacketPlayerPosLook.EnumFlags.Y)) {
                serverY += mc.player.getEntityBoundingBox().minY;
            }

            if (((SPacketPlayerPosLook) event.getPacket()).getFlags().contains(SPacketPlayerPosLook.EnumFlags.Z)) {
                serverZ += mc.player.posZ;
            }

            if (((SPacketPlayerPosLook) event.getPacket()).getFlags().contains(SPacketPlayerPosLook.EnumFlags.X_ROT)) {
                serverPitch += mc.player.rotationPitch;
            }

            if (((SPacketPlayerPosLook) event.getPacket()).getFlags().contains(SPacketPlayerPosLook.EnumFlags.Y_ROT)) {
                serverYaw += mc.player.rotationYaw;
            }

            // quickly confirm the teleport
            if (type.getValue().equals(Type.LIMIT) || type.getValue().equals(Type.LIMIT_JITTER)) {
                mc.player.connection.sendPacket(new CPacketConfirmTeleport(((SPacketPlayerPosLook) event.getPacket()).getTeleportId()));
            }

            // accept server position
            if (strict.getValue()) {
                mc.player.setPosition(serverX, serverY, serverZ);

                if (mc.getConnection() != null) {
                    mc.getConnection().sendPacket(new CPacketPlayer.PositionRotation(serverX, serverY, serverZ, serverYaw, serverPitch, false));
                }
            }

            switch (mode.getValue()) {
                // we already predicted this rubberband and have sent a packet to resolve it, so we can ignore the server request
                case FAST:
                    if (packetVector.x == serverX && packetVector.y == serverY && packetVector.z == serverZ) {
                        event.setCanceled(true);
                    }

                    else {
                        ((ISPacketPlayerPosLook) event.getPacket()).setYaw(mc.player.rotationYaw);
                        ((ISPacketPlayerPosLook) event.getPacket()).setPitch(mc.player.rotationPitch);

                        // remove packet flags
                        ((SPacketPlayerPosLook) event.getPacket()).getFlags().remove(SPacketPlayerPosLook.EnumFlags.X_ROT);
                        ((SPacketPlayerPosLook) event.getPacket()).getFlags().remove(SPacketPlayerPosLook.EnumFlags.Y_ROT);
                    }

                    break;
                case CONCEAL:
                    event.setCanceled(true);
                    break;
                case FACTOR:
                    ((ISPacketPlayerPosLook) event.getPacket()).setYaw(mc.player.rotationYaw);
                    ((ISPacketPlayerPosLook) event.getPacket()).setPitch(mc.player.rotationPitch);

                    // remove packet flags
                    ((SPacketPlayerPosLook) event.getPacket()).getFlags().remove(SPacketPlayerPosLook.EnumFlags.X_ROT);
                    ((SPacketPlayerPosLook) event.getPacket()).getFlags().remove(SPacketPlayerPosLook.EnumFlags.Y_ROT);

                    break;
            }

            // update our current teleport id
            teleportID = ((SPacketPlayerPosLook) event.getPacket()).getTeleportId();
        }
    }

    @SubscribeEvent
    public void onPushOutOfBlock(PushOutOfBlocksEvent event) {
        if (isPhased()) {
            event.setCanceled(true); // prevent blocks from applying velocity to the player
        }
    }

    @SubscribeEvent
    public void onEntityRemove(EntityWorldEvent.EntityRemoveEvent event) {
        if (event.getEntity().equals(mc.player)) {
            // disable module on death
            disable(true);
        }
    }

    @SubscribeEvent
    public void onDisconnect(DisconnectEvent event) {
        // disable module on disconnect
        disable(true);
    }

    /**
     * Checks whether the player is phased inside a block
     * @return Whether the player is phased inside a block
     */
    public boolean isPhased() {
        return !mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().contract(0.125, 0.15, 0.125)).isEmpty();
    }

    public enum Mode {

        /**
         * Cancels server packet requests
         */
        FAST,

        /**
         * Attempts to conceal bounds
         */
        CONCEAL,

        /**
         * Allows you to change the packet rate
         */
        FACTOR
    }

    public enum Type {

        /**
         * Confirms packets proactively
         */
        LIMIT,

        /**
         * Confirms packets reactively
         */
        JITTER,

        /**
         * Confirms packets proactively and reactively
         */
        LIMIT_JITTER,

        /**
         * Does not confirm teleport packets
         */
        NONE
    }

    public enum Phase {

        /**
         * Allows you to phase through any blocks
         */
        FULL,

        /**
         * Allows you to phase through "no clip" blocks
         */
        SEMI,

        /**
         * Does not allow you to phase through blocks
         */
        NONE
    }

    public enum Friction {

        /**
         * Does not apply block friction when phasing
         */
        FAST,

        /**
         * Applies block friction when phasing
         */
        STRICT
    }

    public enum Bounds {

        /**
         * Up bounds
         */
        UP(0, 6980085, 0),

        /**
         * Down bounds
         */
        DOWN(0, -6980085, 0),

        /**
         * Conceals the horizontal and vertical positions
         */
        CONCEAL(ThreadLocalRandom.current().nextInt(-100000, 100000), 2, ThreadLocalRandom.current().nextInt(-100000, 100000)),

        /**
         * Preserves the player height
         */
        PRESERVE(ThreadLocalRandom.current().nextInt(100000), 0, ThreadLocalRandom.current().nextInt(100000));

        // the vector to add to the player position
        private final Vec3d addition;

        Bounds(double x, double y, double z) {
            addition = new Vec3d(x, y, z);
        }

        /**
         * Gets the vector to add to the player position
         * @return The vector to add to the player position
         */
        public Vec3d getAddition() {
            return addition;
        }
    }
}
