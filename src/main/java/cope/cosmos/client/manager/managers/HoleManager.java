package cope.cosmos.client.manager.managers;

import cope.cosmos.client.manager.Manager;
import cope.cosmos.util.world.BlockUtil;
import cope.cosmos.util.world.BlockUtil.Resistance;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author linustouchtips
 * @since 12/17/2021
 */
public class HoleManager extends Manager {

    // list of holes
    private List<Hole> holes = new CopyOnWriteArrayList<>();

    public HoleManager() {
        super("HoleManager", "Manages all nearby holes");
    }

    /**
     * Standard Holes, player can stand in them to prevent large amounts of explosion damage
     */
    public static final Vec3i[] HOLE = {
            new Vec3i(1, 0, 0),
            new Vec3i(-1, 0, 0),
            new Vec3i(0, 0, 1),
            new Vec3i(0, 0, -1)
    };

    /**
     * Double X Holes, player can stand in the middle of the blocks to prevent placements on these blocks
     */
    public static final Vec3i[] DOUBLE_HOLE_X = {
            new Vec3i(2, 0, 0),
            new Vec3i(-1, 0, 0),
            new Vec3i(0, 0, 1),
            new Vec3i(1, 0, 1),
            new Vec3i(0, 0, -1),
            new Vec3i(1, 0, -1)
    };

    /**
     * Double Z Holes, player can stand in the middle of the blocks to prevent placements on these blocks
     */
    public static final Vec3i[] DOUBLE_HOLE_Z = {
            new Vec3i(1, 0, 0),
            new Vec3i(1, 0, 1),
            new Vec3i(-1, 0, 0),
            new Vec3i(-1, 0, 1),
            new Vec3i(0, 0, 2),
            new Vec3i(0, 0, -1)
    };

    /**
     * Quad Holes, player can stand in the middle of four blocks to prevent placements on these blocks
     */
    public static final Vec3i[] QUAD_HOLE = {
            new Vec3i(2, 0, 0),
            new Vec3i(2, 0, 1),
            new Vec3i(1, 0, 2),
            new Vec3i(0, 0, 2),
            new Vec3i(-1, 0, 0),
            new Vec3i(-1, 0, 1),
            new Vec3i(0, 0, -1),
            new Vec3i(1, 0, -1),
    };

    @Override
    public void onThread() {
        // search for holes on our client thread, process is too CPU intensive to be run on mc thread
        holes = searchHoles();
    }

