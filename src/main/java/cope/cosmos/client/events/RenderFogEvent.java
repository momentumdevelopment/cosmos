package cope.cosmos.client.events;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called when fog density is rendered
 * @author linustouchtips
 * @since 12/17/2021
 */
@Cancelable
public class RenderFogEvent extends Event {

    // fog density
    private float density;

    public RenderFogEvent(float density) {
        this.density = density;
    }

    /**
     * Sets the fog's density
     * @param in The fog's new density
     */
    public void setDensity(float in) {
        density = in;
    }

    /**
     * Gets the fog's density
     * @return The fog's density
     */
    public float getDensity() {
        return density;
    }
}
