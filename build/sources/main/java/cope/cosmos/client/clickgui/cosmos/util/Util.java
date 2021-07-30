package cope.cosmos.client.clickgui.cosmos.util;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.clickgui.cosmos.CosmosGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public interface Util {

    default boolean mouseOver(float x, float y, float width, float height) {
        if (!Float.isNaN(getGUI().getMouse().getMousePosition().x) && !Float.isNaN(getGUI().getMouse().getMousePosition().y)) {
            return getGUI().getMouse().getMousePosition().x >= x && getGUI().getMouse().getMousePosition().y >= y && getGUI().getMouse().getMousePosition().x <= (x + width) && getGUI().getMouse().getMousePosition().y <= (y + height);
        }

        return false;
    }

    default CosmosGUI getGUI() {
        return Cosmos.INSTANCE.getCosmosGUI();
    }

    default int getGlobalAnimation() {
        return Cosmos.INSTANCE.getCosmosGUI().getGlobalAnimation();
    }
}