    /**
     * Search nearby holes
     * @return List of nearby {@link Hole} Holes
     */
    public List<Hole> searchHoles() {

        // list of our found holes
        List<Hole> searchedHoles = new CopyOnWriteArrayList<>();

        // search all blocks in range
        for (BlockPos blockPosition : BlockUtil.getBlocksInArea(mc.player, new AxisAlignedBB(
                -10, -10, -10, 10, 10, 10 // range = 10
        ))) {

            // void holes
            if (BlockUtil.isBreakable(blockPosition) && (mc.player.dimension == -1 ? (blockPosition.getY() == 0 || blockPosition.getY() == 127) : blockPosition.getY() == 0)) {
                searchedHoles.add(new Hole(blockPosition, Type.VOID));
                continue;
            }

            // check air sides
            if (mc.world.getBlockState(blockPosition).getMaterial().isReplaceable()) {

                // check lower positions
                if (!mc.world.getBlockState(blockPosition.add(0, -1, 0)).getMaterial().isReplaceable()) {

                    // boolean to keep track of whether or not the hole is able to be entered
                    boolean standable = false;
                    
                    // check above position
                    if (mc.world.getBlockState(blockPosition.add(0, 1, 0)).getMaterial().isReplaceable() && mc.world.getBlockState(blockPosition.add(0, 2, 0)).getMaterial().isReplaceable()) {
                        standable = true;
                    }

                    // number of sides that are resistant or unbreakable
                    int resistantSides = 0;
                    int unbreakableSides = 0;

                    // check all offsets
                    for (Vec3i holeSide : HOLE) {

                        // offset position by hole side
                        BlockPos holeOffset = blockPosition.add(holeSide);

                        // add resistant side
                        if (BlockUtil.getResistance(holeOffset).equals(Resistance.RESISTANT)) {
                            resistantSides++;
                        }

                        // add unbreakable side
                        else if (BlockUtil.getResistance(holeOffset).equals(Resistance.UNBREAKABLE)) {
                            unbreakableSides++;
                        }

                        else {
                            resistantSides = 0;
                            unbreakableSides = 0;
                            break;
                        }
                    }

                    // reg holes
                    if (standable) {

                        // all unbreakable = bedrock hole
                        if (unbreakableSides == HOLE.length) {
                            searchedHoles.add(new Hole(blockPosition, Type.BEDROCK));
                            continue;
                        }

                        // all resistant = obsidian hole
                        else if (resistantSides == HOLE.length) {
                            searchedHoles.add(new Hole(blockPosition, Type.OBSIDIAN));
                            continue;
                        }

                        // resistant + unbreakable = mixed hole
                        else if (unbreakableSides + resistantSides == HOLE.length) {
                            searchedHoles.add(new Hole(blockPosition, Type.MIXED));
                            continue;
                        }

                        // we didn't find a hole, reset our values and move onto next check
                        else {
                            resistantSides = 0;
                            unbreakableSides = 0;
                        }
                    }

                    // check air sides
                    if (mc.world.getBlockState(blockPosition.add(1, 0, 0)).getMaterial().isReplaceable()) {

                        // check lower positions
                        if (!mc.world.getBlockState(blockPosition.add(1, -1, 0)).getMaterial().isReplaceable()) {

                            // boolean to keep track of whether or not the hole is able to be entered
                            boolean doubleXStandable = false;

                            // check above position
                            if (mc.world.getBlockState(blockPosition.add(0, 1, 0)).getMaterial().isReplaceable() && mc.world.getBlockState(blockPosition.add(0, 2, 0)).getMaterial().isReplaceable() || mc.world.getBlockState(blockPosition.add(1, 1, 0)).getMaterial().isReplaceable() && mc.world.getBlockState(blockPosition.add(1, 2, 0)).getMaterial().isReplaceable()) {
                                doubleXStandable = true;
                            }

                            // check all offsets
                            for (Vec3i holeSide : DOUBLE_HOLE_X) {

                                // offset position by hole side
                                BlockPos holeOffset = blockPosition.add(holeSide);

                                // add resistant side
                                if (BlockUtil.getResistance(holeOffset).equals(Resistance.RESISTANT)) {
                                    resistantSides++;
                                }

                                // add unbreakable side
                                else if (BlockUtil.getResistance(holeOffset).equals(Resistance.UNBREAKABLE)) {
                                    unbreakableSides++;
                                }

                                else {
                                    resistantSides = 0;
                                    unbreakableSides = 0;
                                    break;
                                }
                            }

                            // double holes
                            if (doubleXStandable) {

                                // all unbreakable = bedrock hole
                                if (unbreakableSides == DOUBLE_HOLE_X.length) {
                                    searchedHoles.add(new Hole(blockPosition, Type.DOUBLE_BEDROCK_X));
                                    continue;
                                }

                                // all resistant = obsidian hole
                                else if (resistantSides == DOUBLE_HOLE_X.length) {
                                    searchedHoles.add(new Hole(blockPosition, Type.DOUBLE_OBSIDIAN_X));
                                    continue;
                                }

                                // resistant + unbreakable = mixed hole
                                else if (unbreakableSides + resistantSides == DOUBLE_HOLE_X.length) {
                                    searchedHoles.add(new Hole(blockPosition, Type.DOUBLE_MIXED_X));
                                    continue;
                                }

                                // we didn't find a hole, reset our values and move onto next check
                                else {
                                    resistantSides = 0;
                                    unbreakableSides = 0;
                                }
                            }
                        }
                    }

                    // check air sides
                    if (mc.world.getBlockState(blockPosition.add(0, 0, 1)).getMaterial().isReplaceable()) {

                        // check lower positions
                        if (!mc.world.getBlockState(blockPosition.add(0, -1, 1)).getMaterial().isReplaceable()) {

                            // boolean to keep track of whether or not the hole is able to be entered
                            boolean doubleZStandable = false;

                            // check above position
                            if (mc.world.getBlockState(blockPosition.add(0, 1, 0)).getMaterial().isReplaceable() && mc.world.getBlockState(blockPosition.add(0, 2, 0)).getMaterial().isReplaceable() || mc.world.getBlockState(blockPosition.add(0, 1, 1)).getMaterial().isReplaceable() && mc.world.getBlockState(blockPosition.add(0, 2, 1)).getMaterial().isReplaceable()) {
                                doubleZStandable = true;
                            }

                            // check all offsets
                            for (Vec3i holeSide : DOUBLE_HOLE_Z) {

                                // offset position by hole side
                                BlockPos holeOffset = blockPosition.add(holeSide);

                                // add resistant side
                                if (BlockUtil.getResistance(holeOffset).equals(Resistance.RESISTANT)) {
                                    resistantSides++;
                                }

                                // add unbreakable side
                                else if (BlockUtil.getResistance(holeOffset).equals(Resistance.UNBREAKABLE)) {
                                    unbreakableSides++;
                                }

                                else {
                                    resistantSides = 0;
                                    unbreakableSides = 0;
                                    break;
                                }
                            }

                            // double holes
                            if (doubleZStandable) {
                                // all unbreakable = bedrock hole
                                if (unbreakableSides == DOUBLE_HOLE_Z.length) {
                                    searchedHoles.add(new Hole(blockPosition, Type.DOUBLE_BEDROCK_Z));
                                    continue;
                                }

                                // all resistant = obsidian hole
                                else if (resistantSides == DOUBLE_HOLE_Z.length) {
                                    searchedHoles.add(new Hole(blockPosition, Type.DOUBLE_OBSIDIAN_Z));
                                    continue;
                                }

                                // resistant + unbreakable = mixed hole
                                else if (unbreakableSides + resistantSides == DOUBLE_HOLE_Z.length) {
                                    searchedHoles.add(new Hole(blockPosition, Type.DOUBLE_MIXED_Z));
                                    continue;
                                }

                                // we didn't find a hole, reset our values and move onto next check
                                else {
                                    resistantSides = 0;
                                    unbreakableSides = 0;
                                }
                            }
                        }
                    }

                    // check air sides
                    if (mc.world.getBlockState(blockPosition.add(0, 0, 1)).getMaterial().isReplaceable() && mc.world.getBlockState(blockPosition.add(1, 0, 0)).getMaterial().isReplaceable() && mc.world.getBlockState(blockPosition.add(1, 0, 1)).getMaterial().isReplaceable()) {

                        // check lower positions
                        if (!mc.world.getBlockState(blockPosition.add(0, -1, 1)).getMaterial().isReplaceable() && !mc.world.getBlockState(blockPosition.add(1, -1, 0)).getMaterial().isReplaceable() && !mc.world.getBlockState(blockPosition.add(1, -1, 1)).getMaterial().isReplaceable()) {

                            // boolean to keep track of whether or not the hole is able to be entered
                            boolean quadStandable;

                            // check above position
                            int stopBlocks = 0;
                            if (mc.world.getBlockState(blockPosition.add(0, 1, 0)).getMaterial().isReplaceable() && mc.world.getBlockState(blockPosition.add(0, 2, 0)).getMaterial().isReplaceable()) {
                                stopBlocks++;
                            }

                            if (mc.world.getBlockState(blockPosition.add(1, 1, 0)).getMaterial().isReplaceable() && mc.world.getBlockState(blockPosition.add(1, 2, 0)).getMaterial().isReplaceable()) {
                                stopBlocks++;
                            }

                            if (mc.world.getBlockState(blockPosition.add(0, 1, 1)).getMaterial().isReplaceable() && mc.world.getBlockState(blockPosition.add(0, 2, 1)).getMaterial().isReplaceable()) {
                                stopBlocks++;
                            }

                            if (mc.world.getBlockState(blockPosition.add(1, 1, 1)).getMaterial().isReplaceable() && mc.world.getBlockState(blockPosition.add(1, 2, 1)).getMaterial().isReplaceable()) {
                                stopBlocks++;
                            }

                            // quads need at least two or three stop blocks
                            quadStandable = stopBlocks != 3;

                            // check all offsets
                            for (Vec3i holeSide : QUAD_HOLE) {

                                // offset position by hole side
                                BlockPos holeOffset = blockPosition.add(holeSide);

                                // add resistant side
                                if (BlockUtil.getResistance(holeOffset).equals(Resistance.RESISTANT)) {
                                    resistantSides++;
                                }

                                // add unbreakable side
                                else if (BlockUtil.getResistance(holeOffset).equals(Resistance.UNBREAKABLE)) {
                                    unbreakableSides++;
                                }

                                else {
                                    resistantSides = 0;
                                    unbreakableSides = 0;
                                    break;
                                }
                            }

                            // quad holes
                            if (quadStandable) {
                                // all unbreakable = bedrock hole
                                if (unbreakableSides == QUAD_HOLE.length) {
                                    searchedHoles.add(new Hole(blockPosition, Type.QUAD_BEDROCK));
                                }

                                // all resistant = obsidian hole
                                else if (resistantSides == QUAD_HOLE.length) {
                                    searchedHoles.add(new Hole(blockPosition, Type.QUAD_OBSIDIAN));
                                }

                                // resistant + unbreakable = mixed hole
                                else if (unbreakableSides + resistantSides == QUAD_HOLE.length) {
                                    searchedHoles.add(new Hole(blockPosition, Type.QUAD_MIXED));
                                }
                            }
                        }
                    }
                }
            }
        }

        return searchedHoles;
    }

