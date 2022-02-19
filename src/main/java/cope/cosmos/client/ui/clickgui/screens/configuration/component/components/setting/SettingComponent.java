package cope.cosmos.client.ui.clickgui.screens.configuration.component.components.setting;

import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.ui.clickgui.screens.configuration.component.ClickType;
import cope.cosmos.client.ui.clickgui.screens.DrawableComponent;
import cope.cosmos.client.ui.clickgui.screens.configuration.component.components.module.ModuleComponent;

/**
 * @author linustouchtips
 * @param <T> The setting type
 * @since 01/31/2022
 */
public class SettingComponent<T> extends DrawableComponent {

    // parent feature and setting
    private final ModuleComponent moduleComponent;
    private final Setting<T> setting;

    // component height
    public int HEIGHT = 14;

    public SettingComponent(ModuleComponent moduleComponent, Setting<T> setting) {
        this.moduleComponent = moduleComponent;
        this.setting = setting;
    }

    @Override
    public void drawComponent() {

    }

    @Override
    public void onClick(ClickType in) {

    }

    @Override
    public void onType(int in) {

    }

    @Override
    public void onScroll(int in) {

    }

    /**
     * Sets the feature height
     * @param in The new feature height
     */
    public void setHeight(int in) {
        HEIGHT = in;
    }

    /**
     * Gets the feature height
     * @return The feature height
     */
    public int getHeight() {
        return HEIGHT;
    }

    /**
     * Gets the parent module feature
     * @return The parent module feature
     */
    public ModuleComponent getModuleComponent() {
        return moduleComponent;
    }

    /**
     * Gets the associated setting
     * @return The associated setting
     */
    public Setting<T> getSetting() {
        return setting;
    }
}
