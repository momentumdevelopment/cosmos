package cope.cosmos.asm.mixins.accessor;

import net.minecraft.client.network.NetHandlerPlayClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NetHandlerPlayClient.class)
public interface INetHandlerPlayClient {

    @Accessor("doneLoadingTerrain")
    boolean isDoneLoadingTerrain();
}
