package cope.cosmos.asm.mixins.render.gui;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.render.gui.RenderPotionHUDEvent;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngame.class)
public class MixinGuiInGame {

    @Inject(method = "renderPotionEffects", at = @At("HEAD"), cancellable = true)
    protected void renderPotionEffectsHUD(ScaledResolution resolution, CallbackInfo info) {
        RenderPotionHUDEvent renderPotionHUDEvent = new RenderPotionHUDEvent();
        Cosmos.EVENT_BUS.post(renderPotionHUDEvent);

        if (renderPotionHUDEvent.isCanceled()) {
            info.cancel();
        }
    }
}
