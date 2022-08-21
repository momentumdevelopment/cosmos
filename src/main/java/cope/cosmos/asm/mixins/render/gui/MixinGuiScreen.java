package cope.cosmos.asm.mixins.render.gui;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.render.gui.tooltip.RenderTooltipEvent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiScreen.class)
public class MixinGuiScreen {

    @Inject(method = "renderToolTip", at = @At("HEAD"), cancellable = true)
    public void onRenderToolTip(ItemStack stack, int x, int y, CallbackInfo info) {
        RenderTooltipEvent renderTooltipEvent = new RenderTooltipEvent(stack, x, y);
        Cosmos.EVENT_BUS.post(renderTooltipEvent);

        if (renderTooltipEvent.isCanceled()) {
            info.cancel();
        }
    }
}
