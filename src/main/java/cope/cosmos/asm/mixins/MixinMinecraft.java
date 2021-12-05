package cope.cosmos.asm.mixins;

import cope.cosmos.client.managment.managers.ProgressManager;
import cope.cosmos.utility.IUtility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(Minecraft.class)
public class MixinMinecraft implements IUtility {

    @Inject(method = "drawSplashScreen", at = @At("HEAD"), cancellable = true)
    public void drawSplashScreen(TextureManager textureManagerInstance, CallbackInfo info) {
        if (mc == null || mc.getLanguageManager() == null)
            return;

        ProgressManager.drawSplash(textureManagerInstance);
        ProgressManager.setProgress();
        info.cancel();
    }
}
