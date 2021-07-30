package cope.cosmos.client.clickgui.cosmos.panel;

import net.minecraft.util.math.Vec2f;

@SuppressWarnings("unused")
public abstract class Panel {

    public final float WIDTH = 150;
    public final float TITLE = 19;
    public final float BAR = 2;
    public final float HEIGHT = 200;

    public abstract void drawPanel(Vec2f position);

    public abstract void handleLeftClick(int mouseX, int mouseY);

    public abstract void handleRightClick(int mouseX, int mouseY);

    public abstract void handleKeyPress(char typedCharacter, int key);

    public abstract void handleScroll(int scroll);

    public float getWidth() {
        return WIDTH;
    }

    public float getHeight() {
        return HEIGHT;
    }
}
