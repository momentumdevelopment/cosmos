package cope.cosmos.client.clickgui.windowed;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.clickgui.util.GUIUtil;
import cope.cosmos.client.clickgui.windowed.taskbar.Taskbar;
import cope.cosmos.client.clickgui.windowed.window.WindowManager;
import cope.cosmos.client.clickgui.util.MousePosition;
import cope.cosmos.client.features.modules.client.ClickGUI;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.Vec2f;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

import java.io.IOException;

@SuppressWarnings("unused")
public class WindowGUI extends GuiScreen implements GUIUtil {

    private final WindowManager windowManager = new WindowManager();
    private final MousePosition mouse = new MousePosition(Vec2f.ZERO, false, false, false, false);

    private final Taskbar taskbar = new Taskbar();

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        mouse.setLeftClick(false);
        mouse.setRightClick(false);
        mouse.setMousePosition(new Vec2f(mouseX, mouseY));

        windowManager.getWindows().forEach(window -> {
            window.drawWindow();

            if (window.isInteractable()) {
                if (mouseOver(window.getPosition().x, window.getPosition().y + window.getBar(), window.getWidth(), window.getHeight()) && Mouse.hasWheel()) {
                    window.handleScroll(Mouse.getDWheel());
                }
            }
        });

        taskbar.drawTaskbar();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        switch (mouseButton) {
            case 0:
                mouse.setLeftClick(true);
                mouse.setLeftHeld(true);

                windowManager.getWindows().forEach(window -> {
                    try {
                        if (window.isInteractable()) {
                            window.handleLeftClick();

                            // push to the top
                            windowManager.pushWindow(window);
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                });

                taskbar.handleLeftClick();
                break;
            case 1:
                mouse.setRightClick(true);
                mouse.setRightHeld(true);

                windowManager.getWindows().forEach(window -> {
                    try {
                        if (window.isInteractable()) {
                            window.handleRightClick();

                            // push to the top
                            windowManager.pushWindow(window);
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                });

                taskbar.handleRightClick();
                break;
            default:
                break;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        if (state == 0) {
            mouse.setLeftHeld(false);
            mouse.setRightHeld(false);

            windowManager.getWindows().forEach(window -> {
                window.setDragging(false);
                window.setExpanding(false);
            });
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        windowManager.getWindows().forEach(window -> {
            try {
                if (window.isInteractable()) {
                    window.handleKeyPress(typedChar, keyCode);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        // disable the GUI modules, keeps the toggle state consistent with open/close
        ClickGUI.INSTANCE.disable();

        // save our configs when exiting the GUI
        Cosmos.INSTANCE.getPresetManager().save();

        // remove any shaders we are using
        if (mc.entityRenderer.isShaderActive()) {
            mc.entityRenderer.getShaderGroup().deleteShaderGroup();
        }

        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return ClickGUI.pauseGame.getValue();
    }

    @SubscribeEvent
    public void onRenderHUD(RenderGameOverlayEvent.Pre event) {
        if (!event.getType().equals(ElementType.TEXT) && !event.getType().equals(ElementType.CHAT)) {
            event.setCanceled(true);
        }
    }

    public WindowManager getManager() {
        return windowManager;
    }

    public Taskbar getTaskbar() {
        return taskbar;
    }

    public MousePosition getMouse() {
        return mouse;
    }
}
