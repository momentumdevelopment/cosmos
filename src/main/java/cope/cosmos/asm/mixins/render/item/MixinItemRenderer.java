package cope.cosmos.asm.mixins.render.item;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.render.player.RenderEatingEvent;
import cope.cosmos.client.events.render.player.RenderHeldItemEvent;
import cope.cosmos.util.Wrapper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class MixinItemRenderer implements Wrapper {

    @Inject(method = "transformEatFirstPerson", at = @At("HEAD"), cancellable = true)
    public void onTransformEat(float p_187454_1_, EnumHandSide handSide, ItemStack stack, CallbackInfo info) {
        RenderEatingEvent renderEatingEvent = new RenderEatingEvent(handSide);
        Cosmos.EVENT_BUS.post(renderEatingEvent);

        if (renderEatingEvent.isCanceled()) {
            info.cancel();

            float f = (float) mc.player.getItemInUseCount() - p_187454_1_ + 1;
            float f1 = f / (float) stack.getMaxItemUseDuration();

            if (f1 < 0.8F) {
                float f2 = MathHelper.abs(MathHelper.cos(f / 4 * (float)Math.PI) * 0.1F);
                GlStateManager.translate(0, f2, 0);
            }

            float f3 = 1 - (float) Math.pow(f1, 27);
            int i = handSide.equals(EnumHandSide.RIGHT) ? 1 : -1;
            GlStateManager.translate((f3 * 0.6F * (float) i) * renderEatingEvent.getScale(), (f3 * -0.5F) * renderEatingEvent.getScale(), (f3 * 0) * renderEatingEvent.getScale());
            GlStateManager.rotate((float) i * f3 * 90, 0, 1, 0);
            GlStateManager.rotate(f3 * 10, 1, 0, 0);
            GlStateManager.rotate((float) i * f3 * 30, 0, 0, 1);
        }
    }

    @Inject(method = "transformFirstPerson", at = @At("HEAD"))
    public void onTransformFirstPersonPre(EnumHandSide handSide, float p_187459_2_, CallbackInfo info) {
        RenderHeldItemEvent.Pre renderHeldItemEventPre = new RenderHeldItemEvent.Pre(handSide);
        Cosmos.EVENT_BUS.post(renderHeldItemEventPre);
    }

    @Inject(method = "transformFirstPerson", at = @At("HEAD"))
    public void onTransformFirstPersonPost(EnumHandSide handSide, float p_187459_2_, CallbackInfo info) {
        RenderHeldItemEvent.Post renderHeldItemEventPost = new RenderHeldItemEvent.Post(handSide);
        Cosmos.EVENT_BUS.post(renderHeldItemEventPost);
    }

    @Inject(method = "transformSideFirstPerson", at = @At("HEAD"), cancellable = true)
    public void onTransformSideFirstPerson(EnumHandSide handSide, float p_187459_2_, CallbackInfo info) {
        RenderHeldItemEvent.Pre renderHeldItemEventPre = new RenderHeldItemEvent.Pre(handSide);
        Cosmos.EVENT_BUS.post(renderHeldItemEventPre);

        if (renderHeldItemEventPre.isCanceled()) {
            info.cancel();
        }
    }
}
