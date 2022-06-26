package cope.cosmos.client.features.modules.movement;

import cope.cosmos.asm.mixins.accessor.INetHandlerPlayClient;
import cope.cosmos.asm.mixins.accessor.ISPacketPlayerPosLook;
import cope.cosmos.client.events.motion.movement.MotionEvent;
import cope.cosmos.client.events.network.DisconnectEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.combat.EnemyUtil;
import cope.cosmos.util.player.MotionUtil;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.PlayerSPPushOutOfBlocksEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author aesthetical, bon55, Doogie13, linustouchtips
 * @since 06/24/2022
 *
 * Special thanks to Doogie13 to explaining to me how all this worked, and his lambda plugin
 */
public class PacketFlightModule extends Module {
    public static PacketFlightModule INSTANCE;

    public PacketFlightModule() {
        super("PacketFlight", Category.MOVEMENT, "Allows you to fly with silent packet movements");
        INSTANCE = this;
    }

    // **************************** anticheat****************************

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.FACTOR)
            .setDescription("The mode for flying");

    public static Setting<Bounds> bounds = new Setting<>("Bounds", Bounds.DOWN)
            .setDescription("The invalid bounds packet to use");

    public static Setting<Phasing> phasing = new Setting<>("Phasing", Phasing.NCP)
            .setDescription("How phasing should be handled");

    public static Setting<Double> factor = new Setting<>("Factor", 0.1, 1.5, 5.0, 1)
            .setDescription("The amount of times to factor");

    public static Setting<Boolean> antiKick = new Setting<>("AntiKick", true)
            .setDescription("If to move down slightly every tick to prevent fly kicks");

    // packet maps
    private final Map<Integer, Vec3d> teleports = new ConcurrentHashMap<>();
    private final List<Position> packets = new ArrayList<>();

    // latest teleport spoof id
    private int teleportID = -1;

    // minecraft moment
    private static final double FUNNY_NUMBER = 0.0624;

    // current move speed
    private double moveSpeed;

    // flags
    private boolean flagged = false;
    private int flagTicks = 0;

    @Override
    public void onDisable() {
        super.onDisable();

        // reset vars
        teleports.clear();
        packets.clear();
        teleportID = -1;
        flagged = false;
        flagTicks = 0;
        moveSpeed = 0;

        // stop any motion we had before
        mc.player.motionX = 0.0;
        mc.player.motionY = 0.0;
        mc.player.motionZ = 0.0;
        mc.player.noClip = false;
    }

    @Override
    public void onTick() {

        // if we are dead, turn off packetfly
        if (EnemyUtil.isDead(mc.player)) {
            disable(true);
        }
    }

    @SubscribeEvent
    public void onMotion(MotionEvent event) {

        // set our move speed
        moveSpeed = 0.2873;

        // our motionY speed
        double motionY = 0.0;

        // use our movement controls to go up or down
        if (mc.gameSettings.keyBindJump.isKeyDown()) {
            motionY = FUNNY_NUMBER;
        }

        else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            motionY = -FUNNY_NUMBER;
        }

        else {

            // if we have antiKick enabled and we are not phasing
            if (antiKick.getValue() && !isPhased()) {

                // every two seconds, we'll move down a bit
                // we should also not try to anti-kick when we're on the ground
                if (mc.player.ticksExisted % 40 == 0 && !mc.player.onGround) {
                    motionY = -0.04;
                }
            }
        }

        // our factor value
        double f = factor.getValue();

        // get our factor based on our speed
        int factor = (int) Math.floor(f);

        // determine the best factor to go at
        if (mode.getValue().equals(Mode.FACTOR)) {

            // check if we have flagged recently
            if (flagged) {
                --flagTicks;
                if (flagTicks > 0) {

                    // slow down
                    moveSpeed = FUNNY_NUMBER;

                    // only send one round of packets
                    factor = 1;
                }
            }

            else {

                // add a factor
                if (mc.player.ticksExisted % 15.0 < 15.0 * (f - Math.floor(f))) {
                    factor++;
                }
            }
        }

        else {

            // we should only send one round of packets
            factor = 1;
        }

        // force NCP compatibility
        if (isPhased() && phasing.getValue().equals(Phasing.NCP)) {

            // we should go slower if we are trying to go up
            if (mc.gameSettings.keyBindJump.isKeyDown() || mc.gameSettings.keyBindSneak.isKeyDown()) {
                moveSpeed = 0.03;
            }

            else {
                moveSpeed = FUNNY_NUMBER;
            }

            // force one packet round
            factor = 1;
        }

        // send our movement packets
        // get our calculated motion values
        double[] strafe = MotionUtil.getMoveSpeed(moveSpeed);
        double motionX = strafe[0];
        double motionZ = strafe[1];

        // if we are not moving, set these values to 0.0
        if (!MotionUtil.isMoving()) {
            motionX = 0.0;
            motionZ = 0.0;
        }

        // get our player base vector
        Vec3d playerVec = mc.player.getPositionVector();

        // our factored in motion values
        double factoredMotionX = motionX * factor;
        double factoredMotionZ = motionZ * factor;

        // set our vertical motion
        mc.player.motionY = motionY;

        // go through each time we should factor through
        for (int i = 0; i < factor; ++ i) {

            // our position vector
            Vec3d vec = playerVec.addVector(factoredMotionX, motionY, factoredMotionZ);

            // set our client-sided motion
            mc.player.motionX = factoredMotionX;
            mc.player.motionZ = factoredMotionZ;

            // send our current position to the server
            sendPacket(vec);

            // do not send bounds packets in single player
            if (!mc.isSingleplayer()) {

                // we can now get out of bounds vec
                Vec3d boundsVec = bounds.getValue().modifier.modify(playerVec);

                // send this bounds vec
                sendPacket(boundsVec);
            }

            // increment our teleport id to predict the next teleport
            teleportID++;

            // the lagback packet we are expecting is the one we sent before our bounds
            teleports.put(teleportID, vec);

            // accept this teleport id
            mc.player.connection.sendPacket(new CPacketConfirmTeleport(teleportID));
        }

        // cancel, we'll use our own movement
        event.setCanceled(true);

        // set our player motion
        event.setX(mc.player.motionX);
        event.setY(motionY);
        event.setZ(mc.player.motionZ);


        // if we should phase, set noClip to true.
        if (!phasing.getValue().equals(Phasing.NONE)) {
            mc.player.noClip = true;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPacketSend(PacketEvent.PacketSendEvent event) {

        // if the client is not done loading the surrounding terrain, DO NOT CANCEL MOVEMENT PACKETS!!!!
        if (!((INetHandlerPlayClient) mc.player.connection).isDoneLoadingTerrain()) {
            return;
        }

        // if we are sending a player packet
        if (event.getPacket() instanceof CPacketPlayer) {

            // we only want to send movement packets
            if (!(event.getPacket() instanceof Position)) {
                event.setCanceled(true);
                return;
            }

            // our position packet
            Position packet = (Position) event.getPacket();

            // remove our packet and check if it didn't contain in our whitelist
            if (!packets.remove(packet)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {

        // if the client is not done loading the surrounding terrain, DO NOT CANCEL SERVER PACKETS!!!!
        if (!((INetHandlerPlayClient) mc.player.connection).isDoneLoadingTerrain()) {
            return;
        }

        // if we received a flag packet from NCP
        if (event.getPacket() instanceof SPacketPlayerPosLook) {

            // our teleport packet
            SPacketPlayerPosLook packet = (SPacketPlayerPosLook) event.getPacket();

            // the flag packet's teleport id
            int id = packet.getTeleportId();

            if (mode.getValue().equals(Mode.FACTOR)) {

                // if we have "predicted" this lagback
                if (teleports.containsKey(id)) {

                    // the cached teleport position
                    Vec3d vec = teleports.getOrDefault(id, null);

                    // remove this teleport from the map
                    teleports.remove(id);

                    // if the teleport position is null, we can disgard this
                    if (vec != null) {

                        // the teleport packet position has to be the EXACT same as the one we have cached
                        if (vec.x == packet.getX() && vec.y == packet.getY() && vec.z == packet.getZ()) {

                            // we found a match!
                            event.setCanceled(true);

                            // confirm that we got this teleport
                            mc.player.connection.sendPacket(new CPacketConfirmTeleport(id));
                            return;
                        }
                    }
                }

                // if we're still here after that return statement, unfortunately we did not predict on time. we should now slow down
                flagged = true;

                // slow down for 20 ticks
                flagTicks = 20;
            }

            // edit the packet client-sided to prevent the flag packet from rotating us to the server-side rotation
            ((ISPacketPlayerPosLook) packet).setYaw(mc.player.rotationYaw);
            ((ISPacketPlayerPosLook) packet).setPitch(mc.player.rotationPitch);

            // cache our teleport id
            teleportID = id;
        }
    }

    @SubscribeEvent
    public void onPushOutOfBlocks(PlayerSPPushOutOfBlocksEvent event) {

        // do not allow us to push out of the blocks
        event.setCanceled(true);
    }

    /**
     * Sends a position packet based on the given vector
     * @param vec the given vector
     */
    private void sendPacket(Vec3d vec) {

        // create our position packet
        Position packet = new Position(vec.x, vec.y, vec.z, true);

        // exempt this packet from being canceled (see onSendPacket)
        packets.add(packet);

        // send the packet to the sendQueue
        mc.player.connection.sendPacket(packet);
    }

    @SubscribeEvent
    public void onDisconnect(DisconnectEvent event) {

        // disable this module if we have been disconnected
        disable(true);
    }

    /**
     * Check if we are phased inside a block
     * @return if we are phased based on our bounding box
     */
    public boolean isPhased() {
        return !mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().expand(-0.0625, -0.0625, -0.0625)).isEmpty();
    }

    public enum Mode {
        /**
         * working on a description, auto told me kinda how it worked so i'll figure it out
         */
        FACTOR,

        /**
         * factor without cancelling packets
         */
        SETBACK
    }

    public enum Bounds {

        /**
         * Clips upwards for the invalid packet
         */
        UP((vec) -> vec.addVector(0.0, 1337.42069, 0.0)),

        /**
         * Clips downwards for the invalid packet
         */
        DOWN((vec) -> vec.addVector(0.0, -1337.42069, 0.0)),

        /**
         * Uses the minimum distance needed for packetfly to work (~100)
         */
        MIN((vec) -> vec.addVector(0.0, -100.42069, 0.0));

        private final PacketPositionModifier modifier;

        Bounds(PacketPositionModifier modifier) {
            this.modifier = modifier;
        }

        @FunctionalInterface
        private interface PacketPositionModifier {
            Vec3d modify(Vec3d in);
        }
    }

    public enum Phasing {
        /**
         * Does not try to phase
         */
        NONE,

        /**
         * Uses vanilla phasing, noClips and does nothing else
         */
        VANILLA,

        /**
         * NoClips and slows down
         */
        NCP
    }
}