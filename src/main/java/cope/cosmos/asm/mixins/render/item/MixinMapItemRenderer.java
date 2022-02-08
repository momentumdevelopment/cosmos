package cope.cosmos.asm.mixins.render.item;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.render.other.RenderMapEvent;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.world.storage.MapData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(MapItemRenderer.class)
public class MixinMapItemRenderer {

    @Inject(method = "renderMap", at = @At("HEAD"), cancellable = true)
    public void renderMap(MapData mapdataIn, boolean noOverlayRendering, CallbackInfo info) {
        RenderMapEvent renderMapEvent = new RenderMapEvent();
        Cosmos.EVENT_BUS.post(renderMapEvent);

        if (renderMapEvent.isCanceled())
            info.cancel();
    }
}
