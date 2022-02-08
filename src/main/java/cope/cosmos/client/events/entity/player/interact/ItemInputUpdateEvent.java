package cope.cosmos.client.events.entity.player.interact;

import net.minecraft.util.MovementInput;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called when the player inputs on an item
 * @author linustouchtips
 * @since 12/09/2021
 */
public class ItemInputUpdateEvent extends Event {

    // player movement input
    private final MovementInput movementInput;

    public ItemInputUpdateEvent(MovementInput movementInput) {
        this.movementInput = movementInput;
    }

    /**
     * Gets the current player's movement input
     * @return The current player's movement input
     */
    public MovementInput getMovementInput() {
        return movementInput;
    }
}
