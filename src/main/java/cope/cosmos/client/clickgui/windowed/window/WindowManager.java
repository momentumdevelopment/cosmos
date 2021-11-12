package cope.cosmos.client.clickgui.windowed.window;

import cope.cosmos.client.clickgui.windowed.window.windows.ConfigurationWindow;
import net.minecraft.util.math.Vec2f;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class WindowManager {

    private final List<Window> windows = new CopyOnWriteArrayList<>();
    private final List<Window> pinnedWindows = new CopyOnWriteArrayList<>();

    public WindowManager() {
        createWindow(new ConfigurationWindow("Configuration", new Vec2f(40, 60)));
    }

    public void pushWindow(Window in) {

    }

    public void createWindow(Window in) {
        windows.add(in);

        if (!pinnedWindows.contains(in)) {
            pinnedWindows.add(in);
        }
    }

    public void removeWindow(Window in) {
        windows.remove(in);

        if (!in.isPinned()) {
            pinnedWindows.remove(in);
        }
    }

    public void purge() {
        windows.clear();
    }

    public List<Window> getWindows() {
        return windows;
    }

    public List<Window> getPinnedWindows() {
        return pinnedWindows;
    }
}
