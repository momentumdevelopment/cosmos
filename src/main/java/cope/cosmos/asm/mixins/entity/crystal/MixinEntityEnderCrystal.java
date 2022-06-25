package cope.cosmos.asm.mixins.entity.crystal;

import cope.cosmos.client.events.render.entity.CrystalUpdateEvent;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityEnderCrystal.class)
public class MixinEntityEnderCrystal {

    @Inject(method = "onUpdate", at = @At("HEAD"), cancellable = true)
    public void onOnUpdate(CallbackInfo info) {
        CrystalUpdateEvent crystalUpdateEvent = new CrystalUpdateEvent();
        MinecraftForge.EVENT_BUS.post(crystalUpdateEvent);

        if (crystalUpdateEvent.isCanceled()) {
            info.cancel();
        }
    }
}
