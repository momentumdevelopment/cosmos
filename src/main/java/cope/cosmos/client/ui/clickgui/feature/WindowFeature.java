package cope.cosmos.client.ui.clickgui.feature;

import cope.cosmos.client.ui.clickgui.window.Window;
import cope.cosmos.util.render.RenderUtil;

public class WindowFeature {

    private final String name;
    /* init as -10000 to avoid artifacts when setting up for the first time */
    public float x = -10000;
    public float y = -10000;
    public float width;
    public float height;
    public String tooltip;

    public WindowFeature(String name, int width, int height) {
        this.name = name;
        this.width = width;
        this.height = height;
    }

    public void draw(Window window, int mouseX, int mouseY) {
        /* render name + bounds */
        RenderUtil.drawRect(this.x, this.y, this.width, this.height, 0xff35353f);
    }

    protected boolean isWithin(int mouseX, int mouseY) {
        return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height;
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isWithin(mouseX, mouseY)) {
            return true;
        }
        return false;
    }

    public boolean mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (isWithin(mouseX, mouseY)) {
            return true;
        }
        return false;
    }

    public void mouseScrolled(float mouseX, float mouseY, int amount) {

    }

    public void keyTyped(char typedChar, int keyCode) {

    }

    public String getName() {
        return name;
    }
}
