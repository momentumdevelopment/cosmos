package cope.cosmos.client.features.modules.client;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;

import java.awt.*;

public class Colors extends Module {
    public static Colors INSTANCE;

    public Colors() {
        super("Colors", Category.CLIENT, "The universal color for the client");
        setDrawn(false);
        setExempt(true);
        INSTANCE = this;
    }

    public static Setting<Color> color = new Setting<>("Color", "The primary color for the client", new Color(118, 98, 224, 255));
}
