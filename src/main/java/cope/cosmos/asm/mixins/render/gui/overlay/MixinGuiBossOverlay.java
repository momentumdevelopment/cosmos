package cope.cosmos.asm.mixins.render.gui.overlay;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.render.gui.BossOverlayEvent;
import net.minecraft.client.gui.GuiBossOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(GuiBossOverlay.class)
public class MixinGuiBossOverlay {

    @Inject(method = "renderBossHealth", at = @At("HEAD"), cancellable = true)
    private void renderBossHealth(CallbackInfo info) {
        BossOverlayEvent bossOverlayEvent = new BossOverlayEvent();
        Cosmos.EVENT_BUS.post(bossOverlayEvent);

        if (bossOverlayEvent.isCanceled()) {
            info.cancel();
        }
    }
}
