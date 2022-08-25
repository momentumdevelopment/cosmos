package cope.cosmos.asm.mixins.render.gui;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.render.gui.RenderChatTextEvent;
import net.minecraft.client.gui.GuiTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiTextField.class)
public class MixinGuiTextField {

    @Shadow
    public int x;

    @Shadow
    public int y;

    @Shadow
    public int width;

    @Shadow
    public int height;

    @Shadow
    private boolean enableBackgroundDrawing;

    @Inject(method = "drawTextBox", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiTextField;getEnableBackgroundDrawing()Z", shift = At.Shift.BEFORE), cancellable = true)
    public void onDrawTextBox(CallbackInfo info) {
        RenderChatTextEvent renderChatTextEvent = new RenderChatTextEvent(x, enableBackgroundDrawing ? y + (height - 8) / 2 : y);
        Cosmos.EVENT_BUS.post(renderChatTextEvent);
    }
}
