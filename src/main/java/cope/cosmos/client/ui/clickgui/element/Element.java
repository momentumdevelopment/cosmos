package cope.cosmos.client.ui.clickgui.element;

import cope.cosmos.client.ui.clickgui.window.Window;

public class Element {

    protected long openTime;
    protected float x, y, width, height;
    protected boolean dragging;
    protected boolean visible = true;

    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        return true;
    }

    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        return true;
    }

    public void mouseScrolled(float mouseX, float mouseY, int scroll) {

    }

    public void keyTyped(char key, int keyCode) {

    }

    public void draw(int mouseX, int mouseY) {

    }

    public void renderTooltip(Window window, int mouseX, int mouseY) {

    }

    public boolean isWithin(float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void setupDrag(float mouseX, float mouseY) {

    }

    public boolean isDragging() {
        return dragging;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    public float getHeight() {
        return height;
    }

    public float getWidth() {
        return width;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public long getOpenTime() {
        return openTime;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setOpenTime(long openTime) {
        this.openTime = openTime;
    }
}
