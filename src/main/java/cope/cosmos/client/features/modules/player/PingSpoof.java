package cope.cosmos.client.features.modules.player;

import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.system.Timer;
import net.minecraft.network.play.client.CPacketKeepAlive;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PingSpoof extends Module {
    public static final Setting<Double> delay = new Setting<>("Delay", "The delay in seconds to hold off sending keep alive packets", 0.1, 0.5, 5.0, 1);

    private final Queue<CPacketKeepAlive> packets = new ConcurrentLinkedQueue<>();
    private final Timer timer = new Timer();
    private boolean processing = false;

    public PingSpoof() {
        super("PingSpoof", Category.PLAYER, "Spoofs your latency to the server", () -> delay.getValue() + "s");
    }

    @Override
    public void onDisable() {
        if (nullCheck() && !this.packets.isEmpty()) {
            this.process();
        }
    }

    @Override
    public void onUpdate() {
        if (this.timer.passed(delay.getValue().longValue() * 1000L, Timer.Format.SYSTEM)) {
            this.process();
            this.timer.reset();
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketKeepAlive && !this.processing) {
            event.setCanceled(true);
            this.packets.add((CPacketKeepAlive) event.getPacket());
        }
    }

    private void process() {
        if (this.processing) { // in case this function is called too many times
            return;
        }

        this.processing = true;
        while (!this.packets.isEmpty()) {
            CPacketKeepAlive packet = this.packets.poll();
            if (packet == null) {
                break;
            }

            mc.player.connection.sendPacket(packet);
        }

        this.processing = false;
    }
}
