package cope.cosmos.client.events.entity.player.interact;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called when an item is right-clicked by the player
 * @author linustouchtips
 * @since 12/09/2021
 */
@Cancelable
public class RightClickItemEvent extends Event {

    private final ItemStack itemStack;

    public RightClickItemEvent(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    /**
     * Gets the item being right clicked
     * @return The item being right clicked
     */
    public ItemStack getItemStack() {
        return itemStack;
    }
}
