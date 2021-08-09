package cope.cosmos.asm.mixins;

import cope.cosmos.client.events.CrystalAttackEvent;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(EntityEnderCrystal.class)
public class MixinEntityEnderCrystal extends EntityEnderCrystal {
    public MixinEntityEnderCrystal(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "attackEntityFrom", at = @At("RETURN"), cancellable = true)
    public void attackEntityFrom(DamageSource source, float amount, CallbackInfo info) {
        CrystalAttackEvent crystalAttackEvent = new CrystalAttackEvent(source);
        MinecraftForge.EVENT_BUS.post(crystalAttackEvent);

        if (crystalAttackEvent.isCanceled())
            info.cancel();
    }
}
