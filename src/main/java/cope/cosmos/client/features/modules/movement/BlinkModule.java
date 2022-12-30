package cope.cosmos.client.features.modules.movement;

import cope.cosmos.client.events.network.DisconnectEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.exploits.PacketFlightModule;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.math.Timer;
import cope.cosmos.util.math.Timer.Format;
import cope.cosmos.util.player.PlayerUtil;
import cope.cosmos.util.string.ColorUtil;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author aesthetical, linustouchtips
 * @since 10/26/2021
 */
public class BlinkModule extends Module {
    public static BlinkModule INSTANCE;

    public BlinkModule() {
        super("Blink", new String[] {"FakeLag"}, Category.MOVEMENT, "Caches player packets and dumps them all at once");
        INSTANCE = this;
    }

    // **************************** general ****************************

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.MANUAL)
            .setDescription("When to send packets");

    // **************************** packets ****************************

    public static Setting<Double> delay = new Setting<>("Delay", 0.1, 1.0, 10.0, 1)
            .setAlias("Pulse")
            .setDescription("The delay in seconds until sending all packets")
            .setVisible(() -> mode.getValue().equals(Mode.PULSE));

    public static Setting<Double> packets = new Setting<>("Packets", 0.0, 10.0, 200.0, 0)
            .setDescription("The amount of packets until sending all packets")
            .setVisible(() -> mode.getValue().equals(Mode.PACKETS));

    public static Setting<Double> distance = new Setting<>("Distance", 1.0, 10.0, 20.0, 0)
            .setDescription("The distance in blocks from the last position until sending all packets")
            .setVisible(() -> mode.getValue().equals(Mode.DISTANCE));

    // list of withheld packets
    private final List<Packet<?>> playerPackets = new CopyOnWriteArrayList<>();

    // keeps track of time between packet dump
    private final Timer packetTimer = new Timer();

    // our last server position
    private BlockPos serverPosition;

    // List of positions
    // Would prefer to use a map, but ConcurrentHashMap does some weird shit when rendering the line, LinkedHashMap throws ConcurrentModificationExceptions, and there isn't a ConcurrentLinkedHashMap :(
    private final LinkedList<Position> positions = new LinkedList<>();

    @Override
    public void onEnable() {
        super.onEnable();

        // mark last server position
        serverPosition = PlayerUtil.getPosition();

        // visual model of last server position
        EntityOtherPlayerMP serverPositionModel = new EntityOtherPlayerMP(mc.world, mc.player.getGameProfile());

        // match characteristics of player to model -> create a copy
        serverPositionModel.copyLocationAndAnglesFrom(mc.player);
        serverPositionModel.rotationYawHead = mc.player.rotationYaw;
        serverPositionModel.inventory.copyInventory(mc.player.inventory);
        serverPositionModel.setSneaking(mc.player.isSneaking());
        serverPositionModel.setPrimaryHand(mc.player.getPrimaryHand());

        // add model to world
        mc.world.addEntityToWorld(-100, serverPositionModel);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // can't blink while flying
        if (PlayerUtil.isFlying() || PacketFlightModule.INSTANCE.isEnabled()) {
            return;
        }

        // can't blink while riding
        if (mc.player.isRiding()) {
            return;
        }

        // dump all player packets
        if (!playerPackets.isEmpty()) {
            playerPackets.forEach(packet -> {
                if (packet != null) {
                    mc.player.connection.sendPacket(packet);
                }
            });

            // clear cached packets
            playerPackets.clear();
        }

        // remove our model from the world
        mc.world.removeEntityFromWorld(-100);
        serverPosition = null;

        // Clear positions
        positions.clear();
    }

    @Override
    public void onTick() {

        // check world
        if (!nullCheck() || mc.player.ticksExisted <= 20) {

            // We may have just loaded into a world, so we need to clear the positions
            positions.clear();
            return;
        }

        // Add the player's position
        // We are adding the player's last position so it is just behind the player, and will not be obvious on the screen (especially when elytra flying)
        positions.add(new Position(new Vec3d(mc.player.lastTickPosX, mc.player.lastTickPosY, mc.player.lastTickPosZ), System.currentTimeMillis()));
    }

    @Override
    public void onUpdate() {

        // can't blink while flying
        if (PlayerUtil.isFlying() || PacketFlightModule.INSTANCE.isEnabled()) {
            return;
        }

        // can't blink while riding
        if (mc.player.isRiding()) {
            return;
        }

        // apply packets
        switch (mode.getValue()) {
            case PULSE: {
                if (packetTimer.passedTime(delay.getValue().longValue(), Format.SECONDS)) {

                    // remove our old model from the world
                    mc.world.removeEntityFromWorld(-100);

                    // mark new server position
                    serverPosition = PlayerUtil.getPosition();

                    // visual model of new server position
                    EntityOtherPlayerMP serverPositionModel = new EntityOtherPlayerMP(mc.world, mc.player.getGameProfile());

                    // match characteristics of player to model -> create a copy
                    serverPositionModel.copyLocationAndAnglesFrom(mc.player);
                    serverPositionModel.rotationYawHead = mc.player.rotationYaw;
                    serverPositionModel.inventory.copyInventory(mc.player.inventory);
                    serverPositionModel.setSneaking(mc.player.isSneaking());
                    serverPositionModel.setPrimaryHand(mc.player.getPrimaryHand());

                    // add model to world
                    mc.world.addEntityToWorld(-100, serverPositionModel);

                    // dump all player packets
                    if (!playerPackets.isEmpty()) {
                        playerPackets.forEach(packet -> {

                            // make sure packet exists
                            if (packet != null) {
                                mc.player.connection.sendPacket(packet);
                            }
                        });

                        // clear cached packets
                        playerPackets.clear();
                        positions.clear();
                    }

                    // reset
                    packetTimer.resetTime();
                }

                break;
            }

            case PACKETS: {
                if (playerPackets.size() >= packets.getValue()) {

                    // remove our old model from the world
                    mc.world.removeEntityFromWorld(-100);

                    // mark new server position
                    serverPosition = PlayerUtil.getPosition();

                    // visual model of new server position
                    EntityOtherPlayerMP serverPositionModel = new EntityOtherPlayerMP(mc.world, mc.player.getGameProfile());

                    // match characteristics of player to model -> create a copy
                    serverPositionModel.copyLocationAndAnglesFrom(mc.player);
                    serverPositionModel.rotationYawHead = mc.player.rotationYaw;
                    serverPositionModel.inventory.copyInventory(mc.player.inventory);
                    serverPositionModel.setSneaking(mc.player.isSneaking());
                    serverPositionModel.setPrimaryHand(mc.player.getPrimaryHand());

                    // add model to world
                    mc.world.addEntityToWorld(-100, serverPositionModel);

                    // dump all player packets
                    if (!playerPackets.isEmpty()) {
                        playerPackets.forEach(packet -> {
                            if (packet != null) {
                                mc.player.connection.sendPacket(packet);
                            }
                        });

                        // clear cached packets
                        playerPackets.clear();
                        positions.clear();
                    }
                }

                break;
            }

            case DISTANCE:
                if (mc.player.getDistance(serverPosition.getX(), serverPosition.getY(), serverPosition.getZ()) >= distance.getValue()) {

                    // remove our old model from the world
                    mc.world.removeEntityFromWorld(-100);

                    // mark new server position
                    serverPosition = PlayerUtil.getPosition();

                    // visual model of new server position
                    EntityOtherPlayerMP serverPositionModel = new EntityOtherPlayerMP(mc.world, mc.player.getGameProfile());

                    // match characteristics of player to model -> create a copy
                    serverPositionModel.copyLocationAndAnglesFrom(mc.player);
                    serverPositionModel.rotationYawHead = mc.player.rotationYaw;
                    serverPositionModel.inventory.copyInventory(mc.player.inventory);
                    serverPositionModel.setSneaking(mc.player.isSneaking());
                    serverPositionModel.setPrimaryHand(mc.player.getPrimaryHand());

                    // add model to world
                    mc.world.addEntityToWorld(-100, serverPositionModel);
                    
                    // dump all player packets
                    if (!playerPackets.isEmpty()) {
                        playerPackets.forEach(packet -> {
                            if (packet != null) {
                                mc.player.connection.sendPacket(packet);
                            }
                        });

                        // clear cached packets
                        playerPackets.clear();
                        positions.clear();
                    }
                }

                break;
        }
    }

    @Override
    public void onRender3D() {
        glPushMatrix();
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glEnable(GL_BLEND);
        glDisable(GL_DEPTH_TEST);
        glLineWidth(1.5F);

        // disable render lighting
        mc.entityRenderer.disableLightmap();

        glBegin(GL_LINE_STRIP);

        // Render positions
        positions.forEach(position -> {

            // Set line colour
            glColor4f(ColorUtil.getPrimaryColor().getRed() / 255F, ColorUtil.getPrimaryColor().getGreen() / 255F, ColorUtil.getPrimaryColor().getBlue() / 255F, 1);

            // draw line
            glVertex3d(position.getVec().x - mc.getRenderManager().viewerPosX, position.getVec().y - mc.getRenderManager().viewerPosY, position.getVec().z - mc.getRenderManager().viewerPosZ);
        });

        // Reset colour
        glColor4d(1, 1, 1, 1);

        glEnd();
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_LINE_SMOOTH);
        glDisable(GL_BLEND);
        glEnable(GL_TEXTURE_2D);
        glPopMatrix();
    }

    @SubscribeEvent
    public void onDisconnect(DisconnectEvent event) {

        // disable on a disconnect to prevent kicks upon joining
        disable(true);
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {

        // can't blink while flying
        if (PlayerUtil.isFlying() || PacketFlightModule.INSTANCE.isEnabled()) {
            return;
        }

        // can't blink while riding
        if (mc.player.isRiding()) {
            return;
        }

        // player packets
        if (!(event.getPacket() instanceof CPacketChatMessage || event.getPacket() instanceof CPacketConfirmTeleport || event.getPacket() instanceof CPacketKeepAlive || event.getPacket() instanceof CPacketTabComplete || event.getPacket() instanceof CPacketClientStatus)) {

            // check it's one of the packets we are dumping
            if (!playerPackets.contains(event.getPacket())) {

                // cache the packet
                event.setCanceled(true);
                playerPackets.add(event.getPacket());
            }
        }
    }

    public enum Mode {

        /**
         * Manually send cached packets when disabling
         */
        MANUAL,

        /**
         * Sends cached packets after a certain delay
         */
        PULSE,

        /**
         * Sends cached packets after a certain threshold
         */
        PACKETS,

        /**
         * Sends cached packets after a certain distance from the server position
         */
        DISTANCE
    }

    public static class Position {

        // The position's vector
        private final Vec3d vec;

        // The System.currentTimeMillis() at the time of instantiating the position
        private final long time;

        public Position(Vec3d vec, long time) {
            this.vec = vec;
            this.time = time;
        }

        /**
         * Gets the position's vector
         * @return The position's vector
         */
        public Vec3d getVec() {
            return vec;
        }

        /**
         * Gets the creation time
         * @return The creation time
         */
        public long getTime() {
            return time;
        }
    }
}
