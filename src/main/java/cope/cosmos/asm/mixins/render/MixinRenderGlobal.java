package cope.cosmos.asm.mixins.render;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.render.player.RenderSelectionBoxEvent;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {

    @Inject(method = "drawSelectionBox", at = @At("HEAD"), cancellable = true)
    public void drawSelectionBox(EntityPlayer player, RayTraceResult movingObjectPositionIn, int execute, float partialTicks, CallbackInfo info) {
        RenderSelectionBoxEvent renderSelectionBoxEvent = new RenderSelectionBoxEvent();
        Cosmos.EVENT_BUS.post(renderSelectionBoxEvent);

        if (renderSelectionBoxEvent.isCanceled()) {
            info.cancel();
        }
    }
}
