package cope.cosmos.asm.mixins;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.input.MiddleClickEvent;
import cope.cosmos.client.events.render.gui.GuiScreenClosedEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Shadow
    @Nullable
    public GuiScreen currentScreen;

    @Inject(method = "displayGuiScreen", at = @At("HEAD"), cancellable = true)
    public void displayGuiScreen(GuiScreen scr, CallbackInfo info) {
        if (scr == null) {
            Cosmos.EVENT_BUS.post(new GuiScreenClosedEvent(currentScreen));
        }
    }

    @Inject(method = "middleClickMouse", at = @At("HEAD"), cancellable = true)
    public void middleClickMouse(CallbackInfo info) {
        MiddleClickEvent middleClickEvent = new MiddleClickEvent();
        Cosmos.EVENT_BUS.post(middleClickEvent);

        if (middleClickEvent.isCanceled()) {
            info.cancel();
        }
    }
}
