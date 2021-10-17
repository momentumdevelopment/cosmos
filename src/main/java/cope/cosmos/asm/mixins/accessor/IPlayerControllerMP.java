package cope.cosmos.asm.mixins.accessor;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PlayerControllerMP.class)
public interface IPlayerControllerMP {

    @Accessor("curBlockDamageMP")
    void setCurrentBlockDamage(float currentBlockDamage);

    @Accessor("blockHitDelay")
    void setBlockHitDelay(int blockHitDelay);

    @Invoker("syncCurrentPlayItem")
    void hookSyncCurrentPlayItem();
}
