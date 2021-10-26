package cope.cosmos.client.manager.managers;

import cope.cosmos.client.features.modules.visual.HoleESP;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.util.Wrapper;

import cope.cosmos.util.world.BlockUtil;
import cope.cosmos.util.world.HoleUtil;
import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.util.*;

@SuppressWarnings("unused")
public class HoleManager extends Manager implements Wrapper {

    public static final Map<Hole, Color> holes = new HashMap<>();

    private boolean search;

    public HoleManager() {
        super("HoleManager", "Manages all nearby holes");
    }

    @Override
    public void onRender3D() {
        holes.clear();
    }

    @Override
    public void onThread() {
        if (nullCheck()) {
            holes.putAll(searchHoles());
        }
    }

    public Map<Hole, Color> searchHoles() {
        Map<Hole, Color> searchedHoles = new HashMap<>();

        for (BlockPos potentialHole : BlockUtil.getSurroundingBlocks(mc.player, 20, false)) {
             if (HoleUtil.isVoidHole(potentialHole.down())) {
                searchedHoles.put(new Hole(potentialHole.down(), Type.VOID), HoleESP.voidColor.getValue());
                continue;
            }

            if (HoleUtil.isBedRockHole(potentialHole)) {
                searchedHoles.put(new Hole(potentialHole, Type.BEDROCK), HoleESP.bedrockColor.getValue());
            }

            else if (HoleUtil.isObsidianHole(potentialHole)) {
                searchedHoles.put(new Hole(potentialHole, Type.OBSIDIAN), HoleESP.obsidianColor.getValue());
            }

            if (HoleUtil.isDoubleBedrockHoleX(potentialHole.west())) {
                searchedHoles.put(new Hole(potentialHole.west(), Type.DOUBLEBEDROCKX), HoleESP.bedrockColor.getValue());
            }

            else if (HoleUtil.isDoubleBedrockHoleZ(potentialHole.north())) {
                searchedHoles.put(new Hole(potentialHole.north(), Type.DOUBLEBEDROCKZ), HoleESP.bedrockColor.getValue());
            }

            else if (HoleUtil.isDoubleObsidianHoleX(potentialHole.west())) {
                searchedHoles.put(new Hole(potentialHole.west(), Type.DOUBLEOBSIDIANX), HoleESP.obsidianColor.getValue());
            }

            else if (HoleUtil.isDoubleObsidianHoleZ(potentialHole.north())) {
                searchedHoles.put(new Hole(potentialHole.north(), Type.DOUBLEOBSIDIANZ), HoleESP.obsidianColor.getValue());
            }
        }

        return searchedHoles;
    }

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

    public Map<Hole, Color> getHoles() {
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
