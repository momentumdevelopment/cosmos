package cope.cosmos.asm.mixins.accessor;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PlayerControllerMP.class)
public interface IPlayerControllerMP {

    @Accessor("curBlockDamageMP")
    void setCurrentBlockDamage(float currentBlockDamage);

    @Accessor("curBlockDamageMP")
    float getCurrentBlockDamage();

    @Accessor("blockHitDelay")
    void setBlockHitDelay(int blockHitDelay);

    @Accessor("currentPlayerItem")
    int getCurrentPlayerItem();

    @Accessor("currentPlayerItem")
    void setCurrentPlayerItem(int currentPlayerItem);

    @Invoker("syncCurrentPlayItem")
    void hookSyncCurrentPlayItem();
}
