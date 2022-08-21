package cope.cosmos.client.events.entity.player;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called after the onUpdateWalkingPlayer method
 * @author linustouchtips
 * @since 06/30/2022
 */
@Cancelable
public class UpdateWalkingPlayerEvent extends Event {

    // how many times to run the update event
    private int iterations;

    /**
     * Sets the number of iterations
     * @param in The new number of iterations
     */
    public void setIterations(int in) {
        iterations = in;
    }

    /**
     * Gets the iterations
     * @return The number of iterations
     */
    public int getIterations() {
        return iterations;
    }
}