    /**
     * Gets all nearby holes
     * @return List of all nearby {@link Hole} Holes
     */
    public List<Hole> getHoles() {
        return holes;
    }

    /**
     * Checks whether or not a position is a hole
     * @param in The position to check
     * @return Whether or not a position is a hole
     */
    public boolean isHole(BlockPos in) {
        // check each of the hole offsets
        for (Vec3i holeOffset : HOLE) {

            // the side we are checking
            BlockPos holeSide = in.add(holeOffset);

            // check the side's resistance
            if (!BlockUtil.getResistance(holeSide).equals(Resistance.RESISTANT) && !BlockUtil.getResistance(holeSide).equals(Resistance.UNBREAKABLE)) {
                return false;
            }
        }

        return true;
    }

    // wtf lel
    public enum Type {

        /**
         * Resistant hole -> Resistant to explosions but breakable
         */
        OBSIDIAN,

        /**
         * Mixed hole -> Mix of resistant and unbreakable blocks
         */
        MIXED,

        /**
         * Unbreakable hole -> Unbreakable blocks, resistant to explosions
         */
        BEDROCK,

        /**
         * Resistant double hole -> Resistant to explosions but breakable
         */
        DOUBLE_OBSIDIAN_X,

        /**
         * Resistant double hole -> Resistant to explosions but breakable
         */
        DOUBLE_OBSIDIAN_Z,

