package cope.cosmos.client.features.modules;

import java.util.function.Supplier;

/**
 * Module that provides a global client service
 * @author linustouchtips
 * @since 08/29/2022
 * @param <S> The value type for the service
 */
public class ServiceModule<S> extends Module {

    // the task to run
    private S task;

    /**
     * Default module in {@link cope.cosmos.client.ui.clickgui.ClickGUIScreen}
     * @param name The name of the module
     * @param category The category of the module
     * @param description The description of the module
     */
    public ServiceModule(String name, Category category, String description) {
        super(name, category, description);
    }

    /**
     * Default module in {@link cope.cosmos.client.ui.clickgui.ClickGUIScreen}
     * @param name The name of the module
     * @param aliases The aliases of the module
     * @param category The category of the module
     * @param description The description of the module
     */
    public ServiceModule(String name, String[] aliases, Category category, String description) {
        super(name, aliases, category, description);
    }

    /**
     * Default module in {@link cope.cosmos.client.ui.clickgui.ClickGUIScreen} with info attached to {@link cope.cosmos.client.features.modules.client.HUDModule}
     * @param name The name of the module
     * @param category The category of the module
     * @param description The description of the module
     * @param info The HUD info
     */
    public ServiceModule(String name, Category category, String description, Supplier<String> info) {
        super(name, category, description, info);
    }

    /**
     * Default module in {@link cope.cosmos.client.ui.clickgui.ClickGUIScreen} with info attached to {@link cope.cosmos.client.features.modules.client.HUDModule}
     * @param name The name of the module
     * @param aliases The aliases of the module
     * @param category The category of the module
     * @param description The description of the module
     * @param info The HUD info
     */
    public ServiceModule(String name, String[] aliases, Category category, String description, Supplier<String> info) {
        super(name, aliases, category, description, info);
    }

    /**
     * Calls the service
     * @param in Provide the service info
     */
    public S call(Runnable in) {
        in.run();
        return task = null;
    }

    /**
     * Checks if the module is done running the task
     * @return Whether the module is done running the task
     */
    public boolean isTaskComplete() {
        return task == null;
    }

    /**
     * Checks if the module is running the task
     * @param override Whether to override the module's task
     * @return Whether the module is running the task
     */
    public boolean isRunningTask(boolean override) {
        return isActive() && !override;
    }
}
