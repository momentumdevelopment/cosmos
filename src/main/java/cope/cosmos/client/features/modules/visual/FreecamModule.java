package cope.cosmos.client.features.modules.visual;

import com.mojang.authlib.GameProfile;
import cope.cosmos.asm.mixins.accessor.IEntityLivingBase;
import cope.cosmos.asm.mixins.accessor.IMinecraft;
import cope.cosmos.client.events.client.SettingUpdateEvent;
import cope.cosmos.client.events.combat.DeathEvent;
import cope.cosmos.client.events.entity.player.PlayerTurnEvent;
import cope.cosmos.client.events.entity.player.RotationUpdateEvent;
import cope.cosmos.client.events.motion.movement.MotionEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.events.render.world.RenderCaveCullingEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.combat.AuraModule;
import cope.cosmos.client.features.modules.combat.AutoCrystalModule;
import cope.cosmos.client.features.modules.world.SpeedMineModule;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.holder.Rotation;
import cope.cosmos.util.player.AngleUtil;
import cope.cosmos.util.player.MotionUtil;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author aesthetical, linustouchtips
 * @since 08/29/2022
 */
public class FreecamModule extends Module {
    public static FreecamModule INSTANCE;

    public FreecamModule() {
        super("Freecam", new String[] {"FreeCamera", "FreeLook"}, Category.VISUAL, "Allows you to view the world freely");
        INSTANCE = this;
    }

