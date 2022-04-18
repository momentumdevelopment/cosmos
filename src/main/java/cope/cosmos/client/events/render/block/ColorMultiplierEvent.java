package cope.cosmos.client.events.render.block;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called on the color multiplier for block
 * @author linustouchtips
 * @since 04/18/2022
 */
@Cancelable
public class ColorMultiplierEvent extends Event {

    // block opacity
    private int opacity;

    public ColorMultiplierEvent(int opacity) {
        this.opacity = opacity;
    }

    /**
     * Sets the color opacity
     * @param in The new color opacity
     */
    public void setOpacity(int in) {
        opacity = in;
    }

    /**
     * Gets the color opacity
     * @return The color opacity
     */
    public int getOpacity() {
        return opacity;
    }
}
