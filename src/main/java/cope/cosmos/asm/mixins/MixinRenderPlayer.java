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
        if (getCosmos().getRotationManager().getClientRotation().isValid() && mc.player.equals(entity)) {
            prevRenderHeadYaw = entity.prevRotationYawHead;
            prevRenderPitch = entity.prevRotationPitch;
            renderPitch = entity.rotationPitch;
            renderYaw = entity.rotationYaw;
            renderHeadYaw = entity.rotationYawHead;
            prevPrevRenderYawOffset = entity.prevRenderYawOffset;
            prevRenderYawOffset = entity.renderYawOffset;

            entity.rotationYaw = getCosmos().getRotationManager().getClientRotation().getYaw();
            entity.rotationYawHead = getCosmos().getRotationManager().getClientRotation().getYaw();
            entity.prevRotationYawHead = getCosmos().getRotationManager().getClientRotation().getYaw();
            entity.prevRenderYawOffset = getCosmos().getRotationManager().getClientRotation().getYaw();
            entity.renderYawOffset = getCosmos().getRotationManager().getClientRotation().getYaw();
            entity.rotationPitch = getCosmos().getRotationManager().getClientRotation().getPitch();
            entity.prevRotationPitch = getCosmos().getRotationManager().getClientRotation().getPitch();
        }
    }

    @Inject(method = "doRender", at = @At("RETURN"))
    private void doRenderPost(AbstractClientPlayer entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo info) {
        if (getCosmos().getRotationManager().getClientRotation().isValid() && mc.player.equals(entity)) {
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
