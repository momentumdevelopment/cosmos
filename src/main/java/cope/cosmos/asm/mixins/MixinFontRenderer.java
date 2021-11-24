package cope.cosmos.asm.mixins;

import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;

@SuppressWarnings("unused")
@Mixin(FontRenderer.class)
public class MixinFontRenderer {

    /*
    @Inject(method = "drawString(Ljava/lang/String;FFIZ)I", at = @At(value="HEAD"), cancellable = true)
    public void renderStringHook(String text, float x, float y, int color, boolean dropShadow, CallbackInfoReturnable<Integer> info) {
        if (Font.INSTANCE.isEnabled() && Font.vanilla.getValue())
            info.setReturnValue(FontUtil.getFontString(text, x, y, color));
    }
     */
}
