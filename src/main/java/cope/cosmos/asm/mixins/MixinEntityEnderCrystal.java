package cope.cosmos.asm.mixins;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.CrystalAttackEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Mixin(EntityEnderCrystal.class)
public abstract class MixinEntityEnderCrystal extends Entity {
    public MixinEntityEnderCrystal(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "attackEntityFrom", at = @At("RETURN"), cancellable = true)
    public void attackEntityFrom(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        CrystalAttackEvent crystalAttackEvent = new CrystalAttackEvent(source);
        Cosmos.EVENT_BUS.post(crystalAttackEvent);

        if (crystalAttackEvent.isCanceled()) {
            info.cancel();
        }
    }
}
