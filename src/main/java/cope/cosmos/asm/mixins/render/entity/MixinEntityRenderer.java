package cope.cosmos.asm.mixins.render.entity;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.render.other.CameraClipEvent;
import cope.cosmos.client.events.render.player.CrosshairBobEvent;
import cope.cosmos.client.events.render.player.HurtCameraEvent;
import cope.cosmos.client.events.render.player.RenderItemActivationEvent;
import cope.cosmos.client.events.render.world.RenderFogEvent;
import cope.cosmos.client.events.render.world.RenderWorldEvent;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    @Shadow
    private ItemStack itemActivationItem;

    @Inject(method = "renderWorld", at = @At("RETURN"))
    private void renderWorld(CallbackInfo info) {
        RenderWorldEvent renderWorldEvent = new RenderWorldEvent();
        Cosmos.EVENT_BUS.post(renderWorldEvent);
    }

    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    public void hurtCameraEffect(float ticks, CallbackInfo info) {
        HurtCameraEvent hurtCameraEvent = new HurtCameraEvent();
        Cosmos.EVENT_BUS.post(hurtCameraEvent);

        if (hurtCameraEvent.isCanceled()) {
            info.cancel();
        }
    }

    /*
    @Redirect(method = "getMouseOver", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getEntitiesInAABBexcluding(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"))
    public List<Entity> onGetEntitiesInAABBexcluding(WorldClient worldClient, Entity entityIn, AxisAlignedBB axisAlignedBB, Predicate<? super Entity> predicate) {
        HitboxInteractEvent hitboxInteractEvent = new HitboxInteractEvent();
        Cosmos.EVENT_BUS.post(hitboxInteractEvent);

        if (hitboxInteractEvent.isCanceled()) {
            return new ArrayList<>();
        }

        else {
            return worldClient.getEntitiesInAABBexcluding(entityIn, axisAlignedBB, predicate);
        }
    }
    */

    @ModifyVariable(method = "orientCamera", at = @At("STORE"), ordinal = 3)
    private double onOrientCameraX(double range) {
        CameraClipEvent cameraClipEvent = new CameraClipEvent(range);
        Cosmos.EVENT_BUS.post(cameraClipEvent);

        return cameraClipEvent.getDistance();
    }

    @ModifyVariable(method = "orientCamera", at = @At("STORE"), ordinal = 7)
    private double onOrientCameraZ(double range) {
        CameraClipEvent cameraClipEvent = new CameraClipEvent(range);
        Cosmos.EVENT_BUS.post(cameraClipEvent);

        return cameraClipEvent.getDistance();
    }

    @Inject(method = "renderItemActivation", at = @At("HEAD"), cancellable = true)
    public void onRenderItemActivation(CallbackInfo info) {
        RenderItemActivationEvent renderItemActivationEvent = new RenderItemActivationEvent();
        Cosmos.EVENT_BUS.post(renderItemActivationEvent);

        if (itemActivationItem != null && itemActivationItem.getItem().equals(Items.TOTEM_OF_UNDYING)) {
            if (renderItemActivationEvent.isCanceled()) {
                info.cancel();
            }
        }
    }

    @Shadow
    protected abstract void applyBobbing(float partialTicks);

    @Redirect(method = "setupCameraTransform", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EntityRenderer;applyBobbing(F)V", remap = false))
    public void onSetupCameraTransform(EntityRenderer instance, float f) {
        CrosshairBobEvent crosshairBobEvent = new CrosshairBobEvent();
        Cosmos.EVENT_BUS.post(crosshairBobEvent);

        // Apply bobbing if we haven't cancelled the event
        if (!crosshairBobEvent.isCanceled()) {
            applyBobbing(f);
        }
    }
}
