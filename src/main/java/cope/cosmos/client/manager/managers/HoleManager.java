package cope.cosmos.client.manager.managers;

import cope.cosmos.client.manager.Manager;
import cope.cosmos.util.Wrapper;

import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings("unused")
public class HoleManager extends Manager implements Wrapper {

    public static final List<Hole> holes = new CopyOnWriteArrayList<>();

    public HoleManager() {
        super("HoleManager", "Manages all nearby holes");
    }

    @Override
    public void onUpdate() {
        holes.clear();
        // holes.addAll(searchHoles());
    }

    /*
    public List<Hole> searchHoles() {
        List<Hole> searchedHoles = new CopyOnWriteArrayList<>();

        for (BlockPos blockPos : BlockUtil.getSurroundingBlocks(mc.player, 6, false)) {
            int resistantSides = 0;
            int unbreakableSides = 0;

            // must be an air block and have a block under it
            if (!mc.world.getBlockState(blockPos).getBlock().equals(Blocks.AIR) || mc.world.getBlockState(blockPos.add(0, -1, 0)).getBlock().equals(Blocks.AIR))
                continue;

            {
                if (BlockUtil.getBlockResistance(blockPos.add(1, 0, 0)).equals(BlockResistance.RESISTANT)) {
                    resistantSides++;
                }

                if (BlockUtil.getBlockResistance(blockPos.add(-1, 0, 0)).equals(BlockResistance.RESISTANT)) {
                    resistantSides++;
                }

                if (BlockUtil.getBlockResistance(blockPos.add(0, 0, 1)).equals(BlockResistance.RESISTANT)) {
                    resistantSides++;
                }

                if (BlockUtil.getBlockResistance(blockPos.add(0, 0, -1)).equals(BlockResistance.RESISTANT)) {
                    resistantSides++;
                }

                if (BlockUtil.getBlockResistance(blockPos.add(1, 0, 0)).equals(BlockResistance.UNBREAKABLE)) {
                    unbreakableSides++;
                }

                if (BlockUtil.getBlockResistance(blockPos.add(-1, 0, 0)).equals(BlockResistance.UNBREAKABLE)) {
                    unbreakableSides++;
                }

                if (BlockUtil.getBlockResistance(blockPos.add(0, 0, 1)).equals(BlockResistance.UNBREAKABLE)) {
                    unbreakableSides++;
                }

                if (BlockUtil.getBlockResistance(blockPos.add(0, 0, -1)).equals(BlockResistance.UNBREAKABLE)) {
                    unbreakableSides++;
                }
            }

            if (unbreakableSides == 4) {
                searchedHoles.add(new Hole(blockPos, Type.BEDROCK));
            }

            if (resistantSides == 4) {
                searchedHoles.add(new Hole(blockPos, Type.OBSIDIAN));
            }

            if (unbreakableSides + resistantSides == 4) {
                searchedHoles.add(new Hole(blockPos, Type.OBSIDIAN));
            }

            if (unbreakableSides == 3) {

            }

            if (resistantSides == 3) {

            }

            if (unbreakableSides + resistantSides == 3) {

            }
        }

        return searchedHoles;
    }

     */

    public enum Type {
        OBSIDIAN(true), BEDROCK(false), DOUBLEOBSIDIANX(true), DOUBLEOBSIDIANZ(true), DOUBLEBEDROCKX(false), DOUBLEBEDROCKZ(false), VOID(false);

        boolean obsidian;

        Type(boolean obsidian) {
            this.obsidian = obsidian;
        }

        public boolean isObsidian() {
            return obsidian;
        }
    }

    public List<Hole> getHoles() {
        return holes;
    }

    public static class Hole {

        private final BlockPos hole;
        private final Type type;

        public Hole(BlockPos hole, Type type) {
            this.hole = hole;
            this.type = type;
        }

        public BlockPos getHole() {
            return hole;
        }

        public Type getType() {
            return type;
        }
    }
}
