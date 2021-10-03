package cope.cosmos.client.clickgui.windowed.window.windows.configuration;

import cope.cosmos.client.clickgui.util.Util;
import cope.cosmos.client.clickgui.windowed.window.windows.configuration.SettingComponent;
import cope.cosmos.client.features.setting.Setting;
import net.minecraft.util.math.Vec2f;

public abstract class TypeComponent<T> implements Util {

    private final SettingComponent settingComponent;
    private final Setting<T> setting;

    private Vec2f position;
    private float width;
    private float height;
    private float boundHeight;

    public TypeComponent(SettingComponent settingComponent, Setting<T> setting) {
        this.settingComponent = settingComponent;
        this.setting = setting;
    }

    public abstract void drawType(Vec2f position, float width, float height, float boundHeight);

    public abstract void handleLeftClick();

    public abstract void handleRightClick();

    public abstract void handleKeyPress(char typedCharacter, int key);

    public Setting<T> getSetting() {
        return setting;
    }

    public SettingComponent getSettingComponent() {
        return settingComponent;
    }

    public void setPosition(Vec2f in) {
        position = in;
    }

    public Vec2f getPosition() {
        return position;
    }

    public void setWidth(float in) {
        width = in;
    }

    public float getWidth() {
        return width;
    }

    public void setHeight(float in) {
        height = in;
    }

    public float getHeight() {
        return height;
    }

    public void setBoundHeight(float in) {
        boundHeight = in;
    }

    public float getBoundHeight() {
        return boundHeight;
    }
}
