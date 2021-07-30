package cope.cosmos.client.events;

import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class LiquidInteractEvent extends Event {

    private IBlockState blockState;
    private boolean liquidLevel;

    public LiquidInteractEvent(IBlockState blockState, boolean liquidLevel) {
        this.blockState = blockState;
        this.liquidLevel = liquidLevel;
    }

    public IBlockState getBlockState() {
        return this.blockState;
    }

    public boolean getLiquidLevel() {
        return this.liquidLevel;
    }
}
