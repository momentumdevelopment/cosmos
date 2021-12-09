package cope.cosmos.asm.mixins;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.RenderAdvancementEvent;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(GuiToast.class)
public class MixinGuiToast {

    @Inject(method = "drawToast", at = @At("HEAD"), cancellable = true)
    public void drawAdvancement(ScaledResolution resolution, CallbackInfo info) {
        RenderAdvancementEvent renderAdvancementEvent = new RenderAdvancementEvent();
        Cosmos.EVENT_BUS.dispatch(renderAdvancementEvent);

        if (renderAdvancementEvent.isCanceled()) {
            info.cancel();
        }
    }
}
