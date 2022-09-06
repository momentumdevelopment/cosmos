package cope.cosmos.asm.mixins.render;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.render.world.RenderCaveCullingEvent;
import net.minecraft.client.renderer.chunk.SetVisibility;
import net.minecraft.client.renderer.chunk.VisGraph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VisGraph.class)
public class MixinVisGraph {

    @Inject(method = "computeVisibility", at = @At("HEAD"), cancellable = true)
    public void onComputeVisibility(CallbackInfoReturnable<SetVisibility> info) {
        if (Cosmos.EVENT_BUS.post(new RenderCaveCullingEvent())) {
            SetVisibility setVisibility = new SetVisibility();
            setVisibility.setAllVisible(true);

            info.setReturnValue(setVisibility);
        }
    }
}