package cope.cosmos.asm.mixins;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.EntityHitboxSizeEvent;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Mixin(Entity.class)
public class MixinEntity {

    @Inject(method = "getCollisionBorderSize", at = @At("HEAD"), cancellable = true)
    public void getCollisionBorderSize(CallbackInfoReturnable<Float> info) {
        EntityHitboxSizeEvent entityHitboxSizeEvent = new EntityHitboxSizeEvent();
        Cosmos.EVENT_BUS.post(entityHitboxSizeEvent);

        if (entityHitboxSizeEvent.isCanceled()) {
            info.cancel();
            info.setReturnValue(entityHitboxSizeEvent.getHitboxSize());
        }
    }
}
