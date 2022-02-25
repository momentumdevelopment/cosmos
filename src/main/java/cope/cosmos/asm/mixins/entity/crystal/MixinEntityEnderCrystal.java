package cope.cosmos.asm.mixins.entity.crystal;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.combat.CrystalAttackEvent;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityEnderCrystal.class)
public class MixinEntityEnderCrystal {

    @Inject(method = "attackEntityFrom", at = @At("RETURN"), cancellable = true)
    public void attackEntityFrom(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        CrystalAttackEvent crystalAttackEvent = new CrystalAttackEvent(source);
        Cosmos.EVENT_BUS.post(crystalAttackEvent);

        if (crystalAttackEvent.isCanceled()) {
            info.cancel();
        }
    }
}
