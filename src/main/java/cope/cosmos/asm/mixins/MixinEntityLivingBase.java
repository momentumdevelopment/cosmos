package cope.cosmos.asm.mixins;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.PotionEvent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityLivingBase.class)
public class MixinEntityLivingBase {

    @Inject(method = "addPotionEffect", at = @At("HEAD"), cancellable = true)
    public void addPotionEffect(PotionEffect potioneffectIn, CallbackInfo info) {
        PotionEvent potionEvent = new PotionEvent(potioneffectIn);
        Cosmos.EVENT_BUS.post(potionEvent);

        if (potionEvent.isCanceled()) {
            info.cancel();
        }
    }
}
