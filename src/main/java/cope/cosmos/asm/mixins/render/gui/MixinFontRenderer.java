package cope.cosmos.asm.mixins.render.gui;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.render.gui.RenderFontEvent;
import cope.cosmos.util.render.FontUtil;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FontRenderer.class)
public class MixinFontRenderer {

    @Inject(method = "drawString(Ljava/lang/String;FFIZ)I", at = @At("HEAD"), cancellable = true)
    public void renderString(String text, float x, float y, int color, boolean dropShadow, CallbackInfoReturnable<Integer> info) {
        RenderFontEvent renderFontEvent = new RenderFontEvent();
        Cosmos.EVENT_BUS.post(renderFontEvent);

        /*
        // reformat based on prefix
        if (text.contains("<Cosmos>")) {
            String[] words = text.split(" ");

            // every word that's not the prefix
            StringBuilder splitText = new StringBuilder();
            for (int i = 1; i < words.length; i++) {
                splitText.append(words[i]);
            }

            // draw the rest of the text
            info.setReturnValue(FontUtil.getFontString("<Cosmos> " + ChatFormatting.WHITE + splitText.toString(), x, y, ColorUtil.getPrimaryColor().getRGB()));
        }
         */

        if (renderFontEvent.isCanceled()) {
            info.setReturnValue(FontUtil.getFontString(text, x, y, color));
        }
    }

    @Inject(method = "getStringWidth", at = @At("HEAD"), cancellable = true)
    public void getWidth(String text, CallbackInfoReturnable<Integer> info) {
        RenderFontEvent renderFontEvent = new RenderFontEvent();
        Cosmos.EVENT_BUS.post(renderFontEvent);

        if (renderFontEvent.isCanceled()) {
            info.setReturnValue(FontUtil.getStringWidth(text));
        }
    }
}
