package cope.cosmos.client.clickgui.util;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.clickgui.windowed.WindowGUI;

public interface Util {

    default boolean mouseOver(float x, float y, float width, float height) {
        if (!Float.isNaN(getGUI().getMouse().getMousePosition().x) && !Float.isNaN(getGUI().getMouse().getMousePosition().y)) {
            return getGUI().getMouse().getMousePosition().x >= x && getGUI().getMouse().getMousePosition().y >= y && getGUI().getMouse().getMousePosition().x <= (x + width) && getGUI().getMouse().getMousePosition().y <= (y + height);
        }

        return false;
    }

    default WindowGUI getGUI() {
        return Cosmos.INSTANCE.getWindowGUI();
    }
}
