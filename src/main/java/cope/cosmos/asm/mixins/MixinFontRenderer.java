package cope.cosmos.asm.mixins;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.RenderFontEvent;
import cope.cosmos.util.client.ColorUtil;
import cope.cosmos.util.render.FontUtil;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Mixin(FontRenderer.class)
public class MixinFontRenderer {

    @Inject(method = "drawString(Ljava/lang/String;FFIZ)I", at = @At("HEAD"), cancellable = true)
    public void renderString(String text, float x, float y, int color, boolean dropShadow, CallbackInfoReturnable<Integer> info) {
        RenderFontEvent renderFontEvent = new RenderFontEvent();
        Cosmos.EVENT_BUS.dispatch(renderFontEvent);

        // formatted values
        String textFormatted = text;
        int colorFormatted = color;

        // reformat based on prefix
        if (text.contains("<Cosmos>")) {
            colorFormatted = ColorUtil.getPrimaryColor().getRGB();
            textFormatted = text.replaceAll(ChatFormatting.PREFIX_CODE + ChatFormatting.RESET.toString(), ChatFormatting.PREFIX_CODE + ChatFormatting.WHITE.toString());
        }

        if (renderFontEvent.isCanceled()) {
            info.setReturnValue(FontUtil.getFontString(textFormatted, x, y, colorFormatted));
        }
    }

    @Inject(method = "getStringWidth", at = @At("HEAD"), cancellable = true)
    public void getWidth(String text, CallbackInfoReturnable<Integer> info) {
        RenderFontEvent renderFontEvent = new RenderFontEvent();
        Cosmos.EVENT_BUS.dispatch(renderFontEvent);

        if (renderFontEvent.isCanceled()) {
            info.setReturnValue(FontUtil.getStringWidth(text));
        }
    }
}
