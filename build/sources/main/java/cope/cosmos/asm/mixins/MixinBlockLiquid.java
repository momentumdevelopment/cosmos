package cope.cosmos.asm.mixins;

import cope.cosmos.client.events.LiquidInteractEvent;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Mixin(BlockLiquid.class)
public class MixinBlockLiquid {

    @Inject(method = "canCollideCheck", at = @At("HEAD"), cancellable = true)
    public void canCollideCheck(IBlockState blockState, boolean liquidLevel, CallbackInfoReturnable<Boolean> info) {
        LiquidInteractEvent liquidInteractEvent = new LiquidInteractEvent(blockState, liquidLevel);
        MinecraftForge.EVENT_BUS.post(liquidInteractEvent);

        if (liquidInteractEvent.isCanceled()) {
            info.cancel();
            info.setReturnValue(true);
        }
    }
}
