package cope.cosmos.client.clickgui.windowed.window.windows.configuration.types;

import cope.cosmos.client.clickgui.util.Util;
import cope.cosmos.client.features.setting.Setting;
import net.minecraft.util.math.Vec2f;

public abstract class TypeComponent<T> implements Util {

    private final Setting<T> setting;

    private Vec2f position;
    private float width;
    private float height;

    public TypeComponent(Setting<T> setting) {
        this.setting = setting;
    }

    public abstract void drawType(Vec2f position, float width);

    public abstract void handleLeftClick();

    public abstract void handleRightClick();

    public abstract void handleKeyPress(char typedCharacter, int key);

    public Setting<T> getSetting() {
        return setting;
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
}
