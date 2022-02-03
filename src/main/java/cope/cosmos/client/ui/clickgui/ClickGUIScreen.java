package cope.cosmos.client.ui.clickgui;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.client.ClickGUI;
import cope.cosmos.client.ui.clickgui.feature.ClickType;
import cope.cosmos.client.ui.clickgui.feature.features.category.CategoryFrameFeature;
import cope.cosmos.client.ui.util.InterfaceUtil;
import cope.cosmos.client.ui.util.MousePosition;
import cope.cosmos.client.ui.util.ScissorStack;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.Vec2f;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.LinkedList;

/**
 * @author linustouchtips
 * @since 01/29/2022
 */
public class ClickGUIScreen extends GuiScreen implements InterfaceUtil {

    private final MousePosition mouse = new MousePosition(Vec2f.ZERO, false, false, false, false);

    // list of windows
    private final LinkedList<CategoryFrameFeature> categoryFrameFeatures = new LinkedList<>();
    private final ScissorStack scissorStack = new ScissorStack();

    public ClickGUIScreen() {
        // add all categories
        int frameSpace = 0;
        for (Category category : Category.values()) {
            if (!category.equals(Category.HIDDEN)) {
                categoryFrameFeatures.add(new CategoryFrameFeature(category, new Vec2f(10 + frameSpace, 20)));
                frameSpace += 110;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        // draws the default dark background
        drawDefaultBackground();

        categoryFrameFeatures.forEach(categoryFrameFeature -> {
            categoryFrameFeature.drawFeature();
        });

        // find the frame we are focused on
        CategoryFrameFeature focusedFrameFeature = categoryFrameFeatures
                .stream()
                .filter(categoryFrameFeature -> isMouseOver(categoryFrameFeature.getPosition().x, categoryFrameFeature.getPosition().y + categoryFrameFeature.getTitle(), categoryFrameFeature.getWidth(), categoryFrameFeature.getHeight()))
                .findFirst()
                .orElse(null);

        if (focusedFrameFeature != null && Mouse.hasWheel()) {

            // scroll length
            int scroll = Mouse.getDWheel();
            focusedFrameFeature.onScroll(scroll);
        }

        mouse.setLeftClick(false);
        mouse.setRightClick(false);
        mouse.setPosition(new Vec2f(mouseX, mouseY));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        switch (mouseButton) {
            case 0:
                mouse.setLeftClick(true);
                mouse.setLeftHeld(true);

                categoryFrameFeatures.forEach(categoryFrameFeature -> {
                    categoryFrameFeature.onClick(ClickType.LEFT);
                });

                break;
            case 1:
                mouse.setRightClick(true);
                mouse.setRightHeld(true);

                categoryFrameFeatures.forEach(categoryFrameFeature -> {
                    categoryFrameFeature.onClick(ClickType.RIGHT);
                });

                break;
            default:
                break;
        }

        // push frame to the front of the stack
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        if (state == 0) {
            mouse.setLeftHeld(false);
            mouse.setRightHeld(false);

            categoryFrameFeatures.forEach(categoryFrameFeature -> {
                categoryFrameFeature.setDragging(false);
                categoryFrameFeature.setExpanding(false);
            });
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        categoryFrameFeatures.forEach(categoryFrameFeature -> {
            categoryFrameFeature.onType(typedChar);
        });
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        Cosmos.EVENT_BUS.unregister(this);

        // disable the GUI modules, keeps the toggle state consistent with open/close
        ClickGUI.INSTANCE.disable();

        // save our configs when exiting the GUI
        Cosmos.INSTANCE.getPresetManager().save();

        if (mc.entityRenderer.isShaderActive()) {
            mc.entityRenderer.getShaderGroup().deleteShaderGroup();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return ClickGUI.pauseGame.getValue();
    }

    @SubscribeEvent
    public void onRenderHUD(RenderGameOverlayEvent.Pre event) {
        // prevents HUD overlays/elements from being rendered while in the GUI screen
        if (!event.getType().equals(ElementType.TEXT) && !event.getType().equals(ElementType.CHAT)) {
            // event.setCanceled(true);
        }
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
