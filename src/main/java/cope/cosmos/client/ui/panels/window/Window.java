package cope.cosmos.client.ui.panels.window;

public abstract class Window {

    private boolean expanding;
    private boolean dragging;

    public abstract void drawWindow();

    public abstract void handleLeftClick(int mouseX, int mouseY);

    public abstract void handleLeftDrag(int mouseX, int mouseY);

    public abstract void handleRightClick(int mouseX, int mouseY);

    public abstract void handleKeyPress(char typedCharacter, int key);

    public abstract void handleScroll(int scroll);

    public boolean isExpanding() {
        return expanding;
    }

    public void setExpanding(boolean in) {
        expanding = in;
    }

    public boolean isDragging() {
        return dragging;
    }

    public void setDragging(boolean in) {
        dragging = in;
    }
}
