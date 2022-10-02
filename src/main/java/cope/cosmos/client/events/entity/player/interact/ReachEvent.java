package cope.cosmos.client.events.entity.player.interact;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called when the player's block reach distance is called
 * @author linustouchtips
 * @since 05/04/2021
 */
@Cancelable
public class ReachEvent extends Event {

    // reach value
    private float reach;

    /**
     * Sets the player reach
     * @param reach The new player reach
     */
    public void setReach(float reach) {
        this.reach = reach;
    }

    /**
     * Gets the player reach
     * @return The player reach
     */
    public float getReach() {
        return reach;
    }
}
