package cope.cosmos.asm.mixins.world.block;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.block.SlimeEvent;
import net.minecraft.block.BlockSlime;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(BlockSlime.class)
public class MixinBlockSlime {

    @Inject(method = "onEntityWalk", at = @At("HEAD"), cancellable = true)
    private void onEntityCollidedWithBlock(World world, BlockPos blockPos, Entity entity, CallbackInfo info) {
        SlimeEvent slimeEvent = new SlimeEvent();
        Cosmos.EVENT_BUS.post(slimeEvent);

        if (slimeEvent.isCanceled())
            info.cancel();
    }
}
