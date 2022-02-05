package cope.cosmos.asm.mixins.render.item;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.RenderHeldItemEvent;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.util.EnumHandSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class MixinItemRenderer {
    @Inject(method = "transformFirstPerson", at = @At("HEAD"))
    public void onTransformFirstPerson(EnumHandSide handSide, float p_187459_2_, CallbackInfo info) {
        Cosmos.EVENT_BUS.post(new RenderHeldItemEvent());
    }

    @Inject(method = "transformSideFirstPerson", at = @At("HEAD"))
    public void onTransformSideFirstPerson(EnumHandSide handSide, float p_187459_2_, CallbackInfo info) {
        Cosmos.EVENT_BUS.post(new RenderHeldItemEvent());
    }
}
