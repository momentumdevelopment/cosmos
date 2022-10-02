package cope.cosmos.asm.mixins.input;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.block.BlockBreakEvent;
import cope.cosmos.client.events.block.BlockResetEvent;
import cope.cosmos.client.events.entity.player.interact.ReachEvent;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {

    @Inject(method = "onPlayerDestroyBlock", at = @At("RETURN"), cancellable = true)
    private void onOnPlayerDestroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> info) {
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(pos);
        Cosmos.EVENT_BUS.post(blockBreakEvent);

        if (blockBreakEvent.isCanceled()) {
            info.cancel();
            info.setReturnValue(false);
        }
    }

    @Inject(method = "resetBlockRemoving", at = @At(value = "HEAD"), cancellable = true)
    private void onResetBlockRemoving(CallbackInfo info) {
        BlockResetEvent blockResetEvent = new BlockResetEvent();
        Cosmos.EVENT_BUS.post(blockResetEvent);

        if (blockResetEvent.isCanceled()) {
            info.cancel();
        }
    }

    @Inject(method = "getBlockReachDistance", at = @At("RETURN"), cancellable = true)
    private void onGetReachDistance(CallbackInfoReturnable<Float> info) {
        ReachEvent reachEvent = new ReachEvent();
        Cosmos.EVENT_BUS.post(reachEvent);

        if (reachEvent.isCanceled()) {
            info.cancel();
            info.setReturnValue(reachEvent.getReach());
        }
    }
}
