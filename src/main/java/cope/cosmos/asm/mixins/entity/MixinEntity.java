package cope.cosmos.asm.mixins.entity;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.entity.hitbox.EntityHitboxSizeEvent;
import cope.cosmos.client.events.entity.player.PlayerTurnEvent;
import cope.cosmos.client.events.motion.movement.StepEvent;
import cope.cosmos.util.Wrapper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity implements Wrapper {

    @Shadow
    public float stepHeight;

    @Shadow
    public abstract AxisAlignedBB getEntityBoundingBox();

    // Credit: auto - we were working on this yesterday and he gave me this injection
    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endSection()V", shift = Shift.BEFORE, ordinal = 0))
    public void onMove(MoverType type, double x, double y, double z, CallbackInfo info) {
        if (((Entity) (Object) this).equals(mc.player)) {
            StepEvent event = new StepEvent(getEntityBoundingBox(), stepHeight);
            Cosmos.EVENT_BUS.post(event);

            if (event.isCanceled()) {
                stepHeight = event.getHeight();
            }
        }
    }

    @Inject(method = "getCollisionBorderSize", at = @At("HEAD"), cancellable = true)
    public void onGetCollisionBorderSize(CallbackInfoReturnable<Float> info) {
        EntityHitboxSizeEvent entityHitboxSizeEvent = new EntityHitboxSizeEvent();
        Cosmos.EVENT_BUS.post(entityHitboxSizeEvent);

        if (entityHitboxSizeEvent.isCanceled()) {
            info.cancel();
            info.setReturnValue(entityHitboxSizeEvent.getHitboxSize());
        }
    }

    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    public void onTurn(float yaw, float pitch, CallbackInfo info) {
        if (((Entity) (Object) this).equals(mc.player)) {
            PlayerTurnEvent event = new PlayerTurnEvent(yaw, pitch);
            Cosmos.EVENT_BUS.post(event);

            if (event.isCanceled()) {
                info.cancel();
            }
        }
    }
}
