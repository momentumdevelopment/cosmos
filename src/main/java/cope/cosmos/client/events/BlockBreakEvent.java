package cope.cosmos.client.events;

import cope.cosmos.event.annotation.Cancelable;
import cope.cosmos.event.listener.Event;
import net.minecraft.util.math.BlockPos;

@Cancelable
public class BlockBreakEvent extends Event {

    private final BlockPos blockPos;

    public BlockBreakEvent(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }
}
