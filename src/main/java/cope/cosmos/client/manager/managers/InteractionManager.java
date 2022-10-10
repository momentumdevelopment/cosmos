package cope.cosmos.client.manager.managers;

import cope.cosmos.asm.mixins.accessor.IEntityLivingBase;
import cope.cosmos.asm.mixins.accessor.IMinecraft;
import cope.cosmos.client.features.modules.exploits.SwingModule;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.util.holder.Rotation;
import cope.cosmos.util.holder.Rotation.Rotate;
import cope.cosmos.util.player.AngleUtil;
import cope.cosmos.util.world.SneakBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPacketAnimation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author linustouchtips
 * @since 11/16/2021
 */
public class InteractionManager extends Manager {
    public InteractionManager() {
        super("InteractionManager", "Manages all player interactions");
    }

    /**
     * If a block is being place in the interaction manager
     */
    private boolean placing = false;

    /**
     * Places a block at a specified position
     * @param position Position of the block to place on
     * @param rotate Mode for rotating {@link Rotate}
     * @param strict Only place on visible offsets
     */
    public void placeBlock(BlockPos position, Rotate rotate, boolean strict) {
        for (EnumFacing direction : EnumFacing.values()) {

            // find a block to place against
            BlockPos directionOffset = position.offset(direction);

            // make sure there is no entity on the block
            for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(position))) {
                if (!(entity instanceof EntityItem) && !(entity instanceof EntityXPOrb)) {
                    return;
                }
            }

            // the opposite facing value
            EnumFacing oppositeFacing = direction.getOpposite();

            // make sure the side is visible, strict NCP flags for non-visible interactions
            if (strict && !getVisibleSides(directionOffset).contains(direction.getOpposite())) {
                continue;
            }

            // make sure the offset is empty
            if (mc.world.getBlockState(directionOffset).getMaterial().isReplaceable()) {
                continue;
            }

            placing = true;

            // stop sprinting before preforming actions
            boolean sprint = mc.player.isSprinting();
            if (sprint) {
                // mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                // mc.player.setSprinting(false);
            }

