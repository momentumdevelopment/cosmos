package cope.cosmos.client.ui.util;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.ui.clickgui.ClickGUI;
import cope.cosmos.client.ui.clickgui.window.WindowManager;

/**
 * @author linustouchtips
 * @since 08/23/2022
 */
public interface InterfaceUtil {

    /**
     * Checks if the mouse is over a region
     * @param x The lower x
     * @param y The lower y
     * @param width The upper x
     * @param height The upper y
     * @return Whether the mouse is over the given region
     */
    default boolean isMouseOver(float x, float y, float width, float height) {
        return !Float.isNaN(getGUI().getMouse().getMousePosition().x) && !Float.isNaN(getGUI().getMouse().getMousePosition().y) && getGUI().getMouse().getMousePosition().x >= x && getGUI().getMouse().getMousePosition().y >= y && getGUI().getMouse().getMousePosition().x <= (x + width) && getGUI().getMouse().getMousePosition().y <= (y + height);
    }

    /**
     * Gets the Click Gui window manager
     * @return the Click Gui window manager
     */
    default WindowManager getManager() {
        return getGUI().getManager();
    }

    /**
     * Gets the Click Gui screen
     * @return The Click Gui screen
     */
    default ClickGUI getGUI() {
        return Cosmos.INSTANCE.getClickGUI();
    }
}
