package cope.cosmos.asm.mixins.render.item;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.render.player.RenderEatingEvent;
import cope.cosmos.client.events.render.player.RenderHeldItemEvent;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class MixinItemRenderer {

    @Inject(method = "transformEatFirstPerson", at = @At("HEAD"), cancellable = true)
    public void onTransformEat(float p_187454_1_, EnumHandSide handSide, ItemStack stack, CallbackInfo info) {
        RenderEatingEvent renderEatingEvent = new RenderEatingEvent();
        Cosmos.EVENT_BUS.post(renderEatingEvent);

        if (renderEatingEvent.isCanceled()) {
            info.cancel();
        }
    }

    @Inject(method = "transformFirstPerson", at = @At("HEAD"))
    public void onTransformFirstPersonPre(EnumHandSide handSide, float p_187459_2_, CallbackInfo info) {
        Cosmos.EVENT_BUS.post(new RenderHeldItemEvent.Pre(handSide));
    }

    @Inject(method = "transformFirstPerson", at = @At("HEAD"))
    public void onTransformFirstPersonPost(EnumHandSide handSide, float p_187459_2_, CallbackInfo info) {
        Cosmos.EVENT_BUS.post(new RenderHeldItemEvent.Post(handSide));
    }

    @Inject(method = "transformSideFirstPerson", at = @At("HEAD"), cancellable = true)
    public void onTransformSideFirstPerson(EnumHandSide handSide, float p_187459_2_, CallbackInfo info) {
        RenderHeldItemEvent.Pre renderHeldItemEvent = new RenderHeldItemEvent.Pre(handSide);
        Cosmos.EVENT_BUS.post(renderHeldItemEvent);

        if (renderHeldItemEvent.isCanceled()) {
            info.cancel();
        }
    }

}
