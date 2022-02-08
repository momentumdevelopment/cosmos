package cope.cosmos.asm.mixins.render.tile;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.render.entity.tile.RenderTileEntityEvent;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityRendererDispatcher.class)
public class MixinTileEntityRenderDispatcher {

    @Shadow
    private Tessellator batchBuffer;

    @Inject(method = "render(Lnet/minecraft/tileentity/TileEntity;DDDFIF)V", at = @At("RETURN"), cancellable = true)
    public void render(TileEntity tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage, float p_192854_10_, CallbackInfo info) {
        RenderTileEntityEvent renderTileEntityEvent = new RenderTileEntityEvent(tileEntityIn, x, y, z, partialTicks, destroyStage, p_192854_10_, batchBuffer);
        Cosmos.EVENT_BUS.post(renderTileEntityEvent);

        if (renderTileEntityEvent.isCanceled()) {
            info.cancel();
        }
    }
}
