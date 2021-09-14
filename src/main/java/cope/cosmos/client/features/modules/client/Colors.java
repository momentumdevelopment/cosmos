package cope.cosmos.client.features.modules.client;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;

import java.awt.*;

public class Colors extends Module {
    public Colors() {
        super("Colors", Category.CLIENT, "The universal color for the client");
    }

    public static Setting<Color> color = new Setting<>("Color", "The primary color for the client", new Color(139, 126, 212, 255));
}
