package cope.cosmos.util.world;

import net.minecraft.util.math.BlockPos;

public class Hole {

    private final BlockPos hole;
    private final Type type;

    public Hole(BlockPos hole, Type type) {
        this.hole = hole;
        this.type = type;
    }

    public BlockPos getHole() {
        return this.hole;
    }

    public Type getType() {
        return this.type;
    }

    public enum Type {
        OBSIDIAN(true), BEDROCK(false), DOUBLEOBSIDIANX(true), DOUBLEOBSIDIANZ(true), DOUBLEBEDROCKX(false), DOUBLEBEDROCKZ(false), VOID(false);

        boolean obsidian;

        Type(boolean obsidian) {
            this.obsidian = obsidian;
        }

        public boolean isObsidian() {
            return this.obsidian;
        }
    }
}
