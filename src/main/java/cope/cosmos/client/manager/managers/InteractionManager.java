package cope.cosmos.client.manager.managers;

import cope.cosmos.asm.mixins.accessor.IMinecraft;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.util.holder.Rotation;
import cope.cosmos.util.holder.Rotation.Rotate;
import cope.cosmos.util.player.AngleUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author linustouchtips
 * @since 11/16/2021
 */
public class InteractionManager extends Manager {
    public InteractionManager() {
        super("InteractionManager", "Manages all player interactions");
    }

    // list of blocks which need to be shift clicked to be placed on
    public static final List<Block> sneakBlocks = Arrays.asList(
            Blocks.ENDER_CHEST,
            Blocks.CHEST,
            Blocks.TRAPPED_CHEST,
            Blocks.CRAFTING_TABLE,
            Blocks.ANVIL, // :troll:
            Blocks.BREWING_STAND,
            Blocks.HOPPER,
            Blocks.DROPPER,
            Blocks.DISPENSER,
            Blocks.TRAPDOOR,
            Blocks.ENCHANTING_TABLE,
            Blocks.WHITE_SHULKER_BOX,
            Blocks.ORANGE_SHULKER_BOX,
            Blocks.MAGENTA_SHULKER_BOX,
            Blocks.LIGHT_BLUE_SHULKER_BOX,
            Blocks.YELLOW_SHULKER_BOX,
            Blocks.LIME_SHULKER_BOX,
            Blocks.PINK_SHULKER_BOX,
            Blocks.GRAY_SHULKER_BOX,
            Blocks.SILVER_SHULKER_BOX,
            Blocks.CYAN_SHULKER_BOX,
            Blocks.PURPLE_SHULKER_BOX,
            Blocks.BLUE_SHULKER_BOX,
            Blocks.BROWN_SHULKER_BOX,
            Blocks.GREEN_SHULKER_BOX,
            Blocks.RED_SHULKER_BOX,
            Blocks.BLACK_SHULKER_BOX
            // Blocks.COMMAND_BLOCK,
            // Blocks.CHAIN_COMMAND_BLOCK
    );

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
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                // mc.player.setSprinting(false);
            }

            // sneak if the block is not right-clickable
            boolean sneak = sneakBlocks.contains(mc.world.getBlockState(directionOffset).getBlock()) && !mc.player.isSneaking();
            if (sneak) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                // mc.player.setSneaking(true);
            }

            // vector to the block
            Vec3d interactVector = new Vec3d(directionOffset).addVector(0.5, 0.5, 0.5).add(new Vec3d(direction.getOpposite().getDirectionVec()).scale(0.5));

            // Rotation oldRotation = getCosmos().getRotationManager().getServerRotation();

            // rotate to block
            if (!rotate.equals(Rotate.NONE)) {
                Rotation blockAngles = AngleUtil.calculateAngles(interactVector);

                // rotate via packet, server should confirm instantly?
                switch (rotate) {
                    case CLIENT:
                        mc.player.rotationYaw = blockAngles.getYaw();
                        mc.player.rotationYawHead = blockAngles.getYaw();
                        mc.player.rotationPitch = blockAngles.getPitch();
                        break;
                    case PACKET:

                        // force a rotation - should this be done?
                        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(blockAngles.getYaw(), blockAngles.getPitch(), mc.player.onGround));

                        // submit to rotation manager
                        getCosmos().getRotationManager().setRotation(blockAngles);

                        // ((IEntityPlayerSP) mc.player).setLastReportedYaw(blockAngles[0]);
                        // ((IEntityPlayerSP) mc.player).setLastReportedPitch(blockAngles[1]);
                        break;
                }
            }

            // right click direction offset block
            EnumActionResult placeResult = mc.playerController.processRightClickBlock(mc.player, mc.world, directionOffset, direction.getOpposite(), interactVector, EnumHand.MAIN_HAND);

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
            if (placeResult != EnumActionResult.FAIL) {
                mc.player.swingArm(EnumHand.MAIN_HAND);
                ((IMinecraft) mc).setRightClickDelayTimer(4);
//
//                // force a rotation - should this be done?
//                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(oldRotation.getYaw(), oldRotation.getPitch(), mc.player.onGround));
//
//                // submit to rotation manager
//                getCosmos().getRotationManager().setRotation(oldRotation);
                break;
            }

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
     */
    public void placeBlockWithEntities(BlockPos position, Rotate rotate, boolean strict) {
        for (EnumFacing direction : EnumFacing.values()) {

            // find a block to place against
            BlockPos directionOffset = position.offset(direction);

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
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                // mc.player.setSprinting(false);
            }

            // sneak if the block is not right-clickable
            boolean sneak = sneakBlocks.contains(mc.world.getBlockState(directionOffset).getBlock()) && !mc.player.isSneaking();
            if (sneak) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                // mc.player.setSneaking(true);
            }

            // vector to the block
            Vec3d interactVector = new Vec3d(directionOffset).addVector(0.5, 0.5, 0.5).add(new Vec3d(direction.getOpposite().getDirectionVec()).scale(0.5));

            // Rotation oldRotation = getCosmos().getRotationManager().getServerRotation();

            // rotate to block
            if (!rotate.equals(Rotate.NONE)) {
                Rotation blockAngles = AngleUtil.calculateAngles(interactVector);

                // rotate via packet, server should confirm instantly?
                switch (rotate) {
                    case CLIENT:
                        mc.player.rotationYaw = blockAngles.getYaw();
                        mc.player.rotationYawHead = blockAngles.getYaw();
                        mc.player.rotationPitch = blockAngles.getPitch();
                        break;
                    case PACKET:

                        // force a rotation - should this be done?
                        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(blockAngles.getYaw(), blockAngles.getPitch(), mc.player.onGround));

                        // submit to rotation manager
                        // getCosmos().getRotationManager().setRotation(blockAngles);

                        // ((IEntityPlayerSP) mc.player).setLastReportedYaw(blockAngles[0]);
                        // ((IEntityPlayerSP) mc.player).setLastReportedPitch(blockAngles[1]);
                        break;
                }
            }

            // right click direction offset block
            EnumActionResult placeResult = mc.playerController.processRightClickBlock(mc.player, mc.world, directionOffset, direction.getOpposite(), interactVector, EnumHand.MAIN_HAND);

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
            if (placeResult != EnumActionResult.FAIL) {
                mc.player.swingArm(EnumHand.MAIN_HAND);
                ((IMinecraft) mc).setRightClickDelayTimer(4);
                break;
            }

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
     * Gets all the blocks that need to be shift clicked
     * @return All the blocks that need to be shift clicked
     */
    public List<Block> getSneakBlocks() {
        return sneakBlocks;
    }

    /**
     * If a block is being placed by the interaction manager
     * @return if a block is not done placing
     */
    public boolean isPlacing() {
        return placing;
    }
}