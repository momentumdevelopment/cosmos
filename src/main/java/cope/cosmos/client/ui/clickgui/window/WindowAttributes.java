package cope.cosmos.client.ui.clickgui.window;

public class WindowAttributes {
    private boolean closable;
    private boolean resizable;

    public WindowAttributes(final boolean closable, final boolean resizable) {
        this.closable = closable;
        this.resizable = resizable;
    }

    public boolean isClosable() {
        return this.closable;
    }

    public boolean isResizable() {
        return this.resizable;
    }

    public void setClosable(boolean closable) {
        this.closable = closable;
    }

    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }

}
