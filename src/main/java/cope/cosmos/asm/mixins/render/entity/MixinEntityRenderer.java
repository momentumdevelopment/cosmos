package cope.cosmos.asm.mixins.render.entity;

import com.google.common.base.Predicate;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.CameraClipEvent;
import cope.cosmos.client.events.HitboxInteractEvent;
import cope.cosmos.client.events.HurtCameraEvent;
import cope.cosmos.client.events.RenderWorldEvent;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

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

    @Redirect(method = "getMouseOver", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getEntitiesInAABBexcluding(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"))
    public List<Entity> getEntitiesInAABBexcluding(WorldClient worldClient, Entity entityIn, AxisAlignedBB axisAlignedBB, Predicate<? super Entity> predicate) {
        HitboxInteractEvent hitboxInteractEvent = new HitboxInteractEvent();
        Cosmos.EVENT_BUS.post(hitboxInteractEvent);

        if (hitboxInteractEvent.isCanceled()) {
            return new ArrayList<>();
        }

        else {
            return worldClient.getEntitiesInAABBexcluding(entityIn, axisAlignedBB, predicate);
        }
    }

    @ModifyVariable(method = "orientCamera", at = @At("STORE"), ordinal = 3)
    private double orientCameraX(double range) {
        CameraClipEvent cameraClipEvent = new CameraClipEvent(range);
        Cosmos.EVENT_BUS.post(cameraClipEvent);

        return cameraClipEvent.getDistance();
    }

    @ModifyVariable(method = "orientCamera", at = @At("STORE"), ordinal = 7)
    private double orientCameraZ(double range) {
        CameraClipEvent cameraClipEvent = new CameraClipEvent(range);
        Cosmos.EVENT_BUS.post(cameraClipEvent);

        return cameraClipEvent.getDistance();
    }
}
