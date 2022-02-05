package cope.cosmos.asm.mixins.render.entity;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.RenderItemEvent;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.entity.item.EntityItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderEntityItem.class)
public class MixinRenderEntityItem {

    @Inject(method = "doRender", at = @At("HEAD"), cancellable = true)
    public void doRender(EntityItem entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo info) {
        RenderItemEvent renderItemEvent = new RenderItemEvent(entity, x, y, z, entityYaw, partialTicks);
        Cosmos.EVENT_BUS.post(renderItemEvent);

        if (renderItemEvent.isCanceled()) {
            info.cancel();
        }
    }
}
