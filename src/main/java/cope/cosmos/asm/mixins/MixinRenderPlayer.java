package cope.cosmos.asm.mixins;

import cope.cosmos.client.events.RenderNametagEvent;
import cope.cosmos.client.events.RenderRotationsEvent;
import cope.cosmos.utility.IUtility;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(RenderPlayer.class)
public class MixinRenderPlayer implements IUtility {

    private float renderPitch, renderYaw, renderHeadYaw, prevRenderHeadYaw, prevRenderPitch, prevRenderYawOffset, prevPrevRenderYawOffset;

    @Inject(method = "doRender", at = @At("HEAD"))
    private void doRenderPre(AbstractClientPlayer entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo info) {
        if (mc.player.equals(entity)) {
            prevRenderHeadYaw = entity.prevRotationYawHead;
            prevRenderPitch = entity.prevRotationPitch;
            renderPitch = entity.rotationPitch;
            renderYaw = entity.rotationYaw;
            renderHeadYaw = entity.rotationYawHead;
            prevPrevRenderYawOffset = entity.prevRenderYawOffset;
            prevRenderYawOffset = entity.renderYawOffset;

            RenderRotationsEvent renderRotationsEvent = new RenderRotationsEvent();
            MinecraftForge.EVENT_BUS.post(renderRotationsEvent);

            if (renderRotationsEvent.isCanceled()) {
                entity.rotationYaw = renderRotationsEvent.getYaw();
                entity.rotationYawHead = renderRotationsEvent.getYaw();
                entity.prevRotationYawHead = renderRotationsEvent.getYaw();
                entity.prevRenderYawOffset = renderRotationsEvent.getYaw();
                entity.renderYawOffset = renderRotationsEvent.getYaw();
                entity.rotationPitch = renderRotationsEvent.getPitch();
                entity.prevRotationPitch = renderRotationsEvent.getPitch();
            }
        }
    }

    @Inject(method = "doRender", at = @At("RETURN"))
    private void doRenderPost(AbstractClientPlayer entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo info) {
        if (mc.player.equals(entity)) {
            entity.rotationPitch = renderPitch;
            entity.rotationYaw = renderYaw;
            entity.rotationYawHead = renderHeadYaw;
            entity.prevRotationYawHead = prevRenderHeadYaw;
            entity.prevRotationPitch = prevRenderPitch;
            entity.renderYawOffset = prevRenderYawOffset;
            entity.prevRenderYawOffset = prevPrevRenderYawOffset;
        }
    }

    @Inject(method = "renderEntityName", at = @At("HEAD"), cancellable = true)
    public void renderNametag(AbstractClientPlayer entityIn, double x, double y, double z, String name, double distanceSq, CallbackInfo info) {
        RenderNametagEvent renderNametagEvent = new RenderNametagEvent();
        MinecraftForge.EVENT_BUS.post(renderNametagEvent);

        if (renderNametagEvent.isCanceled()) {
            info.cancel();
        }
    }
}