    // **************************** general ****************************

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.CAMERA)
            .setAlias("M", "Type")
            .setDescription("The type of freecam");

    public static Setting<Double> speed = new Setting<>("Speed", 0.1D, 0.5D, 5D, 1)
            .setAlias("MoveSpeed")
            .setDescription("The speed to move at");

    public static Setting<Boolean> rotate = new Setting<>("Rotation", false)
            .setAlias("Rotate")
            .setDescription("Rotates to the interaction")
            .setVisible(() -> mode.getValue().equals(Mode.CAMERA));

    public static Setting<Boolean> cancelPackets = new Setting<>("CancelPackets", true)
            .setAlias("Exploit")
            .setDescription("Cancels player packets")
            .setVisible(() -> mode.getValue().equals(Mode.NORMAL));

    // camera entity
    private EntityOtherPlayerMP camera;
    private EntityOtherPlayerMP playerModel;

    @Override
    public void onEnable() {
        super.onEnable();

        // check if the camera exists
        if (camera != null) {

            // TODO: this is the shittest way to test...
            int id = camera.getEntityId();

            // id equals specific number then we know it's the camera
            if (id == -133769421 && !mode.getValue().equals(Mode.CAMERA)) {
                mc.world.removeEntity(camera);
                mc.world.removeEntityDangerously(camera);
                camera = null;
            }

            // id equals specific number then we know it's the camera
            if (id == -133769422 && !mode.getValue().equals(Mode.NORMAL)) {
                mc.world.removeEntity(camera);
                mc.world.removeEntityDangerously(camera);
                camera = null;
            }
        }

        // check if the camera is not null
        if (camera == null) {

            // just opened game
            if (mc.player.ticksExisted < 5) {
                return;
            }

            // add camera entity
            if (mode.getValue().equals(Mode.CAMERA)) {
                camera = new Camera(mc.world, mc.player.getGameProfile());
                camera.setEntityId(-133769421);
            }

            // add camera entity
            else {
                camera = new EntityOtherPlayerMP(mc.world, mc.player.getGameProfile());
                camera.setEntityId(-133769422);
            }

            // copy
            camera.copyLocationAndAnglesFrom(mc.player);
            camera.inventory.copyInventory(mc.player.inventory);

            // spawn
            mc.world.addEntityToWorld(camera.getEntityId(), camera);

            // set view entity
            if (mode.getValue().equals(Mode.CAMERA)) {

                // visual model of last server position
                playerModel = new EntityOtherPlayerMP(mc.world, mc.player.getGameProfile());

                // match characteristics of player to model -> create a copy
                playerModel.copyLocationAndAnglesFrom(mc.player);
                playerModel.rotationYawHead = mc.player.rotationYaw;
                playerModel.inventory.copyInventory(mc.player.inventory);
                playerModel.setSneaking(mc.player.isSneaking());
                playerModel.setPrimaryHand(mc.player.getPrimaryHand());

                // add model to world
                mc.world.addEntityToWorld(-100, playerModel);
                ((IMinecraft) mc).hookSetRenderViewEntity(camera);
            }

            else {
                ((IMinecraft) mc).hookSetRenderViewEntity(mc.player);
            }
        }

        // prevent chunk renders
        mc.renderChunksMany = false;
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // copy angles from camera
        if (mode.getValue().equals(Mode.NORMAL)) {
            mc.player.copyLocationAndAnglesFrom(camera);
        } 
        
        else {
            // remove our model from the world
            mc.world.removeEntityFromWorld(-100);
            ((IMinecraft) mc).hookSetRenderViewEntity(mc.player);
        }

        // remove camera
        mc.world.removeEntity(camera);
        mc.world.removeEntityDangerously(camera);
        camera = null;
        mc.renderChunksMany = true;
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {

        // disable on logout
        disable(true);
    }

    @Override
    public void onUpdate() {

        // sync camera
        if (camera != null) {
            camera.setHealth(mc.player.getHealth());
            camera.setAbsorptionAmount(mc.player.getAbsorptionAmount());
            camera.inventory.copyInventory(mc.player.inventory);
        }

        // sync server model
        if (playerModel != null) {
            playerModel.motionX = mc.player.motionX;
            playerModel.motionY = mc.player.motionY;
            playerModel.motionZ = mc.player.motionZ;
            playerModel.distanceWalkedModified = mc.player.distanceWalkedModified;
            playerModel.distanceWalkedOnStepModified = mc.player.distanceWalkedOnStepModified;
            playerModel.inventory.copyInventory(mc.player.inventory);
            playerModel.setSneaking(mc.player.isSneaking());
            playerModel.setPrimaryHand(mc.player.getPrimaryHand());
            playerModel.setHealth(mc.player.getHealth());
            playerModel.setAbsorptionAmount(mc.player.getAbsorptionAmount());
            playerModel.setInvisible(mc.player.isInvisible());
            playerModel.swingProgress = mc.player.swingProgress;
            playerModel.swingingHand = mc.player.swingingHand;
            playerModel.isSwingInProgress = mc.player.isSwingInProgress;
            playerModel.prevSwingProgress = mc.player.prevSwingProgress;
            playerModel.swingProgressInt = mc.player.swingProgressInt;
            playerModel.ticksExisted = mc.player.ticksExisted;
            playerModel.posX = mc.player.posX;
            playerModel.posY = mc.player.posY;
            playerModel.posZ = mc.player.posZ;
            playerModel.setPosition(playerModel.posX, playerModel.posY, playerModel.posZ);
            playerModel.lastTickPosX = mc.player.lastTickPosX;
            playerModel.lastTickPosY = mc.player.lastTickPosY;
            playerModel.lastTickPosZ = mc.player.lastTickPosZ;
            playerModel.limbSwing = mc.player.limbSwing;
            playerModel.limbSwingAmount = mc.player.limbSwingAmount;
            playerModel.prevLimbSwingAmount = mc.player.prevLimbSwingAmount;
            playerModel.hurtResistantTime = mc.player.hurtResistantTime;
            playerModel.hurtTime = mc.player.hurtTime;
            ((IEntityLivingBase) playerModel).setActivePotionMap(mc.player.getActivePotionMap());
            playerModel.move(MoverType.SELF, playerModel.motionX, playerModel.motionY, playerModel.motionZ);
        }
    }

    @SubscribeEvent
    public void onRotationUpdate(RotationUpdateEvent event) {

        if (nullCheck()) {

            // rotate to interaction
            if (isInteracting()) {

                // check if needs rotate
                if (rotate.getValue()) {

                    // multitask
                    if (!AutoCrystalModule.INSTANCE.isActive() && !AuraModule.INSTANCE.isActive()) {

                        // check if the camera exists
                        if (camera != null) {

                            // check if the interaction is valid
                            if (mc.objectMouseOver != null) {

                                // rotations to interactions
                                Rotation rotation = null;

                                // force rotations to mine
                                if (SpeedMineModule.INSTANCE.isActive()) {

                                    // interacting block position
                                    BlockPos mine = SpeedMineModule.INSTANCE.getMinePosition();

                                    // update rotations to interactions
                                    if (mine != null) {
                                        rotation = AngleUtil.calculateAngles(mine.add(0.5, 0.5, 0.5));
                                    }
                                }

                                // block raytrace
                                else if (mc.objectMouseOver.typeOfHit.equals(RayTraceResult.Type.BLOCK)) {

                                    // interacting block position
                                    BlockPos interact = mc.objectMouseOver.getBlockPos();

                                    // update rotations to interactions
                                    rotation = AngleUtil.calculateAngles(interact.add(0.5, 0.5, 0.5));
                                }

                                // entity raytrace
                                else if (mc.objectMouseOver.typeOfHit.equals(RayTraceResult.Type.ENTITY)) {

                                    // interacting entity
                                    Entity interact = mc.objectMouseOver.entityHit;

                                    // update rotations to interactions
                                    rotation = AngleUtil.calculateAngles(interact.getPositionVector());
                                }

                                // rotation exists
                                if (rotation != null) {

                                    // remove vanilla rotations
                                    event.setCanceled(true);

                                    // update rotations
                                    mc.player.connection.sendPacket(new CPacketPlayer.Rotation(rotation.getYaw(), rotation.getPitch(), mc.player.onGround));
                                    playerModel.rotationYaw = rotation.getYaw();
                                    playerModel.rotationYawHead = rotation.getYaw();
                                    playerModel.rotationPitch = rotation.getPitch();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onSettingUpdate(SettingUpdateEvent event) {

        if (nullCheck()) {

            // mode update
            if (event.getSetting().equals(mode)) {

                // check if the camera exists
                if (camera != null) {

                    // TODO: this is the shittest way to test...
                    int id = camera.getEntityId();

                    // id equals specific number then we know it's the camera
                    if (id == -133769421 && !mode.getValue().equals(Mode.CAMERA)) {
                        mc.world.removeEntity(camera);
                        mc.world.removeEntityDangerously(camera);
                        camera = null;
                    }

                    // id equals specific number then we know it's the camera
                    if (id == -133769422 && !mode.getValue().equals(Mode.NORMAL)) {
                        mc.world.removeEntity(camera);
                        mc.world.removeEntityDangerously(camera);
                        camera = null;
                    }
                }

                // check if the camera is not null
                if (camera == null) {

                    // just opened game
                    if (mc.player.ticksExisted < 5) {
                        return;
                    }

                    // add camera entity
                    if (mode.getValue().equals(Mode.CAMERA)) {
                        camera = new Camera(mc.world, mc.player.getGameProfile());
                        camera.setEntityId(-133769421);
                    }

                    // add camera entity
                    else {
                        camera = new EntityOtherPlayerMP(mc.world, mc.player.getGameProfile());
                        camera.setEntityId(-133769422);
                    }

                    // copy
                    camera.copyLocationAndAnglesFrom(mc.player);
                    camera.inventory.copyInventory(mc.player.inventory);

                    // spawn
                    mc.world.addEntityToWorld(camera.getEntityId(), camera);

                    // set view entity
                    if (mode.getValue().equals(Mode.CAMERA)) {

                        // visual model of last server position
                        playerModel = new EntityOtherPlayerMP(mc.world, mc.player.getGameProfile());

                        // match characteristics of player to model -> create a copy
                        playerModel.copyLocationAndAnglesFrom(mc.player);
                        playerModel.rotationYawHead = mc.player.rotationYaw;
                        playerModel.inventory.copyInventory(mc.player.inventory);
                        playerModel.setSneaking(mc.player.isSneaking());
                        playerModel.setPrimaryHand(mc.player.getPrimaryHand());

                        // add model to world
                        mc.world.addEntityToWorld(-100, playerModel);
                        ((IMinecraft) mc).hookSetRenderViewEntity(camera);
                    }

                    else {
                        ((IMinecraft) mc).hookSetRenderViewEntity(mc.player);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onDeath(DeathEvent event) {

        // check if the player has died and respawns the freecam entity
        if (event.getEntity().equals(mc.player)) {
            mc.world.removeEntity(camera);
            mc.world.removeEntityDangerously(camera);
            camera = null;
            ((IMinecraft) mc).hookSetRenderViewEntity(mc.player);

            // disable module
            disable(true);
        }
    }

    @SubscribeEvent
    public void onMove(MotionEvent event) {
        if (mode.getValue().equals(Mode.NORMAL)) {

            // player inputs
            float forward = mc.player.movementInput.moveForward;
            float strafe = mc.player.movementInput.moveStrafe;
            float yaw = mc.player.rotationYaw;

            // if we are not inputting any movements, then don't update motion
            if (!MotionUtil.isMoving()) {
                event.setX(0);
                event.setZ(0);
            }

            else if (forward != 0) {
                if (strafe >= 1) {
                    yaw += (float) (forward > 0 ? -45 : 45);
                    strafe = 0;
                }

                else if (strafe <= -1) {
                    yaw += (float) (forward > 0 ? 45 : -45);
                    strafe = 0;
                }

                if (forward > 0) {
                    forward = 1;
                }

                else if (forward < 0) {
                    forward = -1;
                }
            }

            // angles
            double sin = Math.sin(Math.toRadians(yaw + 90));
            double cos = Math.cos(Math.toRadians(yaw + 90));

            // apply no clip and cancel vanilla movements
            mc.player.noClip = true;
            event.setCanceled(true);

            // update motion
            event.setX((double) forward * speed.getValue() * cos + (double) strafe * speed.getValue() * sin);
            event.setZ((double) forward * speed.getValue() * sin - (double) strafe * speed.getValue() * cos);

            if (!MotionUtil.isMoving()) {
                event.setX(0);
                event.setZ(0);
            }

            double motionY = 0;

            // up movement
            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                motionY = speed.getValue();
            }

            // down movement
            else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                motionY = -speed.getValue();
            }

            // update vertical movement
            event.setY(motionY);
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {

        // packet for moving and rotating
        if (event.getPacket() instanceof CPacketPlayer) {

            // cancel packets
            if (mode.getValue().equals(Mode.NORMAL)) {

                // prevent player packets from sending
                if (cancelPackets.getValue()) {
                    event.setCanceled(true);
                }
            }
        }

        // packet for attacking
        if (event.getPacket() instanceof CPacketUseEntity) {

            // entity we are attacking
            Entity entity = ((CPacketUseEntity) event.getPacket()).getEntityFromWorld(mc.world);

            // do not allow us to interact with our selves
            if (entity != null && entity.equals(mc.player)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onInputUpdate(InputUpdateEvent event) {
        
        // cancel movement inputs
        if (event.getMovementInput() instanceof MovementInputFromOptions && mode.getValue().equals(Mode.CAMERA)) {
            
            // reset inputs
            event.getMovementInput().moveForward = 0;
            event.getMovementInput().moveStrafe = 0;
            event.getMovementInput().forwardKeyDown = false;
            event.getMovementInput().backKeyDown = false;
            event.getMovementInput().rightKeyDown = false;
            event.getMovementInput().leftKeyDown = false;
            event.getMovementInput().jump = false;
            event.getMovementInput().sneak = false;
        }
    }

    @SubscribeEvent
    public void onPlayerTurn(PlayerTurnEvent event) {
        if (mode.getValue().equals(Mode.CAMERA) && camera != null) {

            // cancel vanilla player rotations
            event.setCanceled(true);

            // turn the camera to match player rotations
            camera.turn(event.getYaw(), event.getPitch());
            camera.cameraYaw = event.getYaw();
            camera.cameraPitch = event.getPitch();
        }
    }

    @SubscribeEvent
    public void onRenderCaveCulling(RenderCaveCullingEvent event) {
        
        // cancel cave culling effect 
        event.setCanceled(true);
    }

    /**
     * Checks if the freecam is interacting
     * @return Whether the freecam is interacting
     */
    public boolean isInteracting() {
        return isEnabled() && mode.getValue().equals(Mode.CAMERA);
    }

    private static class Camera extends EntityOtherPlayerMP {

        public Camera(World worldIn, GameProfile gameProfileIn) {
            super(worldIn, gameProfileIn);
            capabilities.isFlying = true;
            capabilities.allowFlying = true;
        }

        @Override
        public void onLivingUpdate() {
            super.onLivingUpdate();

            // copy inventory of player
            inventory.copyInventory(mc.player.inventory);

            // update action states
            updateEntityActionState();

            // update move states
            if (mc.gameSettings.keyBindForward.isKeyDown()) {
                moveForward = 1;
            } 
            
            else if (mc.gameSettings.keyBindBack.isKeyDown()) {
                moveForward = -1;
            } 
            
            else {
                moveForward = 0;
            }

            if (mc.gameSettings.keyBindRight.isKeyDown()) {
                moveStrafing = -1;
            } 
            
            else if (mc.gameSettings.keyBindLeft.isKeyDown()) {
                moveStrafing = 1;
            } 
            
            else {
                moveStrafing = 0;
            }

            // move speeds
            motionY = 0;

            // inputs
            float forward = moveForward;
            float strafe = moveStrafing;
            float yaw = rotationYaw;

            // if we are not inputting any movements, then don't update motion
            if (!isMoving()) {
                motionX = 0;
                motionZ = 0;
            }

            if (forward != 0) {
                if (strafe >= 1) {
                    yaw += (float) (forward > 0 ? -45 : 45);
                    strafe = 0;
                }

                else if (strafe <= -1) {
                    yaw += (float) (forward > 0 ? 45 : -45);
                    strafe = 0;
                }

                if (forward > 0) {
                    forward = 1;
                }

                else if (forward < 0) {
                    forward = -1;
                }
            }

            // angles
            double sin = Math.sin(Math.toRadians(yaw + 90));
            double cos = Math.cos(Math.toRadians(yaw + 90));

            // update motion
            motionX = forward * speed.getValue() * cos + strafe * speed.getValue() * sin;
            motionZ = forward * speed.getValue() * sin - strafe * speed.getValue() * cos;

            if (!isMoving()) {
                motionX = 0;
                motionZ = 0;
            }

            // up speed
            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                motionY = speed.getValue();
            }

            // down speed
            else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                motionY = -speed.getValue();
            }

            noClip = true;
            move(MoverType.SELF, motionX, motionY, motionZ);
        }

        /**
         * Checks if the player is moving
         * @return Whether the player is moving
         */
        public boolean isMoving() {
            return moveForward != 0 || moveStrafing != 0;
        }
    }

    public enum Mode {

        /**
         * Basic ass freecam, creates a fakeplayer and cancels movement
         */
        NORMAL,

        /**
         * A more legit type of freecam
         */
        CAMERA
    }
}