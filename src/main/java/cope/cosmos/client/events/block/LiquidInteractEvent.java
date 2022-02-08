package cope.cosmos.client.events.block;

import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

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
