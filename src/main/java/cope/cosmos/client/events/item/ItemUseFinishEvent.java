package cope.cosmos.client.events.item;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called when the player finishes using an item
 * @author linustouchtips
 * @since 10/25/2022
 */
@Cancelable
public class ItemUseFinishEvent extends Event {

    // item in use
    private final ItemStack itemStack;

    public ItemUseFinishEvent(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    /**
     * Gets the item in use
     * @return The item in use
     */
    public ItemStack getItemStack() {
        return itemStack;
    }
}
