package cope.cosmos.client.events.render.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.eventhandler.Event;

public class GuiScreenClosedEvent extends Event {

    private GuiScreen currentScreen;

    public GuiScreenClosedEvent(GuiScreen screen) {
        this.currentScreen = screen;
    }

    public GuiScreen getCurrentScreen() {
        return currentScreen;
    }

}
