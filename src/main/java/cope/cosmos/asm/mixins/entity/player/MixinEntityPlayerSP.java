package cope.cosmos.asm.mixins.entity.player;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.entity.LivingUpdateEvent;
import cope.cosmos.client.events.entity.player.RotationUpdateEvent;
import cope.cosmos.client.events.entity.player.UpdateWalkingPlayerEvent;
import cope.cosmos.client.events.motion.movement.MotionEvent;
import cope.cosmos.client.events.motion.movement.MotionUpdateEvent;
import cope.cosmos.util.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.MoverType;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP extends AbstractClientPlayer implements Wrapper {
    public MixinEntityPlayerSP() {
        super(Minecraft.getMinecraft().world, Minecraft.getMinecraft().player.getGameProfile());
    }

    // locks the update function
    private boolean updateLock;

    // mc
    @Shadow
    protected Minecraft mc;

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

    @Shadow
    public abstract void onUpdate();

    @Shadow
    protected abstract void onUpdateWalkingPlayer();

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;move(Lnet/minecraft/entity/MoverType;DDD)V"))
    public void move(AbstractClientPlayer player, MoverType type, double x, double y, double z) {
        MotionEvent motionEvent = new MotionEvent(type, x, y, z);
        Cosmos.EVENT_BUS.post(motionEvent);

        if (motionEvent.isCanceled()) {
            super.move(motionEvent.getType(), motionEvent.getX(), motionEvent.getY(), motionEvent.getZ());
        }

        else {
            super.move(type, x, y, z);
        }
    }

    @Redirect(method= "onLivingUpdate" , at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;setSprinting(Z)V", ordinal = 2))
    public void onLivingUpdate(EntityPlayerSP entityPlayerSP, boolean sprintUpdate) {
        LivingUpdateEvent livingUpdateEvent = new LivingUpdateEvent(entityPlayerSP, sprintUpdate);
        Cosmos.EVENT_BUS.post(livingUpdateEvent);

        if (livingUpdateEvent.isCanceled()) {
            livingUpdateEvent.getEntityPlayerSP().setSprinting(true);
        }

        else {
            entityPlayerSP.setSprinting(sprintUpdate);
        }
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"), cancellable = true)
    public void onOnUpdateWalkingPlayer(CallbackInfo info) {

        // pre
        RotationUpdateEvent rotationUpdateEvent = new RotationUpdateEvent();
        Cosmos.EVENT_BUS.post(rotationUpdateEvent);

        // prevent client from sending packets
        if (rotationUpdateEvent.isCanceled()) {

            // post
            MotionUpdateEvent motionUpdateEvent = new MotionUpdateEvent();
            Cosmos.EVENT_BUS.post(motionUpdateEvent);

            // prevent vanilla packets from sending
            info.cancel();

            // send custom packets
            if (motionUpdateEvent.isCanceled()) {
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
                    boolean movementUpdate = StrictMath.pow(motionUpdateEvent.getX() - lastReportedPosX, 2) + StrictMath.pow(motionUpdateEvent.getY() - lastReportedPosY, 2) + StrictMath.pow(motionUpdateEvent.getZ() - lastReportedPosZ, 2) > 9.0E-4D || positionUpdateTicks >= 20;
                    boolean rotationUpdate = motionUpdateEvent.getYaw() - lastReportedYaw != 0.0D || motionUpdateEvent.getPitch() - lastReportedPitch != 0.0D;

                    if (isRiding()) {
                        mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(motionX, -999.0D, motionZ, motionUpdateEvent.getYaw(), motionUpdateEvent.getPitch(), motionUpdateEvent.getOnGround()));
                        movementUpdate = false;
                    }

                    else if (movementUpdate && rotationUpdate) {
                        mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(motionUpdateEvent.getX(), motionUpdateEvent.getY(), motionUpdateEvent.getZ(), motionUpdateEvent.getYaw(), motionUpdateEvent.getPitch(), motionUpdateEvent.getOnGround()));
                    }

                    else if (movementUpdate) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(motionUpdateEvent.getX(), motionUpdateEvent.getY(), motionUpdateEvent.getZ(), motionUpdateEvent.getOnGround()));
                    }

                    else if (rotationUpdate) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(motionUpdateEvent.getYaw(), motionUpdateEvent.getPitch(), motionUpdateEvent.getOnGround()));
                    }

                    else if (prevOnGround != motionUpdateEvent.getOnGround()) {
                        mc.player.connection.sendPacket(new CPacketPlayer(motionUpdateEvent.getOnGround()));
                    }

                    if (movementUpdate) {
                        lastReportedPosX = motionUpdateEvent.getX();
                        lastReportedPosY = motionUpdateEvent.getY();
                        lastReportedPosZ = motionUpdateEvent.getZ();
                        positionUpdateTicks = 0;
                    }

                    if (rotationUpdate) {
                        lastReportedYaw = motionUpdateEvent.getYaw();
                        lastReportedPitch = motionUpdateEvent.getPitch();
                    }

                    prevOnGround = motionUpdateEvent.getOnGround();
                    autoJumpEnabled = mc.gameSettings.autoJump;
                }
            }
        }
    }

    @Inject(method = "onUpdate", at = @At(value = "INVOKE", target = "net/minecraft/client/entity/EntityPlayerSP.onUpdateWalkingPlayer()V", ordinal = 0, shift = At.Shift.AFTER), cancellable = true)
    public void onOnUpdatePost(CallbackInfo info) {

        // event is locked
        if (updateLock) {
            return;
        }

        UpdateWalkingPlayerEvent updateWalkingPlayerEvent = new UpdateWalkingPlayerEvent();
        Cosmos.EVENT_BUS.post(updateWalkingPlayerEvent);

        // prevent player from updating
        if (updateWalkingPlayerEvent.isCanceled()) {
            info.cancel();

            // idk
            if (updateWalkingPlayerEvent.getIterations() > 0) {

                // run
                for (int i = 0; i < updateWalkingPlayerEvent.getIterations(); i++) {

                    // lock
                    updateLock = true;

                    // run onUpdate, this updates motion
                    onUpdate();

                    // unlock
                    updateLock = false;
                    onUpdateWalkingPlayer();
                }
            }
        }
    }
}
