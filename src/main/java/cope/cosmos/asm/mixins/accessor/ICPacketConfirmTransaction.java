package cope.cosmos.asm.mixins.accessor;

import net.minecraft.network.play.client.CPacketConfirmTransaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CPacketConfirmTransaction.class)
public interface ICPacketConfirmTransaction {
  
    @Accessor("uid")
    void setUid(short uid);
}
