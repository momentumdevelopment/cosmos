package cope.cosmos.asm.mixins;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.BlockBreakEvent;
import cope.cosmos.client.events.BlockResetEvent;
import cope.cosmos.client.events.ReachEvent;
import cope.cosmos.util.Wrapper;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP implements Wrapper {

    @Inject(method = "onPlayerDestroyBlock", at = @At("RETURN"))
    private void destroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> info) {
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(pos);
        Cosmos.EVENT_BUS.dispatch(blockBreakEvent);

        if (blockBreakEvent.isCanceled()) {
            info.cancel();
            info.setReturnValue(false);
        }
    }

    @Inject(method = "resetBlockRemoving", at = @At(value = "HEAD"), cancellable = true)
    private void resetBlock(CallbackInfo info) {
        BlockResetEvent blockResetEvent = new BlockResetEvent();
        Cosmos.EVENT_BUS.dispatch(blockResetEvent);

        if (blockResetEvent.isCanceled())
            info.cancel();
    }

    @Inject(method = "getBlockReachDistance", at = @At("RETURN"), cancellable = true)
    private void getReachDistanceHook(final CallbackInfoReturnable<Float> info) {
        ReachEvent reachEvent = new ReachEvent();
        Cosmos.EVENT_BUS.dispatch(reachEvent);

        if (reachEvent.isCanceled()) {
            info.cancel();
            info.setReturnValue(reachEvent.getReach());
        }
    }
}
