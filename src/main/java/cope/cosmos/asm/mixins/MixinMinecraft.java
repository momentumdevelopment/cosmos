package cope.cosmos.asm.mixins;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.DualInteractEvent;
import cope.cosmos.util.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("unused")
@Mixin(Minecraft.class)
public class MixinMinecraft implements Wrapper {

    @Redirect(method = "sendClickBlockToController", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isHandActive()Z"))
    public boolean isHandActive(EntityPlayerSP entityPlayerSP) {
        DualInteractEvent dualInteractEvent = new DualInteractEvent();
        Cosmos.EVENT_BUS.post(dualInteractEvent);

        if (dualInteractEvent.isCanceled()) {
            return false;
        }

        else {
            return entityPlayerSP.isHandActive();
        }
    }

    /*
    @Inject(method = "drawSplashScreen", at = @At("HEAD"), cancellable = true)
    public void drawSplashScreen(TextureManager textureManagerInstance, CallbackInfo info) {
        if (mc == null || mc.getLanguageManager() == null)
            return;

        ProgressManager.drawSplash(textureManagerInstance);
        ProgressManager.setProgress();
        info.cancel();
    }
     */
}
