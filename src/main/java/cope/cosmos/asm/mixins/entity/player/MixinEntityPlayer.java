package cope.cosmos.asm.mixins.entity.player;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.motion.collision.EntityCollisionEvent;
import cope.cosmos.client.events.motion.movement.TravelEvent;
import cope.cosmos.client.events.block.WaterCollisionEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends EntityLivingBase {
    public MixinEntityPlayer(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    public void travel(float strafe, float vertical, float forward, CallbackInfo info) {
        TravelEvent travelEvent = new TravelEvent(strafe, vertical, forward);
        Cosmos.EVENT_BUS.post(travelEvent);

        if (travelEvent.isCanceled()) {
            move(MoverType.SELF, motionX, motionY, motionZ);
            info.cancel();
        }
    }

    @Inject(method = "applyEntityCollision", at = @At("HEAD"), cancellable = true)
    public void applyEntityCollision(Entity entity, CallbackInfo info) {
        EntityCollisionEvent entityCollisionEvent = new EntityCollisionEvent();
        Cosmos.EVENT_BUS.post(entityCollisionEvent);

        if (entityCollisionEvent.isCanceled()) {
            info.cancel();
        }
    }

    @Inject(method = "isPushedByWater()Z", at = @At("HEAD"), cancellable = true)
    public void isPushedByWater(CallbackInfoReturnable<Boolean> info) {
        WaterCollisionEvent waterCollisionEvent = new WaterCollisionEvent();
        Cosmos.EVENT_BUS.post(waterCollisionEvent);

        if (waterCollisionEvent.isCanceled()) {
            info.cancel();
            info.setReturnValue(false);
        }
    }
}
