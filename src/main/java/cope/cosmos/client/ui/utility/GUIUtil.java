package cope.cosmos.client.ui.utility;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.ui.windows.WindowGUI;
import cope.cosmos.client.ui.windows.window.WindowManager;

public interface GUIUtil {

    default boolean mouseOver(float x, float y, float width, float height) {
        if (!Float.isNaN(getGUI().getMouse().getMousePosition().x) && !Float.isNaN(getGUI().getMouse().getMousePosition().y)) {
            return getGUI().getMouse().getMousePosition().x >= x && getGUI().getMouse().getMousePosition().y >= y && getGUI().getMouse().getMousePosition().x <= (x + width) && getGUI().getMouse().getMousePosition().y <= (y + height);
        }

        return false;
    }

    default WindowManager getManager() {
        return getGUI().getManager();
    }

    default WindowGUI getGUI() {
        return Cosmos.INSTANCE.getWindowGUI();
    }
}
