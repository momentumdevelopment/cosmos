package cope.cosmos.asm.mixins.input;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.input.KeyDownEvent;
import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBinding.class)
public class MixinKeyBinding {

    @Shadow
    private boolean pressed;

    @Shadow
    private int keyCode;

    @Inject(method = "isKeyDown", at = @At("HEAD"), cancellable = true)
    public void onIsKeyDown(CallbackInfoReturnable<Boolean> info) {
        KeyDownEvent keyDownEvent = new KeyDownEvent(keyCode, pressed);
        Cosmos.EVENT_BUS.post(keyDownEvent);

        if (keyDownEvent.isCanceled()) {
            info.setReturnValue(keyDownEvent.isPressed());
        }
    }
}
