package cope.cosmos.client.clickgui.windowed.window.windows;

import cope.cosmos.client.clickgui.windowed.window.TabbedWindow;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.setting.Setting;
import net.minecraft.util.math.Vec2f;

public class ModulesWindow extends TabbedWindow {
    public ModulesWindow(String name, Vec2f position) {
        super(name, position);

        // add each of the categories as a tab
        for (Category category : Category.values()) {
            getTabs().add(new Tab<>(Setting.formatEnum(category), category));
        }

        // set our current tab as the first category
        setTab(getTabs().get(0));
    }

    @Override
    public void drawWindow() {
        super.drawWindow();

    }
}
