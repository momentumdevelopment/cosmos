package cope.cosmos.client.events;

import net.minecraft.block.state.IBlockState;
import cope.cosmos.event.annotation.Cancelable;
import cope.cosmos.event.listener.Event;

@Cancelable
public class LiquidInteractEvent extends Event {

    private final IBlockState blockState;
    private final boolean liquidLevel;

    public LiquidInteractEvent(IBlockState blockState, boolean liquidLevel) {
        this.blockState = blockState;
        this.liquidLevel = liquidLevel;
    }

    public IBlockState getBlockState() {
        return blockState;
    }

    public boolean getLiquidLevel() {
        return liquidLevel;
    }
}
