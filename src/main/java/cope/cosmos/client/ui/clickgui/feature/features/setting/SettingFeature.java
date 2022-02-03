package cope.cosmos.client.ui.clickgui.feature.features.setting;

import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.ui.clickgui.feature.ClickType;
import cope.cosmos.client.ui.clickgui.feature.DrawableFeature;
import cope.cosmos.client.ui.clickgui.feature.features.module.ModuleFeature;

/**
 * @author linustouchtips
 * @param <T> The setting type
 * @since 01/31/2022
 */
public class SettingFeature<T> extends DrawableFeature {

    // parent feature and setting
    private final ModuleFeature moduleFeature;
    private final Setting<T> setting;

    // component height
    public int HEIGHT = 14;

    public SettingFeature(ModuleFeature moduleFeature, Setting<T> setting) {
        this.moduleFeature = moduleFeature;
        this.setting = setting;
    }

    @Override
    public void drawFeature() {

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
    public ModuleFeature getModuleFeature() {
        return moduleFeature;
    }

    /**
     * Gets the associated setting
     * @return The associated setting
     */
    public Setting<T> getSetting() {
        return setting;
    }
}
