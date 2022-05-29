package cope.cosmos.client.features.modules.player;

import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.math.Timer;
import cope.cosmos.util.math.Timer.Format;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author aesthetical, linustouchtips
 * @since 10/26/2021
 */
public class BlinkModule extends Module {
    public static BlinkModule INSTANCE;

    public BlinkModule() {
        super("Blink", Category.PLAYER, "Caches player packets and dumps them all at once");
        INSTANCE = this;
    }

    // **************************** general ****************************

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.MANUAL)
            .setDescription("When to send packets");

    // **************************** packets ****************************

    public static Setting<Double> delay = new Setting<>("Delay", 0.1, 5.0, 50.0, 1)
            .setDescription("The delay in seconds until sending all packets")
            .setVisible(() -> mode.getValue().equals(Mode.DELAY));

    public static Setting<Double> packets = new Setting<>("Packets", 0.0, 10.0, 200.0, 0)
            .setDescription("The amount of packets until sending all packets")
            .setVisible(() -> mode.getValue().equals(Mode.PACKETS));

    public static Setting<Double> distance = new Setting<>("Distance", 1.0, 10.0, 20.0, 0)
            .setDescription("The distance in blocks from the last position until sending all packets")
            .setVisible(() -> mode.getValue().equals(Mode.DISTANCE));

    // list of withheld packets
    private final List<CPacketPlayer> playerPackets = new CopyOnWriteArrayList<>();

    // keeps track of time between packet dump
    private final Timer packetTimer = new Timer();

    // our last server position
    private BlockPos serverPosition;

    @Override
    public void onEnable() {
        super.onEnable();

        // mark last server position
        serverPosition = mc.player.getPosition();

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
    }

    @Override
    public void onUpdate() {
        switch (mode.getValue()) {
            case DELAY: {
                if (packetTimer.passedTime(delay.getValue().longValue(), Format.SECONDS)) {

                    // remove our old model from the world
                    mc.world.removeEntityFromWorld(-100);

                    // mark new server position
                    serverPosition = mc.player.getPosition();

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
                    }
                    
                    packetTimer.resetTime();
                }

                break;
            }

            case PACKETS: {
                if (playerPackets.size() >= packets.getValue()) {
                    // remove our old model from the world
                    mc.world.removeEntityFromWorld(-100);

                    // mark new server position
                    serverPosition = mc.player.getPosition();

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
                    }
                }

                break;
            }

            case DISTANCE:
                if (mc.player.getDistance(serverPosition.getX(), serverPosition.getY(), serverPosition.getZ()) >= distance.getValue()) {
                    // remove our old model from the world
                    mc.world.removeEntityFromWorld(-100);

                    // mark new server position
                    serverPosition = mc.player.getPosition();

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
                    }
                }

                break;
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        // player packet
        if (event.getPacket() instanceof CPacketPlayer) {

            // check it's one of the packets we are dumping
            if (!playerPackets.contains((CPacketPlayer) event.getPacket())) {

                // cache the packet
                event.setCanceled(true);
                playerPackets.add((CPacketPlayer) event.getPacket());
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
        DELAY,

        /**
         * Sends cached packets after a certain threshold
         */
        PACKETS,

        /**
         * Sends cached packets after a certain distance from the server position
         */
        DISTANCE
    }
}
