package cope.cosmos.asm.mixins;

import cope.cosmos.client.features.modules.client.Font;
import cope.cosmos.util.render.FontUtil;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
