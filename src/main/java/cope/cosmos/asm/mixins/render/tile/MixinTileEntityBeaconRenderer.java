package cope.cosmos.asm.mixins.render.tile;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.render.entity.RenderBeaconBeamEvent;
import net.minecraft.client.renderer.tileentity.TileEntityBeaconRenderer;
import net.minecraft.tileentity.TileEntityBeacon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityBeaconRenderer.class)
public class MixinTileEntityBeaconRenderer {

    @Inject(method = "render", at = @At("INVOKE"), cancellable = true)
    public void renderBeaconBeam(TileEntityBeacon tileEntityBeacon, double x, double y, double z, float partialTicks, int destroyStage, float alpha, CallbackInfo info) {
        RenderBeaconBeamEvent renderBeaconBeamEvent = new RenderBeaconBeamEvent();
        Cosmos.EVENT_BUS.post(renderBeaconBeamEvent);

        if (renderBeaconBeamEvent.isCanceled()) {
            info.cancel();
        }
    }
}
