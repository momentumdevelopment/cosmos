package cope.cosmos.client.ui.clickgui;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.ui.clickgui.element.Element;
import cope.cosmos.client.ui.clickgui.feature.CategoryWindowFeature;
import cope.cosmos.client.ui.clickgui.feature.TabbedWindowFeature;
import cope.cosmos.client.ui.clickgui.taskbar.Taskbar;
import cope.cosmos.client.ui.clickgui.window.Window;
import cope.cosmos.client.ui.util.MousePosition;
import cope.cosmos.util.client.ColorUtil;
import cope.cosmos.util.render.RenderUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.Vec2f;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClickGuiScreen extends GuiScreen {
    public static final MousePosition MOUSE = new MousePosition(Vec2f.ZERO, false, false, false, false);
    private Taskbar taskbar;
    private final CopyOnWriteArrayList<Element> listElements = new CopyOnWriteArrayList<>();

    @Override
    public void initGui() {
        super.initGui();
        taskbar = new Taskbar(this);
        reset();
    }

    public void reset() {
        listElements.clear();
        listElements.add(new Window(50, 50, 500, 300, "Cosmos", this, new TabbedWindowFeature("Cosmos", 499, 299, Arrays.stream(Category.values()).filter(i -> !i.toString().equalsIgnoreCase("hidden")).map(i -> new CategoryWindowFeature(0, 0, i)).toArray(CategoryWindowFeature[]::new))));
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        RenderUtil.drawGradientVerticalRect(0, 0, RenderUtil.displayWidth(), RenderUtil.displayHeight(), 0x00000000, ColorUtil.getPrimaryAlphaColor(0xaa).getRGB());
        MOUSE.setMousePosition(new Vec2f(mouseX, mouseY));
        taskbar.draw(mouseX, mouseY);
        for (Element w: listElements) {
            if (w.isVisible()) {
                w.draw(mouseX, mouseY);
            }
        }
    }

    @Override public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        pollInFront(mouseX, mouseY);
        switch (mouseButton) {
            case 0: {
                MOUSE.setLeftClick(true);
                MOUSE.setLeftHeld(true);
            }
            break;
            case 1: {
                MOUSE.setRightClick(true);
                MOUSE.setRightHeld(true);
            }
            break;
            default: break;
        }
        if (!listElements.isEmpty()) {
            listElements.get(0).mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    private void pollInFront(int mouseX, int mouseY) {
        Element obj = null;
        for (Element element : listElements) {
            if (element != null) {
                if (element.isWithin(mouseX, mouseY)) {
                    if (shouldClickElement(element, mouseX, mouseY) && element.isVisible()) {
                        element.setupDrag(mouseX, mouseY);
                        obj = element;
                        break;
                    }
                }
            }
        }
        if (obj != null) {
            elementToFront(obj);
        }
    }

    private boolean shouldClickElement(Element elementIn, int mouseX, int mouseY) {
        boolean yes = elementIn.isWithin(mouseX, mouseY);
        for (Element element: listElements) {
            if (element == elementIn) continue;
            if (element.isWithin(mouseX, mouseY) && listElements.indexOf(element) > listElements.indexOf(elementIn)) {
                yes = false;
            }
        }
        return yes;
    }

    private void elementToFront(Element element) {
        listElements.remove(element);
        listElements.add(listElements.size(), element);
    }

    @Override public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (taskbar.mouseClicked(mouseX, mouseY, mouseButton)) return;
        switch (mouseButton) {
            case 0: {
                MOUSE.setLeftClick(false);
                MOUSE.setLeftHeld(false);
            }
            break;
            case 1: {
                MOUSE.setRightClick(false);
                MOUSE.setRightHeld(false);
            }
            break;
            default: break;
        }
        if (!listElements.isEmpty()) {
            listElements.get(0).mouseReleased(mouseX, mouseY, mouseButton);
        }
    }

    @Override public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (Mouse.getEventDWheel() != 0 && !listElements.isEmpty()) {
            listElements.get(0).mouseScrolled(MOUSE.getMousePosition().x, MOUSE.getMousePosition().y, Mouse.getEventDWheel());
        }
    }

    @Override public void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        if (!listElements.isEmpty()) {
            listElements.get(0).keyTyped(typedChar, keyCode);
        }
    }

    @Override public boolean doesGuiPauseGame() {
        return false;
    }

    public CopyOnWriteArrayList<Element> getListElements() {
        return listElements;
    }
}