        /**
         * Mixed double hole -> Mix of resistant and unbreakable blocks
         */
        DOUBLE_MIXED_X,

        /**
         * Mixed double hole -> Mix of resistant and unbreakable blocks
         */
        DOUBLE_MIXED_Z,

        /**
         * Unbreakable double hole -> Unbreakable blocks, resistant to explosions
         */
        DOUBLE_BEDROCK_X,

        /**
         * Unbreakable double hole -> Unbreakable blocks, resistant to explosions
         */
        DOUBLE_BEDROCK_Z,

        /**
         * Resistant quad hole -> Resistant to explosions but breakable
         */
        QUAD_OBSIDIAN,

        /**
         * Mixed quad hole -> Mix of resistant and unbreakable blocks
         */
        QUAD_MIXED,

        /**
         * Unbreakable quad hole -> Unbreakable blocks, resistant to explosions
         */
        QUAD_BEDROCK,

        /**
         * Void hole -> Hole to outside world bounds
         */
        VOID
    }

    public static class Hole {

        // hole info
        private final BlockPos hole;
        private final Type type;

        public Hole(BlockPos hole, Type type) {
            this.hole = hole;
            this.type = type;
        }

        /**
         * Gets the hole's position
         * @return The hole's position
         */
        public BlockPos getHole() {
            return hole;
        }

        /**
         * Gets the hole's type
         * @return The hole's type
         */
        public Type getType() {
            return type;
        }

        /**
         * Checks whether or not a hole is a double hole
         * @return whether or not the hole is a double hole
         */
        public boolean isDouble() {
            return type.equals(Type.DOUBLE_OBSIDIAN_X) || type.equals(Type.DOUBLE_MIXED_X) || type.equals(Type.DOUBLE_BEDROCK_X) || type.equals(Type.DOUBLE_OBSIDIAN_Z) || type.equals(Type.DOUBLE_MIXED_Z) || type.equals(Type.DOUBLE_BEDROCK_Z);
        }

        /**
         * Checks whether or not a hole is a quad hole
         * @return whether or not the hole is a quad hole
         */
        public boolean isQuad() {
            return type.equals(Type.QUAD_OBSIDIAN) || type.equals(Type.QUAD_MIXED) || type.equals(Type.QUAD_BEDROCK);
        }
    }
}