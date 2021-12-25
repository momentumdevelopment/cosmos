package cope.cosmos.asm.mixins;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.ExceptionThrownEvent;
import cope.cosmos.client.events.PacketEvent;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(value = NetworkManager.class)
public class MixinNetworkManager {

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    public void onPacketSend(Packet<?> packet, CallbackInfo info) {
        PacketEvent.PacketSendEvent packetSendEvent = new PacketEvent.PacketSendEvent(packet);
        Cosmos.EVENT_BUS.post(packetSendEvent);

        if (packetSendEvent.isCanceled()) {
            info.cancel();
        }
    }

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    public void onPacketReceive(ChannelHandlerContext chc, Packet<?> packet, CallbackInfo info) {
        PacketEvent.PacketReceiveEvent packetReceiveEvent = new PacketEvent.PacketReceiveEvent(packet);
        Cosmos.EVENT_BUS.post(packetReceiveEvent);

        if (packetReceiveEvent.isCanceled()) {
            info.cancel();
        }
    }

    @Inject(method = "exceptionCaught", at = @At("HEAD"), cancellable = true)
    private void exceptionCaught(ChannelHandlerContext exceptionCaught1, Throwable exceptionCaught2, CallbackInfo info) {
        ExceptionThrownEvent exceptionThrownEvent = new ExceptionThrownEvent(exceptionCaught2);
        Cosmos.EVENT_BUS.post(exceptionThrownEvent);

        if (exceptionThrownEvent.isCanceled()) {
            info.cancel();
        }
    }
}
