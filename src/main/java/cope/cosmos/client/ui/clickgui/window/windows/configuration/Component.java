package cope.cosmos.client.ui.clickgui.window.windows.configuration;

import cope.cosmos.client.ui.util.InterfaceUtil;
import net.minecraft.util.math.Vec2f;

public abstract class Component implements InterfaceUtil {

    private Vec2f position;
    private float width;
    private float height;

    private boolean visible = true;

    public abstract void drawComponent(Vec2f position, float width);

    public abstract void handleLeftClick();

    public abstract void handleRightClick();

    public abstract void handleKeyPress(char typedCharacter, int key);

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

    public void setVisible(boolean in) {
        visible = in;
    }

    public boolean isVisible() {
        return visible;
    }
}
