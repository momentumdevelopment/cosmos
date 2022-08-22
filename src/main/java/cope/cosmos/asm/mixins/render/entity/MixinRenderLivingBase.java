package cope.cosmos.asm.mixins.render.entity;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.render.entity.RenderLivingEntityEvent;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderLivingBase.class)
public abstract class MixinRenderLivingBase {

    @Shadow
    protected ModelBase mainModel;

    @Redirect(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelBase;render(Lnet/minecraft/entity/Entity;FFFFFF)V"))
    private void onRenderModelPreEntityLivingBase(ModelBase modelBase, Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        RenderLivingEntityEvent.RenderLivingEntityPreEvent renderLivingEntityPreEvent = new RenderLivingEntityEvent.RenderLivingEntityPreEvent(mainModel, (EntityLivingBase) entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        Cosmos.EVENT_BUS.post(renderLivingEntityPreEvent);

        if (!renderLivingEntityPreEvent.isCanceled()) {
            modelBase.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }
    }

    @Inject(method = "renderModel", at = @At("RETURN"), cancellable = true)
    private void onRenderModelPost(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, CallbackInfo info) {
        RenderLivingEntityEvent.RenderLivingEntityPostEvent renderLivingEntityPostEvent = new RenderLivingEntityEvent.RenderLivingEntityPostEvent(mainModel, entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
        Cosmos.EVENT_BUS.post(renderLivingEntityPostEvent);

        if (renderLivingEntityPostEvent.isCanceled()) {
            info.cancel();
        }
    }
}

