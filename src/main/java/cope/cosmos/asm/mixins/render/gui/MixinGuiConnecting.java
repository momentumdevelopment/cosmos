package cope.cosmos.asm.mixins.render.gui;

import net.minecraft.client.multiplayer.GuiConnecting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiConnecting.class)
public class MixinGuiConnecting {

    @Inject(method = "connect", at = @At("RETURN"))
    public void onConnect(String ip, int port, CallbackInfo info) {
        // Client client = new Client(ip, port);
    }
}
