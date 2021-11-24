package cope.cosmos.client.manager.managers;

import cope.cosmos.client.manager.Manager;
import cope.cosmos.util.Wrapper;
import cope.cosmos.util.combat.EnemyUtil;
import cope.cosmos.util.world.BlockUtil;
import cope.cosmos.util.world.BlockUtil.Resistance;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HoleManager extends Manager implements Wrapper {

    private List<Hole> holes = new CopyOnWriteArrayList<>();
    private List<Entity> holeEntities = new CopyOnWriteArrayList<>();

    public HoleManager() {
        super("HoleManager", "Manages all nearby holes");
    }

    public static final Vec3i[] HOLE = {
            new Vec3i(1, 0, 0),
            new Vec3i(-1, 0, 0),
            new Vec3i(0, 0, 1),
            new Vec3i(0, 0, -1),
    };

    @Override
    public void onThread() {
        holes = searchHoles();
        holeEntities = searchHoleEntities();
    }

    public List<Hole> searchHoles() {
        List<Hole> searchedHoles = new CopyOnWriteArrayList<>();

        for (BlockPos blockPos : BlockUtil.getSurroundingBlocks(mc.player, 10, false)) {
            // number of sides that are resistant, unbreakable, or air
            int resistantSides = 0;
            int unbreakableSides = 0;
            int airSides = 0;

            // air side
            Side airSide = null;

            if (BlockUtil.isBreakable(blockPos) && (mc.player.dimension == -1 ? (blockPos.getY() == 0 || blockPos.getY() == 127) : blockPos.getY() == 0)) {
                searchedHoles.add(new Hole(blockPos, Type.VOID));
                continue;
            }

            // must be an air block & be able to fit a player & have a block below it
            if (mc.world.getBlockState(blockPos).getBlock().equals(Blocks.AIR) && mc.world.getBlockState(blockPos.up()).getBlock().equals(Blocks.AIR) && !mc.world.getBlockState(blockPos.down()).getBlock().equals(Blocks.AIR)) {
                // count sides
                for (Vec3i side : HOLE) {
                    if (BlockUtil.getResistance(blockPos.add(side)).equals(Resistance.RESISTANT)) {
                        resistantSides++;
                    }

                    else if (BlockUtil.getResistance(blockPos.add(side)).equals(Resistance.UNBREAKABLE)) {
                        unbreakableSides++;
                    }

                    else if (BlockUtil.getResistance(blockPos.add(side)).equals(Resistance.REPLACEABLE)) {
                        airSides++;

                        EnumFacing facing = getFacingFromVector(side);

                        if (!facing.equals(EnumFacing.SOUTH) && !facing.equals(EnumFacing.WEST)) {
                            airSide = new Side(blockPos.add(side), facing);
                        }
                    }
                }

                // too many air sides
                if (airSides > 1) {
                    continue;
                }

                // reg holes
                if (unbreakableSides == 4) {
                    searchedHoles.add(new Hole(blockPos, Type.BEDROCK));
                }

                else if (resistantSides == 4) {
                    searchedHoles.add(new Hole(blockPos, Type.OBSIDIAN));
                }

                else if (unbreakableSides + resistantSides == 4) {
                    searchedHoles.add(new Hole(blockPos, Type.MIXED));
                }

                // double holes
                if (airSide != null) {
                    for (Vec3i side : HOLE) {
                        if (getFacingFromVector(side).equals(airSide.getFacing().getOpposite())) {
                            continue;
                        }

                        if (BlockUtil.getResistance(airSide.getSide().add(side)).equals(Resistance.RESISTANT)) {
                            resistantSides++;
                        }

                        else if (BlockUtil.getResistance(airSide.getSide().add(side)).equals(Resistance.UNBREAKABLE)) {
                            unbreakableSides++;
                        }
                    }

                    if (!mc.world.getBlockState(airSide.getSide().down()).getBlock().equals(Blocks.AIR) && mc.world.getBlockState(airSide.getSide().up()).getBlock().equals(Blocks.AIR)) {
                        if (unbreakableSides == 6) {
                            searchedHoles.add(new Hole(blockPos, airSide.getFacing().equals(EnumFacing.EAST) ? Type.DOUBLEBEDROCKX : Type.DOUBLEBEDROCKZ));
                        }

                        else if (resistantSides == 6) {
                            searchedHoles.add(new Hole(blockPos, airSide.getFacing().equals(EnumFacing.EAST) ? Type.DOUBLEOBSIDIANX : Type.DOUBLEOBSIDIANZ));
                        }

                        else if (unbreakableSides + resistantSides == 6) {
                            searchedHoles.add(new Hole(blockPos, airSide.getFacing().equals(EnumFacing.EAST) ? Type.DOUBLEMIXEDX : Type.DOUBLEMIXEDZ));
                        }
                    }
                }
            }
        }

        return searchedHoles;
    }

    public List<Entity> searchHoleEntities() {
        List<Entity> searchedHoleEntities = new CopyOnWriteArrayList<>();

        for (Entity entity : mc.world.loadedEntityList) {
            if (entity.isDead || EnemyUtil.isDead(entity)) {
                continue;
            }

            for (Hole hole : holes) {
                if (entity.getPosition().equals(hole.getHole())) {
                    searchedHoleEntities.add(entity);
                }
            }
        }

        return searchedHoleEntities;
    }

    public EnumFacing getFacingFromVector(Vec3i in) {
        if (in.equals(new Vec3i(1, 0, 0))) {
            return EnumFacing.EAST;
        }

        if (in.equals(new Vec3i(-1, 0, 0))) {
            return EnumFacing.WEST;
        }

        if (in.equals(new Vec3i(0, 0, 1))) {
            return EnumFacing.NORTH;
        }

        if (in.equals(new Vec3i(0, 0, -1))) {
            return EnumFacing.SOUTH;
        }

        return null;
    }

    public List<Hole> getHoles() {
        return holes;
    }

    public List<Entity> getHoleEntities() {
        return holeEntities;
    }

    public boolean isHoleEntity(Entity in) {
        return getHoleEntities().contains(in);
    }

    public enum Type {
        OBSIDIAN, MIXED, BEDROCK, DOUBLEOBSIDIANX, DOUBLEOBSIDIANZ, DOUBLEMIXEDX, DOUBLEMIXEDZ, DOUBLEBEDROCKX, DOUBLEBEDROCKZ, VOID
    }

    public static class Side {

        private final BlockPos side;
        private final EnumFacing facing;

        public Side(BlockPos side, EnumFacing facing) {
            this.side = side;
            this.facing = facing;
        }

        public BlockPos getSide() {
            return side;
        }

        public EnumFacing getFacing() {
            return facing;
        }
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

        public boolean isDouble() {
            return type.equals(Type.DOUBLEOBSIDIANX) || type.equals(Type.DOUBLEMIXEDX) || type.equals(Type.DOUBLEBEDROCKX) || type.equals(Type.DOUBLEOBSIDIANZ) || type.equals(Type.DOUBLEMIXEDZ) || type.equals(Type.DOUBLEBEDROCKZ);
        }
    }
}
