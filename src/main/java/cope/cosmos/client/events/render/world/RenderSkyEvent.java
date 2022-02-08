package cope.cosmos.client.events.render.world;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.awt.*;

/**
 * Called when the color of sky is calculated
 * @author linustouchtips
 * @since 12/28/2021
 */
@Cancelable
public class RenderSkyEvent extends Event {

    // sky color
    private Color color;

    /**
     * Sets the sky color
     * @param in The new sky color
     */
    public void setColor(Color in) {
        color = in;
    }

    /**
     * Gets the sky color
     * @return The sky color
     */
    public Color getColor() {
        return color;
    }
}
