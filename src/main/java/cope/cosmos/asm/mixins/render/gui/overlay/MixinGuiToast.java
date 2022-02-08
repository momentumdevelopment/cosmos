package cope.cosmos.asm.mixins.render.gui.overlay;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.render.gui.RenderAdvancementEvent;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.toasts.GuiToast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiToast.class)
public class MixinGuiToast {

    @Inject(method = "drawToast", at = @At("HEAD"), cancellable = true)
    public void drawAdvancement(ScaledResolution resolution, CallbackInfo info) {
        RenderAdvancementEvent renderAdvancementEvent = new RenderAdvancementEvent();
        Cosmos.EVENT_BUS.post(renderAdvancementEvent);

        if (renderAdvancementEvent.isCanceled()) {
            info.cancel();
        }
    }
}
