package cope.cosmos.asm.mixins;

import com.mojang.authlib.GameProfile;
import cope.cosmos.client.events.LivingUpdateEvent;
import cope.cosmos.client.events.MotionEvent;
import cope.cosmos.client.events.MotionUpdateEvent;
import cope.cosmos.util.Wrapper;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.MoverType;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP extends AbstractClientPlayer implements Wrapper {
    public MixinEntityPlayerSP(World worldIn, GameProfile playerProfile) {
        super(mc.world, mc.getSession().getProfile());
    }

    @Shadow
    private boolean prevOnGround;

    @Shadow
    private float lastReportedYaw;

    @Shadow
    private float lastReportedPitch;

    @Shadow
    private int positionUpdateTicks;

    @Shadow
    private double lastReportedPosX;

    @Shadow
    private double lastReportedPosY;

    @Shadow
    private double lastReportedPosZ;

    @Shadow
    private boolean autoJumpEnabled;

    @Shadow
    private boolean serverSprintState;

    @Shadow
    private boolean serverSneakState;

    @Shadow
    protected abstract boolean isCurrentViewEntity();

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;move(Lnet/minecraft/entity/MoverType;DDD)V"), cancellable = true)
    public void move(MoverType type, double x, double y, double z, CallbackInfo info) {
        MotionEvent motionEvent = new MotionEvent(type, x, y, z);
        MinecraftForge.EVENT_BUS.post(motionEvent);

        if (motionEvent.isCanceled()) {
            info.cancel();
            super.move(type, motionEvent.getX(), motionEvent.getY(), motionEvent.getZ());
        }
    }

    @Redirect(method= "onLivingUpdate" , at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;setSprinting(Z)V", ordinal = 2))
    public void onLivingUpdate(EntityPlayerSP entityPlayerSP, boolean sprintUpdate) {
        LivingUpdateEvent livingUpdateEvent = new LivingUpdateEvent(entityPlayerSP, sprintUpdate);
        MinecraftForge.EVENT_BUS.post(livingUpdateEvent);

        if (livingUpdateEvent.isCanceled()) {
            livingUpdateEvent.getEntityPlayerSP().setSprinting(true);
        }

        else {
            entityPlayerSP.setSprinting(sprintUpdate);
        }
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"), cancellable = true)
    public void onUpdateMovingPlayer(CallbackInfo info) {
        MotionUpdateEvent motionUpdateEvent = new MotionUpdateEvent();
        MinecraftForge.EVENT_BUS.post(motionUpdateEvent);

        if (motionUpdateEvent.isCanceled()) {
            info.cancel();

            positionUpdateTicks++;

            boolean sprintUpdate = isSprinting();
            if (sprintUpdate != serverSprintState) {
                if (sprintUpdate) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_SPRINTING));
                }

                else {
                    mc.player.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.STOP_SPRINTING));
                }

                serverSprintState = sprintUpdate;
            }

            boolean sneakUpdate = isSneaking();
            if (sneakUpdate != serverSneakState) {
                if (sneakUpdate) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_SNEAKING));
                }

                else {
                    mc.player.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.STOP_SNEAKING));
                }

                serverSneakState = sneakUpdate;
            }

            if (isCurrentViewEntity()) {
                boolean movementUpdate = Math.pow(mc.player.posX - lastReportedPosX, 2) + Math.pow(mc.player.getEntityBoundingBox().minY - lastReportedPosY, 2) + Math.pow(mc.player.posZ - lastReportedPosZ, 2) > 9.0E-4D || positionUpdateTicks >= 20;
                boolean rotationUpdate = motionUpdateEvent.getYaw() - lastReportedYaw != 0.0D || motionUpdateEvent.getPitch() - lastReportedPitch != 0.0D;

                if (isRiding()) {
                    mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(motionX, -999.0D, motionZ, motionUpdateEvent.getYaw(), motionUpdateEvent.getPitch(), onGround));
                    movementUpdate = false;
                } 
                
                else if (movementUpdate && rotationUpdate) {
                    mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.getEntityBoundingBox().minY, mc.player.posZ, motionUpdateEvent.getYaw(), motionUpdateEvent.getPitch(), onGround));
                } 
                
                else if (movementUpdate) {
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.getEntityBoundingBox().minY, mc.player.posZ, onGround));
                } 
                
                else if (rotationUpdate) {
                    mc.player.connection.sendPacket(new CPacketPlayer.Rotation(motionUpdateEvent.getYaw(), motionUpdateEvent.getPitch(), onGround));
                } 
                
                else if (prevOnGround != onGround) {
                    mc.player.connection.sendPacket(new CPacketPlayer(onGround));
                }

                if (movementUpdate) {
                    lastReportedPosX = mc.player.posX;
                    lastReportedPosY = mc.player.getEntityBoundingBox().minY;
                    lastReportedPosZ = mc.player.posZ;
                    positionUpdateTicks = 0;
                }

                if (rotationUpdate) {
                    lastReportedYaw = motionUpdateEvent.getYaw();
                    lastReportedPitch = motionUpdateEvent.getPitch();
                }

                prevOnGround = onGround;
                autoJumpEnabled = mc.gameSettings.autoJump;
            }
        }
    }
}
