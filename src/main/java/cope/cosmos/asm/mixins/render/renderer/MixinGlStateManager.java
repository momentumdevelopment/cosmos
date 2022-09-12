package cope.cosmos.asm.mixins.render.renderer;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.render.world.RenderFogEvent;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.class)
public class MixinGlStateManager {

    @Inject(method = "enableFog", at = @At("HEAD"), cancellable = true)
    private static void onEnableFog(CallbackInfo info) {
        RenderFogEvent renderFogEvent = new RenderFogEvent();
        Cosmos.EVENT_BUS.post(renderFogEvent);

        if (renderFogEvent.isCanceled()) {
            info.cancel();
        }
    }
}
