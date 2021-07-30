package cope.cosmos.client.clickgui.cosmos.window;

import cope.cosmos.client.clickgui.cosmos.window.windows.CategoryWindow;
import cope.cosmos.client.features.modules.Category;
import net.minecraft.util.math.Vec2f;

import java.util.Arrays;
import java.util.List;

public class WindowManager {

    private static Window focusedWindow;

    private static final List<Window> windows = Arrays.asList(
            new CategoryWindow(new Vec2f(570, 70), Category.CLIENT),
            new CategoryWindow(new Vec2f(460, 70), Category.VISUAL),
            new CategoryWindow(new Vec2f(350, 70), Category.PLAYER),
            new CategoryWindow(new Vec2f(240, 70), Category.MISC),
            new CategoryWindow(new Vec2f(130, 70), Category.MOVEMENT),
            new CategoryWindow(new Vec2f(20, 70), Category.COMBAT)
    );

    public static Window getFocusedWindow() {
        return focusedWindow;
    }

    public static void setFocusedWindow(Window in) {
        focusedWindow = in;
    }

    public static List<Window> getWindows() {
        return windows;
    }
}
