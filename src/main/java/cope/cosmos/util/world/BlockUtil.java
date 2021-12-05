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

public class BlockUtil implements Wrapper {

    public static List<BlockPos> getSurroundingBlocks(EntityPlayer player, double blockRange, boolean motion) {
        if (player == null) { // bruh momentum
            return new ArrayList<>(); // rofl, threading is so funny
        }

        List<BlockPos> nearbyBlocks = new ArrayList<>();

        int rangeX = (int) (Math.round(blockRange) + (motion ? player.motionX : 0));
        int rangeY = (int) (Math.round(blockRange) + (motion ? player.motionY : 0));
        int rangeZ = (int) (Math.round(blockRange) + (motion ? player.motionZ : 0));

        for (int x = -rangeX; x <= rangeX; x++) {
            for (int y = -rangeY; y <= rangeY; y++) {
                for (int z = -rangeZ; z <= rangeZ; z++) {
                    BlockPos pos = player.getPosition().add(x, y, z);
                    if (mc.player.getDistance(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5) >= blockRange) {
                        continue;
                    }

                    nearbyBlocks.add(pos);
                }
            }
        }

        return nearbyBlocks;
    }

    public static double getNearestBlockBelow() {
        for (double y = mc.player.posY; y > 0.0; y -= 0.001) {
            if (mc.world.getBlockState(new BlockPos(mc.player.posX, y, mc.player.posZ)).getBlock() instanceof BlockSlab || mc.world.getBlockState(new BlockPos(mc.player.posX, y, mc.player.posZ)).getBlock().getDefaultState().getCollisionBoundingBox(mc.world, new BlockPos(0, 0, 0)) == null)
                continue;

            return y;
        }

        return -1;
    }

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
