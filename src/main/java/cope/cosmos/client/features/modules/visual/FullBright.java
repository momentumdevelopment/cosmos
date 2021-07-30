package cope.cosmos.client.features.modules.visual;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;

public class FullBright extends Module {
    public static FullBright INSTANCE;

    public FullBright() {
        super("FullBright", Category.VISUAL, "Brightens up the world");
        INSTANCE = this;
    }

    float previousBright;

    @Override
    public void onEnable() {
        super.onEnable();

        previousBright = mc.gameSettings.gammaSetting;
        mc.gameSettings.gammaSetting = 100;
    }

    @Override
    public void onDisable() {
        super.onDisable();

        mc.gameSettings.gammaSetting = previousBright;
    }
}
