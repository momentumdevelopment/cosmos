package cope.cosmos.asm.mixins.network;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.network.DecodeEvent;
import net.minecraft.network.NettyCompressionDecoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(NettyCompressionDecoder.class)
public class MixinNettyCompressionDecoder {

    @ModifyConstant(method = "decode", constant = @Constant(intValue = 0x200000))
    private int onDecode(int n) {
        DecodeEvent decodeEvent = new DecodeEvent();
        Cosmos.EVENT_BUS.post(decodeEvent);

        // no packet limit
        if (decodeEvent.isCanceled()) {
            return Integer.MAX_VALUE;
        }

        return n;
    }
}
