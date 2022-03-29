package cope.cosmos.asm.mixins.network;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.client.ExceptionThrownEvent;
import cope.cosmos.client.events.network.DisconnectEvent;
import cope.cosmos.client.events.network.PacketEvent;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NetworkManager.class)
public class MixinNetworkManager {

    @Inject(method = "checkDisconnected", at = @At("HEAD"))
    public void onDisconnect(CallbackInfo info) {
        DisconnectEvent disconnectEvent = new DisconnectEvent();
        Cosmos.EVENT_BUS.post(disconnectEvent);
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    public void onSendPacket(Packet<?> packetIn, CallbackInfo info) {
        PacketEvent.PacketSendEvent packetSendEvent = new PacketEvent.PacketSendEvent(packetIn);
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
    private void onExceptionCaught(ChannelHandlerContext exceptionCaught1, Throwable exceptionCaught2, CallbackInfo info) {
        ExceptionThrownEvent exceptionThrownEvent = new ExceptionThrownEvent(exceptionCaught2);
        Cosmos.EVENT_BUS.post(exceptionThrownEvent);

        if (exceptionThrownEvent.isCanceled()) {
            info.cancel();
        }
    }
}