            // sneak if the block is not right-clickable
            boolean sneak = SneakBlocks.contains(mc.world.getBlockState(directionOffset).getBlock()) && !mc.player.isSneaking();
            if (sneak) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                // mc.player.setSneaking(true);
            }

            // our rotation
            Rotation rotation = getAnglesToBlock(directionOffset, oppositeFacing);

            // vector to the block
            Vec3d interactVector = null; //= new Vec3d(directionOffset).addVector(0.5, 0.5, 0.5).add(new Vec3d(direction.getOpposite().getDirectionVec()).scale(0.5));

            if (strict) {
                RayTraceResult result = getTraceResult(getReachDistance(), rotation);
                if (result != null && result.typeOfHit.equals(Type.BLOCK)) {
                    interactVector = result.hitVec;
                }
            }

            if (interactVector == null) {
                interactVector = new Vec3d(directionOffset).addVector(0.5, 0.5, 0.5);
                rotation = AngleUtil.calculateAngles(interactVector);
            }

            // Rotation oldRotation = getCosmos().getRotationManager().getServerRotation();

            // rotate to block
            if (!rotate.equals(Rotate.NONE) && rotation.isValid()) {
                // rotate via packet, server should confirm instantly?
                switch (rotate) {
                    case CLIENT:
                        mc.player.rotationYaw = rotation.getYaw();
                        mc.player.rotationYawHead = rotation.getYaw();
                        mc.player.rotationPitch = rotation.getPitch();
                        break;
                    case PACKET:

                        // force a rotation - should this be done?
                        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(rotation.getYaw(), rotation.getPitch(), mc.player.onGround));

                        // submit to rotation manager
                        // getCosmos().getRotationManager().setRotation(blockAngles);

                        // ((IEntityPlayerSP) mc.player).setLastReportedYaw(blockAngles[0]);
                        // ((IEntityPlayerSP) mc.player).setLastReportedPitch(blockAngles[1]);
                        break;
                }
            }

            float facingX = (float) (interactVector.x - directionOffset.getZ());
            float facingY = (float) (interactVector.y - directionOffset.getY());
            float facingZ = (float) (interactVector.z - directionOffset.getZ());

            // ip
            String ip = mc.getCurrentServerData() != null ? mc.getCurrentServerData().serverIP : "";

            // right click direction offset block
            if (ip.equalsIgnoreCase("2b2t.org") && mc.getConnection() != null) {

                // send our place packet
                // todo: fuckery with playerController
                //mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(directionOffset, direction.getOpposite(), EnumHand.MAIN_HAND, facingX, facingY, facingZ));
                mc.playerController.processRightClickBlock(
                        mc.player,
                        mc.world,
                        directionOffset,
                        direction.getOpposite(),
                        interactVector,
                        EnumHand.MAIN_HAND
                );
            }

            else {

                // place
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(directionOffset, direction.getOpposite(), EnumHand.MAIN_HAND, facingX, facingY, facingZ));
            }

            // reset sneak
            if (sneak) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                // mc.player.setSneaking(false);
            }

            // reset sprint
            if (sprint) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
                // mc.player.setSprinting(true);
            }

            // swing the player's arm
            // held item stack
            ItemStack stack = mc.player.getHeldItem(EnumHand.MAIN_HAND);

            // check stack
            if (!stack.isEmpty()) {
                if (!stack.getItem().onEntitySwing(mc.player, stack)) {

                    // apply swing progress
                    if (!mc.player.isSwingInProgress || mc.player.swingProgressInt >= ((IEntityLivingBase) mc.player).hookGetArmSwingAnimationEnd() / 2 || mc.player.swingProgressInt < 0) {
                        mc.player.swingProgressInt = -1;
                        mc.player.isSwingInProgress = true;
                        mc.player.swingingHand = SwingModule.INSTANCE.isEnabled() ? SwingModule.INSTANCE.getHand() : EnumHand.MAIN_HAND;

                        // send animation packet
                        if (mc.player.world instanceof WorldServer) {
                            ((WorldServer) mc.player.world).getEntityTracker().sendToTracking(mc.player, new SPacketAnimation(mc.player, 0));
                        }
                    }
                }
            }

            // swing with packets
            mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
            ((IMinecraft) mc).setRightClickDelayTimer(4);
            break;

            /*
            if (!mc.playerController.getCurrentGameType().equals(GameType.CREATIVE)) {
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, directionOffset, EnumFacing.UP));
            }
             */
        }

        placing = false;
    }

    /**
     * Places a block at a specified position
     * @param position Position of the block to place on
     * @param rotate Mode for rotating {@link Rotate}
     * @param strict Only place on visible offsets
     * @param safeEntities  Entities that are able to be placed on
     */
    public void placeBlock(BlockPos position, Rotate rotate, boolean strict, List<Class<? extends Entity>> safeEntities) {
        for (EnumFacing direction : EnumFacing.values()) {

            // find a block to place against
            BlockPos directionOffset = position.offset(direction);

            // the opposite facing value
            EnumFacing oppositeFacing = direction.getOpposite();

            // make sure the side is visible, strict NCP flags for non-visible interactions
            if (strict && !getVisibleSides(directionOffset).contains(direction.getOpposite())) {
                continue;
            }

            // make sure there is no entity on the block
            for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(position))) {
                if (!safeEntities.contains(entity.getClass())) {
                    return;
                }
            }

            // make sure the offset is empty
            if (mc.world.getBlockState(directionOffset).getMaterial().isReplaceable()) {
                continue;
            }

            placing = true;

            // stop sprinting before preforming actions
            boolean sprint = mc.player.isSprinting();
            if (sprint) {
                // mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                // mc.player.setSprinting(false);
            }

            // sneak if the block is not right-clickable
            boolean sneak = SneakBlocks.contains(mc.world.getBlockState(directionOffset).getBlock()) && !mc.player.isSneaking();
            if (sneak) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                // mc.player.setSneaking(true);
            }

            // our rotation
            Rotation rotation = getAnglesToBlock(directionOffset, oppositeFacing);

            // vector to the block
            Vec3d interactVector = null; //= new Vec3d(directionOffset).addVector(0.5, 0.5, 0.5).add(new Vec3d(direction.getOpposite().getDirectionVec()).scale(0.5));

            if (strict) {
                RayTraceResult result = getTraceResult(getReachDistance(), rotation);
                if (result != null && result.typeOfHit.equals(Type.BLOCK)) {
                    interactVector = result.hitVec;
                }
            }

            if (interactVector == null) {
                interactVector = new Vec3d(directionOffset).addVector(0.5, 0.5, 0.5);
            }

            // Rotation oldRotation = getCosmos().getRotationManager().getServerRotation();

            // rotate to block
            if (!rotate.equals(Rotate.NONE) && rotation.isValid()) {
                // rotate via packet, server should confirm instantly?
                switch (rotate) {
                    case CLIENT:
                        mc.player.rotationYaw = rotation.getYaw();
                        mc.player.rotationYawHead = rotation.getYaw();
                        mc.player.rotationPitch = rotation.getPitch();
                        break;
                    case PACKET:

                        // force a rotation - should this be done?
                        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(rotation.getYaw(), rotation.getPitch(), mc.player.onGround));

                        // submit to rotation manager
                        // getCosmos().getRotationManager().setRotation(blockAngles);

                        // ((IEntityPlayerSP) mc.player).setLastReportedYaw(blockAngles[0]);
                        // ((IEntityPlayerSP) mc.player).setLastReportedPitch(blockAngles[1]);
                        break;
                }
            }

            float facingX = (float) (interactVector.x - directionOffset.getZ());
            float facingY = (float) (interactVector.y - directionOffset.getY());
            float facingZ = (float) (interactVector.z - directionOffset.getZ());

            // sync item
            //((IPlayerControllerMP) mc.playerController).hookSyncCurrentPlayItem();

            // ip
           String ip = mc.getCurrentServerData() != null ? mc.getCurrentServerData().serverIP : "";

           // right click direction offset block
          if (ip.equalsIgnoreCase("2b2t.org") && mc.getConnection() != null) {

              // send our place packet
              // todo: fuckery with playerController
              //mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(directionOffset, direction.getOpposite(), EnumHand.MAIN_HAND, facingX, facingY, facingZ));
              mc.playerController.processRightClickBlock(
                      mc.player,
                      mc.world,
                      directionOffset,
                      direction.getOpposite(),
                      interactVector,
                      EnumHand.MAIN_HAND
              );
           }

           else {

               // place
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(directionOffset, direction.getOpposite(), EnumHand.MAIN_HAND, facingX, facingY, facingZ));
           }

            // reset sneak
            if (sneak) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                // mc.player.setSneaking(false);
            }

            // reset sprint
            if (sprint) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
                // mc.player.setSprinting(true);
            }

            // swing hand
            // swing the player's arm
            // held item stack
            ItemStack stack = mc.player.getHeldItem(EnumHand.MAIN_HAND);

            // check stack
            if (!stack.isEmpty()) {
                if (!stack.getItem().onEntitySwing(mc.player, stack)) {

                    // apply swing progress
                    if (!mc.player.isSwingInProgress || mc.player.swingProgressInt >= ((IEntityLivingBase) mc.player).hookGetArmSwingAnimationEnd() / 2 || mc.player.swingProgressInt < 0) {
                        mc.player.swingProgressInt = -1;
                        mc.player.isSwingInProgress = true;
                        mc.player.swingingHand = SwingModule.INSTANCE.isEnabled() ? SwingModule.INSTANCE.getHand() : EnumHand.MAIN_HAND;

                        // send animation packet
                        if (mc.player.world instanceof WorldServer) {
                            ((WorldServer) mc.player.world).getEntityTracker().sendToTracking(mc.player, new SPacketAnimation(mc.player, 0));
                        }
                    }
                }
            }

            // swing with packets
            mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
            ((IMinecraft) mc).setRightClickDelayTimer(4);
            break;

            /*
            if (!mc.playerController.getCurrentGameType().equals(GameType.CREATIVE)) {
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, directionOffset, EnumFacing.UP));
            }
             */
        }

        placing = false;
    }

    /**
     * Swings the player's hand
     * @param in The hand to swing
     */
    public void swingArm(EnumHand in) {
        mc.player.swingArm(in);
    }

    public void attackEntity(Entity entity, boolean packet, double variation) {

        // check hit chance
        if (Math.random() <= (variation / 100)) {

            // attack
            if (packet) {
                mc.player.connection.sendPacket(new CPacketUseEntity(entity));

                // reset the attack cooldown
                // we only reset the cooldown in this condition because PlayerControllerMP#attackEntity already resets the cooldown
                mc.player.resetCooldown();
            }

            else {
                mc.playerController.attackEntity(mc.player, entity);
            }
        }
    }

    /**
     * Checks to see if we can see a block face via vanilla raytracing
     * @param position the position to place at
     * @param facing the face of the block
     * @return if we can see this face at the server-sided rotations
     */
    public boolean isFaceVisible(BlockPos position, EnumFacing facing) {
        RayTraceResult result = getTraceResult(getReachDistance(), getCosmos().getRotationManager().getServerRotation());
        if (result == null || !result.typeOfHit.equals(Type.BLOCK)) {
            return false;
        }

        return position.equals(result.getBlockPos()) && result.sideHit.equals(facing);
    }

    /**
     * Gets the vanilla raytrace result from server-sided rotations
     * @param distance the distance to look for things at
     * @return the raytrace result, or null if nothing was found
     */
    public RayTraceResult getTraceResult(double distance, Rotation rotation) {
        Vec3d eyes = mc.player.getPositionEyes(1.0f);

        if (!rotation.isValid()) {
            rotation = getCosmos().getRotationManager().getServerRotation();
        }

        Vec3d rotationVector = AngleUtil.getVectorForRotation(rotation);

        return mc.world.rayTraceBlocks(
                eyes,
                eyes.addVector(rotationVector.x * distance, rotationVector.y * distance, rotationVector.z * distance),
                false,
                false,
                true
        );
    }

    private Rotation getAnglesToBlock(BlockPos pos, EnumFacing facing) {

        // get our player positions
        double x = mc.player.posX;
        double y = mc.player.posY;
        double z = mc.player.posZ;

        // get the difference between the position and facing
        Vec3d diff = new Vec3d(
                pos.getX() + 0.5 - x + facing.getFrontOffsetX() / 2.0,
                pos.getY() + 0.5,
                pos.getZ() + 0.5 - z + facing.getFrontOffsetZ() / 2.0
        );

        // find the distance between two points
        double distance = Math.sqrt(diff.x * diff.x + diff.z * diff.z);

        // find the yaw and pitch to the vector
        float yaw = (float) (Math.atan2(diff.z, diff.x) * 180.0 / Math.PI - 90.0);
        float pitch = (float) (Math.atan2(y + mc.player.getEyeHeight() - diff.y, distance) * 180.0 / Math.PI);

        // wrap the degrees to values between -180 and 180
        return new Rotation(MathHelper.wrapDegrees(yaw), MathHelper.wrapDegrees(pitch));
    }

    /**
     * Gets the player interaction reach distance
     * @return the player interaction reach distance
     */
    public float getReachDistance() {
        return mc.playerController.getBlockReachDistance();
    }

    /**
     * Finds all the visible sides of a certain position
     * @param position The position to find all the visible sides of
     * @return List of visible sides
     */
    public List<EnumFacing> getVisibleSides(BlockPos position) {
        List<EnumFacing> visibleSides = new ArrayList<>();

        // pos vector
        Vec3d positionVector = new Vec3d(position).addVector(0.5, 0.5, 0.5);

        // facing
        double facingX = mc.player.getPositionEyes(1).x - positionVector.x;
        double facingY = mc.player.getPositionEyes(1).y - positionVector.y;
        double facingZ = mc.player.getPositionEyes(1).z - positionVector.z;

        // x
        {
            if (facingX < -0.5) {
                visibleSides.add(EnumFacing.WEST);
            }

            else if (facingX > 0.5) {
                visibleSides.add(EnumFacing.EAST);
            }

            else if (!mc.world.getBlockState(position).isFullBlock() || !mc.world.isAirBlock(position)) {
                visibleSides.add(EnumFacing.WEST);
                visibleSides.add(EnumFacing.EAST);
            }
        }

        // y
        {
            if (facingY < -0.5) {
                visibleSides.add(EnumFacing.DOWN);
            }

            else if (facingY > 0.5) {
                visibleSides.add(EnumFacing.UP);
            }

            else {
                visibleSides.add(EnumFacing.DOWN);
                visibleSides.add(EnumFacing.UP);
            }
        }

        // z
        {
            if (facingZ < -0.5) {
                visibleSides.add(EnumFacing.NORTH);
            }

            else if (facingZ > 0.5) {
                visibleSides.add(EnumFacing.SOUTH);
            }

            else if (!mc.world.getBlockState(position).isFullBlock() || !mc.world.isAirBlock(position)) {
                visibleSides.add(EnumFacing.NORTH);
                visibleSides.add(EnumFacing.SOUTH);
            }
        }

        return visibleSides;
    }

    /**
     * If a block is being placed by the interaction manager
     * @return if a block is not done placing
     */
    public boolean isPlacing() {
        return placing;
    }
}