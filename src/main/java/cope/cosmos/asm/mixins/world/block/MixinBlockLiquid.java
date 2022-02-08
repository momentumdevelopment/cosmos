package cope.cosmos.asm.mixins.world.block;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.block.LiquidInteractEvent;
import cope.cosmos.util.Wrapper;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Mixin(BlockLiquid.class)
public class MixinBlockLiquid implements Wrapper {

    @Inject(method = "canCollideCheck", at = @At("HEAD"), cancellable = true)
    public void canCollideCheck(IBlockState blockState, boolean liquidLevel, CallbackInfoReturnable<Boolean> info) {
        LiquidInteractEvent liquidInteractEvent = new LiquidInteractEvent(blockState, liquidLevel);
        Cosmos.EVENT_BUS.post(liquidInteractEvent);

        if (liquidInteractEvent.isCanceled()) {
            info.cancel();
            info.setReturnValue(true);
        }
    }
}
