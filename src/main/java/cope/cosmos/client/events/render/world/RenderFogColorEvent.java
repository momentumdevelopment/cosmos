package cope.cosmos.client.events.render.world;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.awt.*;

/**
 * Called when the color of the fog is calculated
 * @author linustouchtips
 * @since 12/28/2021
 */
@Cancelable
public class RenderFogColorEvent extends Event {

    // fog color
    private Color color;

    /**
     * Gets the fog color
     * @return The fog color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the fog color
     * @param in The new fog color
     */
    public void setColor(Color in) {
        color = in;
    }
}
