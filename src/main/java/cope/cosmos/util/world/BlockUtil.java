package cope.cosmos.util.world;

import cope.cosmos.util.Wrapper;
import cope.cosmos.util.player.PlayerUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author linustouchtips
 * @since 05/06/2021
 */
public class BlockUtil implements Wrapper {

    // All blocks that are resistant to explosions
    public static final List<Block> resistantBlocks = Arrays.asList(
            Blocks.OBSIDIAN,
            Blocks.ANVIL,
            Blocks.ENCHANTING_TABLE,
            Blocks.ENDER_CHEST,
            Blocks.BEACON
    );

    // All blocks that are unbreakable with tools in survival mode
    public static final List<Block> unbreakableBlocks = Arrays.asList(
            Blocks.BEDROCK,
            Blocks.COMMAND_BLOCK,
            Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.END_PORTAL_FRAME,
            Blocks.BARRIER,
            Blocks.PORTAL
    );

    /**
     * Finds the if a given position is breakable
     * @param position The position to check
     * @return Whether or not the given position is breakable
     */
    public static boolean isBreakable(BlockPos position) {
        return !getResistance(position).equals(Resistance.UNBREAKABLE);
    }

    /**
     * Checks if a block is replaceable
     * @param pos the position to check
     * @return if this block pos can be placed at
     */
    public static boolean isReplaceable(BlockPos pos) {
        return mc.world.getBlockState(pos).getMaterial().isReplaceable();
    }

    /**
     * Finds the resistance of a given position
     * @param position The position to find the resistance for
     * @return The {@link Resistance} resistance of the given position
     */
    public static Resistance getResistance(BlockPos position) {

        // the block at the given position
        Block block = mc.world.getBlockState(position).getBlock();

        // idk why this would be null but it throws errors
        if (block != null) {

            // find resistance
            if (resistantBlocks.contains(block)) {
                return Resistance.RESISTANT;
            }

            else if (unbreakableBlocks.contains(block)) {
                return Resistance.UNBREAKABLE;
            }

            else if (block.getDefaultState().getMaterial().isReplaceable()) {
                return Resistance.REPLACEABLE;
            }

            else {
                return Resistance.BREAKABLE;
            }
        }

        return Resistance.NONE;
    }

    /**
     * Gets the distance to the center of the block
     * @param in The block to get the distance to
     * @return The distance to the center of the block
     */
    public static double getDistanceToCenter(EntityPlayer player, BlockPos in) {

        // distances
        double dX = in.getX() + 0.5 - player.posX;
        double dY = in.getY() + 0.5 - player.posY;
        double dZ = in.getZ() + 0.5 - player.posZ;

        // distance to center
        return StrictMath.sqrt((dX * dX) + (dY * dY) + (dZ * dZ));
    }

    /**
     * Gets all blocks in range from a specified player
     * @param player The player to find the surrounding blocks (anchor)
     * @param area The area range to consider blocks
     * @return A list of the surrounding blocks
     */
    public static List<BlockPos> getBlocksInArea(EntityPlayer player, AxisAlignedBB area) {
        if (player != null) {

            // list of nearby blocks
            List<BlockPos> blocks = new ArrayList<>();

            // iterate through all surrounding blocks
            for (double x = StrictMath.floor(area.minX); x <= StrictMath.ceil(area.maxX); x++) {
                for (double y = StrictMath.floor(area.minY); y <= StrictMath.ceil(area.maxY); y++) {
                    for (double z = StrictMath.floor(area.minZ); z <= StrictMath.ceil(area.maxZ); z++) {

                        // the current position
                        BlockPos position = PlayerUtil.getPosition().add(x, y, z);

                        // check distance to block
                        if (getDistanceToCenter(player, position) >= area.maxX) {
                            continue;
                        }

                        // add the block to our list
                        blocks.add(position);
                    }
                }
            }

            return blocks;
        }

        // rofl, threading is so funny
        return new ArrayList<>();
    }

    // the resistance level of the block
    public enum Resistance {

        /**
         * Blocks that are able to be replaced by other blocks
         */
        REPLACEABLE,

        /**
         * Blocks that are able to be broken with tools in survival mode
         */
        BREAKABLE,

        /**
         * Blocks that are resistant to explosions
         */
        RESISTANT,

        /**
         * Blocks that are unbreakable with tools in survival mode
         */
        UNBREAKABLE,

        /**
         * Null equivalent
         */
        NONE
    }
}
