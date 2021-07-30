package cope.cosmos.asm.mixins;

import cope.cosmos.client.events.RenderBeaconBeamEvent;
import net.minecraft.client.renderer.tileentity.TileEntityBeaconRenderer;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(TileEntityBeaconRenderer.class)
public class MixinTileEntityBeaconRenderer {

    @Inject(method = "render", at = @At("INVOKE"), cancellable = true)
    private void renderBeaconBeam(TileEntityBeacon tileEntityBeacon, double x, double y, double z, float partialTicks, int destroyStage, float alpha, CallbackInfo info) {
        RenderBeaconBeamEvent renderBeaconBeamEvent = new RenderBeaconBeamEvent();
        MinecraftForge.EVENT_BUS.post(renderBeaconBeamEvent);

        if (renderBeaconBeamEvent.isCanceled())
            info.cancel();
    }
}
