package cope.cosmos.asm.mixins.world.block;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.block.SoulSandEvent;
import net.minecraft.block.BlockSoulSand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(BlockSoulSand.class)
public class MixinBlockSoulSand {

    @Inject(method = "onEntityCollidedWithBlock", at = @At("HEAD"), cancellable = true)
    public void onEntityCollidedWithBlock(World world, BlockPos blockPos, IBlockState iBlockState, Entity entity, CallbackInfo info) {
        SoulSandEvent soulSandEvent = new SoulSandEvent();
        Cosmos.EVENT_BUS.post(soulSandEvent);

        if (soulSandEvent.isCanceled())
            info.cancel();
    }
}
