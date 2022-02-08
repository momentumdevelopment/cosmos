package cope.cosmos.asm.mixins.render.item;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.render.player.RenderHeldItemAlphaEvent;
import net.minecraft.client.renderer.RenderItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(RenderItem.class)
public class MixinRenderItem {
    @ModifyArg(method = "renderItemModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V"), index = 3)
    public float itemRenderAlphaHook(float a) {
        RenderHeldItemAlphaEvent event = new RenderHeldItemAlphaEvent(a);
        Cosmos.EVENT_BUS.post(event);
        return event.getAlpha();
    }
}
