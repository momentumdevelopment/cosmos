package cope.cosmos.client.ui.panels.navigation;

public abstract class Navigation {

    public float SCREEN_WIDTH;
    public float SCREEN_HEIGHT;

    public abstract void drawNavigation();

    public abstract void handleLeftClick(int mouseX, int mouseY);

    public abstract void handleRightClick(int mouseX, int mouseY);

    public abstract void handleKeyPress(char typedCharacter, int key);

    public abstract void handleScroll(int scroll);
}
