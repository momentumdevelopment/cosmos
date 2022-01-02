package cope.cosmos.util.world;

import cope.cosmos.util.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
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
            Blocks.BARRIER
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
     * Finds the resistance of a given position
     * @param position The position to find the resistance for
     * @return The {@link Resistance} resistance of the given position
     */
    public static Resistance getResistance(BlockPos position) {
        // the block at the given position
        Block block = mc.world.getBlockState(position).getBlock();

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

    /**
     * Gets all blocks in range from a specified player
     * @param player The player to find the surrounding blocks (anchor)
     * @param range The range to consider a block
     * @param motion Whether or not to take into account player motion
     * @return A list of the surrounding blocks
     */
    public static List<BlockPos> getSurroundingBlocks(EntityPlayer player, double range, boolean motion) {
        if (player != null) {
            // list of nearby blocks
            List<BlockPos> blocks = new ArrayList<>();

            // ranges
            double rangeX = Math.round(range);
            double rangeY = Math.round(range);
            double rangeZ = Math.round(range);

            // add motion to reach
            if (motion) {
                rangeX += mc.player.motionX;
                rangeY += mc.player.motionY;
                rangeZ += mc.player.motionZ;
            }

            // iterate through all surrounding blocks
            for (double x = -rangeX; x <= rangeX; x++) {
                for (double y = -rangeY; y <= rangeY; y++) {
                    for (double z = -rangeZ; z <= rangeZ; z++) {

                        // the current position
                        BlockPos position = player.getPosition().add(x, y, z);

                        // get distance to block
                        if (mc.player.getDistance(position.getX() + 0.5, position.getY() + 1, position.getZ() + 0.5) >= range) {
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
        UNBREAKABLE
    }
}
