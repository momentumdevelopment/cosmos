package cope.cosmos.client.ui.clickgui;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.client.ClickGUIModule;
import cope.cosmos.client.ui.clickgui.screens.configuration.component.ClickType;
import cope.cosmos.client.ui.clickgui.screens.configuration.component.components.category.CategoryFrameComponent;
import cope.cosmos.client.ui.clickgui.screens.configuration.taskbar.Taskbar;
import cope.cosmos.client.ui.util.InterfaceWrapper;
import cope.cosmos.client.ui.util.MousePosition;
import cope.cosmos.client.ui.util.ScissorStack;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.Vec2f;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author linustouchtips
 * @since 01/29/2022
 */
public class ClickGUIScreen extends GuiScreen implements InterfaceWrapper {

    private final MousePosition mouse = new MousePosition(Vec2f.ZERO, false, false, false, false);

    // list of windows
    private final LinkedList<CategoryFrameComponent> categoryFrameComponents = new LinkedList<>();

    // scissor stack
    private final ScissorStack scissorStack = new ScissorStack();

    // taskbar
    private final Taskbar taskbar = new Taskbar();

    public ClickGUIScreen() {

        // add all categories
        int frameSpace = 0;
        for (Category category : Category.values()) {
            if (!category.equals(Category.HIDDEN)) {
                categoryFrameComponents.add(new CategoryFrameComponent(category, new Vec2f(10 + frameSpace, 20)));
                frameSpace += 110;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        // draws the default dark background
        try {
            drawDefaultBackground();
        } catch (NullPointerException ignored) {

        }

        categoryFrameComponents.forEach(categoryFrameComponent -> {
            categoryFrameComponent.drawComponent();
        });

        // find the frame we are focused on
        CategoryFrameComponent focusedFrameComponent = categoryFrameComponents
                .stream()
                .filter(categoryFrameComponent -> isMouseOver(categoryFrameComponent.getPosition().x, categoryFrameComponent.getPosition().y + categoryFrameComponent.getTitle(), categoryFrameComponent.getWidth(), categoryFrameComponent.getHeight()))
                .findFirst()
                .orElse(null);

        if (focusedFrameComponent != null && Mouse.hasWheel()) {

            // scroll length
            int scroll = Mouse.getDWheel();
            focusedFrameComponent.onScroll(scroll);
        }

        // draw taskbar
        taskbar.drawComponent();

        mouse.setLeftClick(false);
        mouse.setRightClick(false);
        mouse.setPosition(new Vec2f(mouseX, mouseY));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        // reversed list
        List<CategoryFrameComponent> reverseCategoryFrameComponents = new ArrayList<>();

        categoryFrameComponents.forEach(categoryFrameComponent -> {
            reverseCategoryFrameComponents.add(categoryFrameComponent);
        });

        // reverse
        Collections.reverse(reverseCategoryFrameComponents);

        switch (mouseButton) {
            case 0:
                mouse.setLeftClick(true);
                mouse.setLeftHeld(true);

                // iterate reversed category components
                for (CategoryFrameComponent categoryFrameComponent : reverseCategoryFrameComponents) {
                    if (isMouseOver(categoryFrameComponent.getPosition().x, categoryFrameComponent.getPosition().y, categoryFrameComponent.getWidth(), categoryFrameComponent.getTitle() + categoryFrameComponent.getHeight() + 2)) {

                        // handle click
                        categoryFrameComponent.onClick(ClickType.LEFT);

                        int index = categoryFrameComponents.indexOf(categoryFrameComponent);

                        // push to front
                        categoryFrameComponents.addLast(categoryFrameComponent);
                        categoryFrameComponents.remove(index);

                        break;
                    }
                }

                break;
            case 1:
                mouse.setRightClick(true);
                mouse.setRightHeld(true);

                // iterate reversed category components
                for (CategoryFrameComponent categoryFrameComponent : reverseCategoryFrameComponents) {
                    if (isMouseOver(categoryFrameComponent.getPosition().x, categoryFrameComponent.getPosition().y, categoryFrameComponent.getWidth(), categoryFrameComponent.getTitle() + categoryFrameComponent.getHeight() + 2)) {

                        // handle click
                        categoryFrameComponent.onClick(ClickType.RIGHT);
                        break;
                    }
                }

                break;

            default:
                if (ClickType.getByIdentifier(mouseButton) != null) {
                    for (CategoryFrameComponent categoryFrameComponent : reverseCategoryFrameComponents) {
                        categoryFrameComponent.onClick(ClickType.getByIdentifier(mouseButton));
                    }
                }

                break;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        if (state == 0) {
            mouse.setLeftHeld(false);
            mouse.setRightHeld(false);

            categoryFrameComponents.forEach(categoryFrameComponent -> {
                categoryFrameComponent.setDragging(false);
                categoryFrameComponent.setExpanding(false);
            });
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        categoryFrameComponents.forEach(categoryFrameComponent -> {
            categoryFrameComponent.onType(keyCode);
        });
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        // close frames
        categoryFrameComponents.forEach(categoryFrameComponent -> {
            categoryFrameComponent.setOpen(false);
        });

        Cosmos.EVENT_BUS.unregister(this);

        // disable the GUI modules, keeps the toggle state consistent with open/close
        ClickGUIModule.INSTANCE.disable(true);

        // save our configs when exiting the GUI
        Cosmos.INSTANCE.getConfigManager().saveGUI();

        if (mc.entityRenderer.isShaderActive()) {
            mc.entityRenderer.getShaderGroup().deleteShaderGroup();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return ClickGUIModule.pauseGame.getValue();
    }

    @SubscribeEvent
    public void onRenderHUD(RenderGameOverlayEvent.Pre event) {

        // prevents HUD overlays/elements from being rendered while in the GUI screen
        if (!event.getType().equals(ElementType.TEXT) && !event.getType().equals(ElementType.CHAT)) {
            event.setCanceled(true);
        }
    }

    /**
     * Gets the category frame features in the GUI
     * @return The category frame features in the GUI
     */
    public LinkedList<CategoryFrameComponent> getCategoryFrameComponents() {
        return categoryFrameComponents;
    }

    /**
     * Gets the scissor stack
     * @return The scissor stack
     */
    public ScissorStack getScissorStack() {
        return scissorStack;
    }

    /**
     * Gets the mouse
     * @return The mouse
     */
    public MousePosition getMouse() {
        return mouse;
    }
}
