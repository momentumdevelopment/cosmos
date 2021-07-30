package cope.cosmos.asm.mixins;

import com.mojang.authlib.GameProfile;
import cope.cosmos.client.events.LivingUpdateEvent;
import cope.cosmos.client.events.MotionEvent;
import cope.cosmos.client.events.MotionUpdateEvent;
import cope.cosmos.util.Wrapper;
import cope.cosmos.util.player.RotationUtil;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.MoverType;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP extends AbstractClientPlayer implements Wrapper {
    public MixinEntityPlayerSP(World worldIn, GameProfile playerProfile) {
        super(worldIn, playerProfile);
    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;move(Lnet/minecraft/entity/MoverType;DDD)V"), cancellable = true)
    public void move(MoverType type, double x, double y, double z, CallbackInfo info) {
        MotionEvent motionEvent = new MotionEvent(type, x, y, z);
        MinecraftForge.EVENT_BUS.post(motionEvent);

        if (motionEvent.isCanceled()) {
            super.move(type, motionEvent.getX(), motionEvent.getY(), motionEvent.getZ());
            info.cancel();
        }
    }

    @Redirect(method= "onLivingUpdate" , at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;setSprinting(Z)V", ordinal = 2))
    public void onLivingUpdate(EntityPlayerSP entityPlayerSP, boolean sprinting) {
        LivingUpdateEvent livingUpdateEvent = new LivingUpdateEvent(entityPlayerSP, sprinting);
        MinecraftForge.EVENT_BUS.post(livingUpdateEvent);

        if (livingUpdateEvent.isCanceled())
            livingUpdateEvent.getEntityPlayerSP().setSprinting(true);
        else
            entityPlayerSP.setSprinting(sprinting);
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"), cancellable = true)
    public void onUpdateMovingPlayer(CallbackInfo info) {
        MotionUpdateEvent motionUpdateEvent = new MotionUpdateEvent();
        MinecraftForge.EVENT_BUS.post(motionUpdateEvent);

        if (motionUpdateEvent.isCanceled()) {
            info.cancel();
            RotationUtil.updateRotationPackets(motionUpdateEvent);
        }
    }
}
