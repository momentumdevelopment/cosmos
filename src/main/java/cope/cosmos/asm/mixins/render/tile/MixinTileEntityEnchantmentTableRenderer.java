package cope.cosmos.asm.mixins.render.tile;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.render.other.RenderEnchantmentTableBookEvent;
import net.minecraft.client.renderer.tileentity.TileEntityEnchantmentTableRenderer;
import net.minecraft.tileentity.TileEntityEnchantmentTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(TileEntityEnchantmentTableRenderer.class)
public class MixinTileEntityEnchantmentTableRenderer {

    @Inject(method = "render", at = @At(value = "INVOKE"), cancellable = true)
    private void renderEnchantingTableBook(TileEntityEnchantmentTable tileEntityEnchantmentTable, double x, double y, double z, float partialTicks, int destroyStage, float alpha, CallbackInfo info) {
        RenderEnchantmentTableBookEvent renderEnchantmentTableBookEvent = new RenderEnchantmentTableBookEvent();
        Cosmos.EVENT_BUS.post(renderEnchantmentTableBookEvent);

        if (renderEnchantmentTableBookEvent.isCanceled()) {
            info.cancel();
        }
    }
}
