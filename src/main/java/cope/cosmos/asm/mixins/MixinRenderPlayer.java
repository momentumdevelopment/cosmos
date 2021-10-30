package cope.cosmos.asm.mixins;

import cope.cosmos.util.Wrapper;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(RenderPlayer.class)
public class MixinRenderPlayer implements Wrapper {

    private float renderPitch, renderYaw, renderHeadYaw, prevRenderHeadYaw, prevRenderPitch, prevRenderYawOffset, prevPrevRenderYawOffset;

    @Inject(method = "doRender", at = @At("HEAD"))
    private void doRenderPre(AbstractClientPlayer entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo info) {
        if (getCosmos().getRotationManager().getServerRotation().isValid() && mc.player.equals(entity)) {
            prevRenderHeadYaw = entity.prevRotationYawHead;
            prevRenderPitch = entity.prevRotationPitch;
            renderPitch = entity.rotationPitch;
            renderYaw = entity.rotationYaw;
            renderHeadYaw = entity.rotationYawHead;
            prevPrevRenderYawOffset = entity.prevRenderYawOffset;
            prevRenderYawOffset = entity.renderYawOffset;

            entity.rotationYaw = getCosmos().getRotationManager().getServerRotation().getYaw();
            entity.rotationYawHead = getCosmos().getRotationManager().getServerRotation().getYaw();
            entity.prevRotationYawHead = getCosmos().getRotationManager().getServerRotation().getYaw();
            entity.prevRenderYawOffset = getCosmos().getRotationManager().getServerRotation().getYaw();
            entity.renderYawOffset = getCosmos().getRotationManager().getServerRotation().getYaw();
            entity.rotationPitch = getCosmos().getRotationManager().getServerRotation().getPitch();
            entity.prevRotationPitch = getCosmos().getRotationManager().getServerRotation().getPitch();
        }
    }

    @Inject(method = "doRender", at = @At("RETURN"))
    private void doRenderPost(AbstractClientPlayer entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo info) {
        if (getCosmos().getRotationManager().getServerRotation().isValid() && mc.player.equals(entity)) {
            entity.rotationPitch = renderPitch;
            entity.rotationYaw = renderYaw;
            entity.rotationYawHead = renderHeadYaw;
            entity.prevRotationYawHead = prevRenderHeadYaw;
            entity.prevRotationPitch = prevRenderPitch;
            entity.renderYawOffset = prevRenderYawOffset;
            entity.prevRenderYawOffset = prevPrevRenderYawOffset;
        }
    }
}
