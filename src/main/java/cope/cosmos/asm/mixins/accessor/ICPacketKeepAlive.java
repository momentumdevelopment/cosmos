package cope.cosmos.asm.mixins.accessor;

import net.minecraft.network.play.client.CPacketKeepAlive;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CPacketKeepAlive.class)
public interface ICPacketKeepAlive {

    @Accessor("key")
    void setKey(long key);
}
