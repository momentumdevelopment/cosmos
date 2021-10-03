package cope.cosmos.client.clickgui.windowed.window;

import cope.cosmos.client.clickgui.windowed.window.windows.ConfigurationWindow;
import net.minecraft.util.math.Vec2f;

import java.util.ArrayList;
import java.util.List;

public class WindowManager {

    private final List<Window> windows = new ArrayList<>();

    public WindowManager() {
        createWindow(new ConfigurationWindow("Configuration", new Vec2f(40, 60)));
    }

    public void createWindow(Window in) {
        windows.add(in);
    }

    public void purgeWindow(Window in) {
        windows.remove(in);
    }

    public void purgeAll() {
        windows.clear();
    }

    public List<Window> getWindows() {
        return windows;
    }
}
