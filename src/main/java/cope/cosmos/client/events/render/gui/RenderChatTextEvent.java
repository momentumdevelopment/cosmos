package cope.cosmos.client.events.render.gui;

import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called when the chat text is being rendered
 * @author linustouchtips
 * @since 08/24/2022
 */
public class RenderChatTextEvent extends Event {

    // render positions
    public int x, y;

    public RenderChatTextEvent(int x, int y) {
        this.x = x;
        this.y = y;
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
