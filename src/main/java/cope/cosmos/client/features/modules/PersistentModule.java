package cope.cosmos.client.features.modules;

import java.util.function.Supplier;

/**
 * @author linustouchtips
 * @since 04/01/2022
 */
public class PersistentModule extends Module {

    /**
     * Default module in {@link cope.cosmos.client.ui.clickgui.ClickGUIScreen}
     * @param name The name of the module
     * @param category The category of the module
     * @param description The description of the module
     */
    public PersistentModule(String name, Category category, String description) {
        super(name, category, description);
        enabled = true;
    }

    /**
     * Default module in {@link cope.cosmos.client.ui.clickgui.ClickGUIScreen}
     * @param name The name of the module
     * @param aliases The aliases of the module
     * @param category The category of the module
     * @param description The description of the module
     */
    public PersistentModule(String name, String[] aliases, Category category, String description) {
        super(name, aliases, category, description);
        enabled = true;
    }

    /**
     * Default module in {@link cope.cosmos.client.ui.clickgui.ClickGUIScreen} with info attached to {@link cope.cosmos.client.features.modules.client.HUDModule}
     * @param name The name of the module
     * @param category The category of the module
     * @param description The description of the module
     * @param info The HUD info
     */
    public PersistentModule(String name, Category category, String description, Supplier<String> info) {
        super(name, category, description, info);
        enabled = true;
    }

    /**
     * Default module in {@link cope.cosmos.client.ui.clickgui.ClickGUIScreen} with info attached to {@link cope.cosmos.client.features.modules.client.HUDModule}
     * @param name The name of the module
     * @param aliases The aliases of the module
     * @param category The category of the module
     * @param description The description of the module
     * @param info The HUD info
     */
    public PersistentModule(String name, String[] aliases, Category category, String description, Supplier<String> info) {
        super(name, aliases, category, description, info);
        enabled = true;
    }

    @Override
    public void enable(boolean in) {
        // prevent enabling
    }

    @Override
    public void disable(boolean in) {
        // prevent disabling
    }
}
