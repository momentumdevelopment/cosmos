package cope.cosmos.asm.mixins.render.gui;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.render.gui.RenderFontEvent;
import cope.cosmos.util.chat.ChatUtil;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.string.ColorUtil;
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
        if (text.contains(ChatUtil.getPrefix())) {
            String[] words = text.split(" ");

            // exclude prefix
            StringBuilder remaining = new StringBuilder();

            // compile
            for (String word : words) {
                if (!word.equalsIgnoreCase("<Cosmos>")) {
                    remaining.append(word).append(" ");
                }
            }


            // draw the rest of the text
            info.setReturnValue(FontUtil.getFontString(ChatUtil.getPrefix(), x, y, ColorUtil.getPrimaryColor().getRGB()) + FontUtil.getFontString(remaining.toString(), x, y, color));
        }
         */

        if (renderFontEvent.isCanceled()) {
            info.setReturnValue(FontUtil.drawStringWithShadow(text, x, y, color));
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
