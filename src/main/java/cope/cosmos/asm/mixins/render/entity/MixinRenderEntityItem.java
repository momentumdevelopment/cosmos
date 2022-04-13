package cope.cosmos.asm.mixins.render.entity;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.render.entity.RenderEntityItemEvent;
import cope.cosmos.client.events.render.entity.RenderItemEvent;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.entity.item.EntityItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderEntityItem.class)
public class MixinRenderEntityItem {

    @Shadow
    @Final
    private RenderItem itemRenderer;

    @Inject(method = "doRender", at = @At("HEAD"), cancellable = true)
    public void onDoRenderHead(EntityItem entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo info) {
        RenderItemEvent renderItemEvent = new RenderItemEvent(entity, x, y, z, entityYaw, partialTicks);
        Cosmos.EVENT_BUS.post(renderItemEvent);

        if (renderItemEvent.isCanceled()) {
            info.cancel();
        }
    }

    @Inject(method = "doRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderItem;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/IBakedModel;)V"), cancellable = true)
    public void onDoRender(EntityItem entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo info) {
        RenderEntityItemEvent renderEntityItemEvent = new RenderEntityItemEvent(itemRenderer, entity, x, y, z, entityYaw, partialTicks);
        Cosmos.EVENT_BUS.post(renderEntityItemEvent);

        if (renderEntityItemEvent.isCanceled()) {
            info.cancel();
        }
    }
}
