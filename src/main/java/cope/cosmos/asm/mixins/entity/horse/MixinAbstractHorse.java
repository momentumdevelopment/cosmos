package cope.cosmos.asm.mixins.entity.horse;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.entity.horse.HorseSaddledEvent;
import cope.cosmos.client.events.entity.horse.HorseSteerEvent;
import net.minecraft.entity.passive.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorse.class)
public class MixinAbstractHorse {

    @Inject(method = "canBeSteered", at = @At("HEAD"), cancellable = true)
    public void canBeSteered(CallbackInfoReturnable<Boolean> info) {
        HorseSteerEvent horseSteerEvent = new HorseSteerEvent();
        Cosmos.EVENT_BUS.post(horseSteerEvent);

        if (horseSteerEvent.isCanceled()) {
            info.cancel();
            info.setReturnValue(true);
        }
    }

    @Inject(method = "isHorseSaddled", at = @At("HEAD"), cancellable = true)
    public void isHorseSaddled(CallbackInfoReturnable<Boolean> info) {
        HorseSaddledEvent horseSaddledEvent = new HorseSaddledEvent();
        Cosmos.EVENT_BUS.post(horseSaddledEvent);

        if (horseSaddledEvent.isCanceled()) {
            info.cancel();
            info.setReturnValue(true);
        }
    }
}
