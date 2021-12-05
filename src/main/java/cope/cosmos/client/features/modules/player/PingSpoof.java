package cope.cosmos.client.features.modules.player;

import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.utility.system.Timer;
import cope.cosmos.utility.system.Timer.Format;
import net.minecraft.network.play.client.CPacketKeepAlive;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PingSpoof extends Module {
    public PingSpoof() {
        super("PingSpoof", Category.PLAYER, "Spoofs your latency to the server", () -> delay.getValue() + "s");
    }

    public static Setting<Double> delay = new Setting<>("Delay", "The delay in seconds to hold off sending keep alive packets", 0.1, 0.5, 5.0, 1);

    private final Queue<CPacketKeepAlive> packets = new ConcurrentLinkedQueue<>();
    private final Timer timer = new Timer();
    private boolean processing = false;

    @Override
    public void onDisable() {
        super.onDisable();

        if (!packets.isEmpty()) {
            process();
        }
    }

    @Override
    public void onUpdate() {
        if (timer.passedTime(delay.getValue().longValue() * 1000L, Format.SYSTEM)) {
            process();
            timer.resetTime();
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketKeepAlive && !processing) {
            event.setCanceled(true);
            packets.add((CPacketKeepAlive) event.getPacket());
        }
    }

    private void process() {
        if (processing) { // in case this function is called too many times
            return;
        }

        processing = true;
        while (!packets.isEmpty()) {
            CPacketKeepAlive packet = packets.poll();
            if (packet == null) {
                break;
            }

            mc.player.connection.sendPacket(packet);
        }

        processing = false;
    }
}
