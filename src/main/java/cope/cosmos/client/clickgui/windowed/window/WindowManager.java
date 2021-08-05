package cope.cosmos.client.clickgui.windowed.window;

import cope.cosmos.client.clickgui.windowed.window.windows.ModulesWindow;
import net.minecraft.util.math.Vec2f;

import java.util.Arrays;
import java.util.List;

public class WindowManager {

    private static final List<Window> windows = Arrays.asList(
        new ModulesWindow(new Vec2f(40, 60))
    );

    public static List<Window> getWindows() {
        return windows;
    }
}
