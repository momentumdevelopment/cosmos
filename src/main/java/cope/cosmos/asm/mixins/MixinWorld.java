package cope.cosmos.asm.mixins;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.EntityWorldEvent;
import cope.cosmos.client.events.RenderSkylightEvent;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Mixin(World.class)
public class MixinWorld {

    @Inject(method = "checkLightFor", at = @At("HEAD"), cancellable = true)
    public void checkLightFor(EnumSkyBlock lightType, BlockPos pos, CallbackInfoReturnable<Boolean> info) {
        RenderSkylightEvent renderSkylightEvent = new RenderSkylightEvent();
        Cosmos.EVENT_BUS.post(renderSkylightEvent);

        if (renderSkylightEvent.isCanceled()) {
            info.cancel();
            info.setReturnValue(true);
        }
    }

    @Inject(method = "spawnEntity", at = @At("RETURN"))
    public void spawnEntity(Entity entityIn, CallbackInfoReturnable<Boolean> info) {
        EntityWorldEvent.EntitySpawnEvent entitySpawnEvent = new EntityWorldEvent.EntitySpawnEvent(entityIn);
        Cosmos.EVENT_BUS.post(entitySpawnEvent);
    }

    @Inject(method = "removeEntity", at = @At("HEAD"))
    public void removeEntity(Entity entityIn, CallbackInfo info) {
        EntityWorldEvent.EntityRemoveEvent entityRemoveEvent = new EntityWorldEvent.EntityRemoveEvent(entityIn);
        Cosmos.EVENT_BUS.post(entityRemoveEvent);
    }
}
