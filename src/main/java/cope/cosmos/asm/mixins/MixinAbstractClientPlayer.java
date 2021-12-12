package cope.cosmos.asm.mixins;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.ModifyFOVEvent;
import cope.cosmos.client.events.SkinLocationEvent;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Mixin(AbstractClientPlayer.class)
public class MixinAbstractClientPlayer {

    @Inject(method = "getLocationSkin()Lnet/minecraft/util/ResourceLocation;", at = @At("HEAD"), cancellable = true)
    public void getLocationSkin(CallbackInfoReturnable<ResourceLocation> info) {
        SkinLocationEvent skinLocationEvent = new SkinLocationEvent();
        Cosmos.EVENT_BUS.post(skinLocationEvent);
    }

    @Inject(method = "getFovModifier", at = @At("HEAD"), cancellable = true)
    public void getFOVModifier(CallbackInfoReturnable<Float> info) {
        ModifyFOVEvent modifyFOVEvent = new ModifyFOVEvent();
        Cosmos.EVENT_BUS.post(modifyFOVEvent);

        if (modifyFOVEvent.isCanceled()) {
            info.cancel();
            info.setReturnValue(1.0F);
        }
    }
}
