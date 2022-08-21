package cope.cosmos.client.events.render.gui.tooltip;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class RenderTooltipEvent extends Event {

    // shulker item
    private final ItemStack itemStack;

    // position
    private final int x, y;

    public RenderTooltipEvent(ItemStack itemStack, int x, int y) {
        this.itemStack = itemStack;
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the shulker item
     * @return The shulker item stack
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Gets the x position
     * @return The x position
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the y position
     * @return The y position
     */
    public int getY() {
        return y;
    }
}
