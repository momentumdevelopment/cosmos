package cope.cosmos.client.ui.panels.component;

import cope.cosmos.client.ui.panels.window.windows.CategoryWindow;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.managment.managers.AnimationManager;
import net.minecraft.util.math.Vec2f;

import java.util.ArrayList;
import java.util.List;

public abstract class Component {

    public final float WIDTH = 100;
    public final float BAR = 2;
    public final float HEIGHT = 14;

    private final CategoryWindow parentWindow;
    private final Module module;

    private AnimationManager animationManager;

    private final List<SettingComponent<?>> settingComponents = new ArrayList<>();

    public Component(Module module, CategoryWindow parentWindow) {
        this.module = module;
        this.parentWindow = parentWindow;

        if (module != null)
            animationManager = new AnimationManager(200, false);
    }

    public abstract void drawComponent(Vec2f position);

    public abstract void handleLeftClick(int mouseX, int mouseY);

    public abstract void handleLeftDrag(int mouseX, int mouseY);

    public abstract void handleRightClick(int mouseX, int mouseY);

    public abstract void handleKeyPress(char typedCharacter, int key);

    public abstract void handleScroll(int scroll);

    public CategoryWindow getParentWindow() {
        return parentWindow;
    }

    public Module getModule() {
        return module;
    }

    public AnimationManager getAnimation() {
        return animationManager;
    }

    public List<SettingComponent<?>> getSettingComponents() {
        return settingComponents;
    }
}
