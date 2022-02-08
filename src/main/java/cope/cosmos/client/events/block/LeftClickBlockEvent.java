package cope.cosmos.client.events.block;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called when the player left clicks a block
 * @author linustouchtips
 * @since 12/09/2021
 */
public class LeftClickBlockEvent extends Event {

    // block info
    private final BlockPos blockPos;
    private final EnumFacing blockFace;

    public LeftClickBlockEvent(BlockPos blockPos, EnumFacing blockFace) {
        this.blockPos = blockPos;
        this.blockFace = blockFace;
    }

    /**
     * Gets the position of the block
     * @return The position of the block
     */
    public BlockPos getPos() {
        return blockPos;
    }

    /**
     * Gets the face of the block
     * @return The face of the block
     */
    public EnumFacing getFace() {
        return blockFace;
    }
}
