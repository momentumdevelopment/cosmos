package cope.cosmos.client.features.modules.player;

import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.system.Timer;
import cope.cosmos.util.system.Timer.Format;
import net.minecraft.network.play.client.CPacketKeepAlive;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Sxmurai, linustouchtips
 * @since 10/26/2021
 */
public class PingSpoof extends Module {
    public static PingSpoof INSTANCE;

    public PingSpoof() {
        super("PingSpoof", Category.PLAYER, "Spoofs your latency to the server");
        INSTANCE = this;
    }

    public static Setting<Double> delay = new Setting<>("Delay", 0.1, 0.5, 5.0, 1).setDescription("The delay in seconds to hold off sending keep alive packets");

    // list of queued packets
    private final List<CPacketKeepAlive> packets = new CopyOnWriteArrayList<>();

    // timer for packet delay
    private final Timer packetTimer = new Timer();
    private boolean processing;

    @Override
    public void onUpdate() {
        // if we've cleared our time to send
        if (packetTimer.passedTime(delay.getValue().longValue() * 1000L, Format.SYSTEM)) {
            // in case this function is called too many times
            if (!processing) { 
                
                // we can now begin processing packets
                processing = true;
                
                // send all queued packets
                packets.forEach(packet -> {
                    if (packet != null) {
                        mc.player.connection.sendPacket(packet);
                    }
                });

                // we are done, processing packets
                processing = false;
            }
            
            // reset our packetTimer
            packetTimer.resetTime();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // send our packets before disabling
        if (!processing) {

            // we can now begin processing packets
            processing = true;

            // send all queued packets
            packets.forEach(packet -> {
                if (packet != null) {
                    mc.player.connection.sendPacket(packet);
                }
            });

            // we are done, processing packets
            processing = false;
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketKeepAlive) {
            if (!processing) {
                // cancel the packet, queue the packet to be sent later
                event.setCanceled(true);
                packets.add((CPacketKeepAlive) event.getPacket());
            }
        }
    }
}
