package cope.cosmos.client.clickgui.windowed;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.clickgui.windowed.window.WindowManager;
import cope.cosmos.client.clickgui.util.MousePosition;
import cope.cosmos.client.features.modules.client.ClickGUI;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.Vec2f;

import java.io.IOException;

public class WindowGUI extends GuiScreen {

    private final MousePosition mouse = new MousePosition(Vec2f.ZERO, false, false, false, false);

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        mouse.setLeftClick(false);
        mouse.setRightClick(false);
        mouse.setMousePosition(new Vec2f(mouseX, mouseY));

        WindowManager.getWindows().forEach(window -> {
            window.drawWindow();
        });
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        switch (mouseButton) {
            case 0:
                mouse.setLeftClick(true);
                mouse.setLeftHeld(true);
                break;
            case 1:
                mouse.setRightClick(true);
                mouse.setRightHeld(true);
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

            WindowManager.getWindows().forEach(window -> {
                window.setDragging(false);
                window.setExpanding(false);
            });
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        // disable the GUI modules, keeps the toggle state consistent with open/close
        ClickGUI.INSTANCE.disable();

        // save our configs when exiting the GUI
        Cosmos.INSTANCE.getPresetManager().save();

        // remove any shaders we are using
        if (mc.entityRenderer.isShaderActive())
            mc.entityRenderer.getShaderGroup().deleteShaderGroup();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return ClickGUI.pauseGame.getValue();
    }

    public MousePosition getMouse() {
        return mouse;
    }
}
