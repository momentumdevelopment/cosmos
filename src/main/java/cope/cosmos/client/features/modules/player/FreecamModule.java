package cope.cosmos.client.features.modules.player;

import com.mojang.authlib.GameProfile;
import cope.cosmos.client.events.client.SettingUpdateEvent;
import cope.cosmos.client.events.input.UpdateMoveStateEvent;
import cope.cosmos.client.events.motion.movement.MotionEvent;
import cope.cosmos.client.events.motion.movement.PushOutOfBlocksEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.MotionUtil;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.*;
import net.minecraft.util.MovementInput;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @author linustouchtips
 * @since 08/26/2022
 */
public class FreecamModule extends Module {
    public static FreecamModule INSTANCE;

    public FreecamModule() {
        super("Freecam", new String[] {"FreeCamera", "FreeLook"}, Category.PLAYER,"Allows you to view the world freely");
        INSTANCE = this;
    }

    // **************************** general ****************************

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.CAMERA)
            .setDescription("Mode for camera");

    public static Setting<Double> speed = new Setting<>("Speed", 0.1D, 1.0D, 10.0D, 1)
            .setDescription("Speed for movement");

    public static Setting<Boolean> rotate = new Setting<>("Rotation", false)
            .setAlias("Rotate")
            .setDescription("Rotates to the interaction");

    public static Setting<Boolean> cancelPackets = new Setting<>("CancelPackets", true)
            .setAlias("Exploit")
            .setDescription("Cancels Packets to the server");
    
    // camera entity
    private Camera camera;

    // previous info
    private double posX, posY, posZ;
    private float pitch, yaw;
    private Entity riding;
    
    @Override
    public void onEnable() {
        super.onEnable();

        // save previous info
        posX = mc.player.posX;
        posY = mc.player.posY;
        posZ = mc.player.posZ;
        pitch = mc.player.rotationPitch;
        yaw = mc.player.rotationYaw;
        // yawHead = mc.player.rotationYawHead;

        // riding entity
        if (mc.player.isRiding()) {
            riding = mc.player.getRidingEntity();
        }
        
        // update view entity
        if (mode.getValue().equals(Mode.CAMERA)) {
            
            // make sure an old camera doesn't exist
            if (camera == null) {

                // create new camera entity
                camera = new Camera(mc.world, mc.player.getGameProfile());

                // update view entity
                mc.setRenderViewEntity(camera);
            }
        }

        // update player
        if (mode.getValue().equals(Mode.NORMAL)) {

            // make sure a camera exists
            if (camera != null) {

                // delete the camera
                camera = null;

                // update view entity
                mc.setRenderViewEntity(mc.player);
            }

            // dismount riding entities
            if (mc.player.isRiding()) {
                mc.player.dismountRidingEntity();
            }

            // visual model of the player
            EntityOtherPlayerMP playerModel = new EntityOtherPlayerMP(mc.world, mc.player.getGameProfile());

            // match characteristics of player to model -> create a copy
            playerModel.copyLocationAndAnglesFrom(mc.player);
            playerModel.rotationYawHead = mc.player.rotationYawHead;
            playerModel.inventory.copyInventory(mc.player.inventory);
            playerModel.setSneaking(mc.player.isSneaking());
            playerModel.setPrimaryHand(mc.player.getPrimaryHand());

            // add model to world
            mc.world.addEntityToWorld(-100, playerModel);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // prevent phasing
        mc.player.noClip = false;

        // reset flying
        if (!mc.playerController.getCurrentGameType().isCreative()) {
            mc.player.capabilities.allowFlying = false;
            mc.player.capabilities.isFlying = false;
        }

        // update player
        if (mode.getValue().equals(Mode.NORMAL)) {

            // reset player
            mc.player.setPositionAndRotation(posX, posY, posZ, yaw, pitch);
            // mc.player.rotationYawHead = yawHead;
            mc.player.setVelocity(0, 0, 0);
            mc.world.removeEntityFromWorld(-100);

            // reset previous info
            posX = 0;
            posY = 0;
            posZ = 0;
            yaw = 0;
            // yawHead = 0;
            pitch = 0;

            // remount
            if (riding != null) {
                mc.player.startRiding(riding, true);
            }
        }
    }

    @SubscribeEvent
    public void onSettingUpdate(SettingUpdateEvent event) {

        if (nullCheck()) {

            // update mode
            if (event.getSetting().equals(mode)) {

                // update view entity
                if (mode.getValue().equals(Mode.NORMAL)) {

                    // make sure a camera exists
                    if (camera != null) {

                        // delete the camera
                        camera = null;

                        // update view entity
                        mc.setRenderViewEntity(mc.player);
                    }

                    // visual model of the player
                    EntityOtherPlayerMP playerModel = new EntityOtherPlayerMP(mc.world, mc.player.getGameProfile());

                    // match characteristics of player to model -> create a copy
                    playerModel.copyLocationAndAnglesFrom(mc.player);
                    playerModel.rotationYawHead = mc.player.rotationYawHead;
                    playerModel.inventory.copyInventory(mc.player.inventory);
                    playerModel.setSneaking(mc.player.isSneaking());
                    playerModel.setPrimaryHand(mc.player.getPrimaryHand());

                    // add model to world
                    mc.world.addEntityToWorld(-100, playerModel);
                }

                // update view entity
                else if (mode.getValue().equals(Mode.CAMERA)) {

                    // prevent phasing
                    mc.player.noClip = false;

                    // reset flying
                    if (!mc.playerController.getCurrentGameType().isCreative()) {
                        mc.player.capabilities.allowFlying = false;
                        mc.player.capabilities.isFlying = false;
                    }

                    // reset player
                    mc.player.setPositionAndRotation(posX, posY, posZ, yaw, pitch);
                    // mc.player.rotationYawHead = yawHead;
                    mc.player.setVelocity(0, 0, 0);
                    mc.world.removeEntityFromWorld(-100);

                    // reset previous info
                    posX = 0;
                    posY = 0;
                    posZ = 0;
                    yaw = 0;
                    // yawHead = 0;
                    pitch = 0;

                    // remount
                    if (riding != null) {
                        mc.player.startRiding(riding, true);
                    }

                    // make sure an old camera doesn't exist
                    if (camera == null) {

                        // create new camera entity
                        camera = new Camera(mc.world, mc.player.getGameProfile());

                        // update view entity
                        mc.setRenderViewEntity(camera);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onUpdateMoveState(UpdateMoveStateEvent event) {

        // inputs go to the new camera entity
        if (mode.getValue().equals(Mode.CAMERA)) {

            // prevent inputs
            mc.player.movementInput.moveForward = 0;
            mc.player.movementInput.moveStrafe = 0;
            mc.player.movementInput.jump = false;
            mc.player.movementInput.forwardKeyDown = false;
            mc.player.movementInput.backKeyDown = false;
            mc.player.movementInput.leftKeyDown = false;
            mc.player.movementInput.rightKeyDown = false;
        }
    }

    @SubscribeEvent
    public void onMotion(MotionEvent event) {

        // move with local player
        if (mode.getValue().equals(Mode.NORMAL)) {

            // allow phasing
            mc.player.noClip = true;
            mc.player.onGround = false;
            mc.player.fallDistance = 0;
            mc.player.capabilities.allowFlying = true;
            mc.player.capabilities.isFlying = true;

            // cancel vanilla movement
            event.setCanceled(true);

            // up/down movement
            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                event.setY(speed.getValue());
            }

            else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                event.setY(-speed.getValue());
            }

            // static
            else {
                event.setY(0);
            }

            // the current movement input values of the user
            float forward = mc.player.movementInput.moveForward;
            float strafe = mc.player.movementInput.moveStrafe;
            float yaw = mc.player.rotationYaw;

            // if we're not inputting any movements, then we shouldn't be adding any motion
            if (!MotionUtil.isMoving()) {
                event.setX(0);
                event.setZ(0);
            }

            else {

                // our facing values, according to movement not rotations
                double cos = Math.cos(Math.toRadians(yaw + 90));
                double sin = Math.sin(Math.toRadians(yaw + 90));

                // update the movements
                event.setX((forward * speed.getValue() * cos) + (strafe * speed.getValue() * sin));
                event.setZ((forward * speed.getValue() * sin) - (strafe * speed.getValue() * cos));

                // if we're not inputting any movements, then we shouldn't be adding any motion
                if (!MotionUtil.isMoving()) {
                    event.setX(0);
                    event.setZ(0);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPushOutOfBlocks(PushOutOfBlocksEvent event) {

        // cancel velocity from blocks
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {

        // player packets
        if ((!(event.getPacket() instanceof CPacketChatMessage || event.getPacket() instanceof CPacketConfirmTeleport || event.getPacket() instanceof CPacketKeepAlive || event.getPacket() instanceof CPacketTabComplete || event.getPacket() instanceof CPacketClientStatus))) {

            // cancel packets
            if (cancelPackets.getValue()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {

        // disable on logout
        disable(true);
    }

    /**
     * Dummy class for the "camera"
     */
    public static class Camera extends EntityOtherPlayerMP {

        // the movement input of the camera
        MovementInput movementInput = new MovementInput();

        public Camera(World worldIn, GameProfile gameProfileIn) {
            super(worldIn, gameProfileIn);

            // copy locations and rotations when spawned
            copyLocationAndAnglesFrom(mc.player);

            // set flying
            capabilities.isFlying = true;
            capabilities.allowFlying = true;
        }

        @Override
        public void onLivingUpdate() {

            // update moving forward
            if (mc.gameSettings.keyBindForward.isKeyDown()) {
                movementInput.moveForward = 1;
            } 
            
            else if (mc.gameSettings.keyBindBack.isKeyDown()) {
                movementInput.moveForward = -1;
            }
            
            else {
                movementInput.moveForward = 0;
            }

            // update moving strafe
            if (mc.gameSettings.keyBindRight.isKeyDown()) {
                movementInput.moveStrafe = -1;
            } 
            
            else if (mc.gameSettings.keyBindLeft.isKeyDown()) {
                movementInput.moveStrafe = 1;
            } 
            
            else {
                movementInput.moveStrafe = 0;
            }

            // up/down movement
            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                motionY = speed.getValue();
            }

            else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                motionY = -speed.getValue();
            }

            // static
            else {
                motionY = 0;
            }

            // the current movement input values of the user
            float forward = movementInput.moveForward;
            float strafe = movementInput.moveStrafe;
            float yaw = rotationYaw;

            // if we're not inputting any movements, then we shouldn't be adding any motion
            if (!isMoving()) {
                motionX = 0;
                motionZ = 0;
            }

            else {

                // our facing values, according to movement not rotations
                double cos = Math.cos(Math.toRadians(yaw + 90));
                double sin = Math.sin(Math.toRadians(yaw + 90));

                // update the movements
                motionX = (forward * speed.getValue() * cos) + (strafe * speed.getValue() * sin);
                motionZ = (forward * speed.getValue() * sin) - (strafe * speed.getValue() * cos);

                // if we're not inputting any movements, then we shouldn't be adding any motion
                if (!isMoving()) {
                    motionX = 0;
                    motionZ = 0;
                }
            }

            // move entity
            move(MoverType.SELF, motionX, motionY, motionZ);
        }

        /**
         * Checks if the camera is moving
         * @return Whether the camera is moving
         */
        public boolean isMoving() {
            return movementInput.moveForward != 0 || movementInput.moveStrafe != 0;
        }

        @Override
        public float getEyeHeight() {
            return 1.65F;
        }

        @Override
        public boolean isSpectator() {
            return true;
        }

        @Override
        protected boolean isMovementBlocked() {
            return false;
        }

        @Override
        public boolean isInvisible() {
            return true;
        }

        @Override
        public boolean isInvisibleToPlayer(@NotNull EntityPlayer player) {
            return true;
        }
    }

    public enum Mode {

        /**
         * Normal freecam
         */
        NORMAL,

        /**
         * Camera freecam
         */
        CAMERA
    }

    public enum Interact {

        /**
         * Interacts at the player
         */
        PLAYER,

        /**
         * Interacts at the camera
         */
        CAMERA
    }
}
