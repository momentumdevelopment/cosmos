package cope.cosmos.client.events;

import net.minecraft.network.Packet;
import cope.cosmos.event.annotation.Cancelable;
import cope.cosmos.event.listener.Event;

public class PacketEvent extends Event {

    private final Packet<?> packet;

    public PacketEvent(Packet<?> packet) {
        this.packet = packet;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    @Cancelable
    public static class PacketReceiveEvent extends PacketEvent {
        public PacketReceiveEvent(Packet<?> packet) {
            super(packet);
        }
    }

    @Cancelable
    public static class PacketSendEvent extends PacketEvent {
        public PacketSendEvent(Packet<?> packet) {
            super(packet);
        }
    }
}
