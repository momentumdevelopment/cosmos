package cope.cosmos.client.clickgui.cosmos;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.clickgui.cosmos.navigation.navs.ControlNavigation;
import cope.cosmos.client.clickgui.cosmos.navigation.navs.PlayerNavigation;
import cope.cosmos.client.clickgui.cosmos.util.Util;
import cope.cosmos.client.clickgui.cosmos.window.Window;
import cope.cosmos.client.clickgui.cosmos.window.WindowManager;
import cope.cosmos.client.clickgui.cosmos.window.windows.CategoryWindow;
import cope.cosmos.client.features.modules.client.ClickGUI;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.Vec2f;
import org.lwjgl.input.Mouse;

import java.io.IOException;

@SuppressWarnings("all")
public class CosmosGUI extends GuiScreen implements Util {

    private final MousePosition mouse = new MousePosition(Vec2f.ZERO, false, false, false, false);

    // global navigation
    PlayerNavigation playerNavigation = new PlayerNavigation();
    ControlNavigation controlNavigation = new ControlNavigation();

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawDefaultBackground();

        mouse.setLeftClick(false);
        mouse.setRightClick(false);
        mouse.setMousePosition(new Vec2f(mouseX, mouseY));

        WindowManager.setFocusedWindow(null);

        for (Window window : WindowManager.getWindows()) {
            if (mouseOver(((CategoryWindow) window).getPosition().x, ((CategoryWindow) window).getPosition().y, ((CategoryWindow) window).getWidth(), ((CategoryWindow) window).getHeight())) {
                WindowManager.setFocusedWindow(window);
                break;
            }
        }

        WindowManager.getWindows().forEach(window -> {
            window.drawWindow();

            if (mouseOver(((CategoryWindow) window).getPosition().x, ((CategoryWindow) window).getPosition().y, ((CategoryWindow) window).getWidth(), ((CategoryWindow) window).getHeight() - 5) && !window.isExpanding())
                window.handleLeftDrag((int) getMouse().mousePosition.x, (int) getMouse().mousePosition.y);
        });

        if (WindowManager.getFocusedWindow() != null && Mouse.hasWheel()) {
            int scroll = Mouse.getDWheel();
            WindowManager.getFocusedWindow().handleScroll(scroll);

            // scrolling?
            controlNavigation.handleScroll(scroll);
            playerNavigation.handleScroll(scroll);
        }

        controlNavigation.drawNavigation();
        playerNavigation.drawNavigation();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        switch (mouseButton) {
            case 0:
                mouse.setLeftClick(true);
                mouse.setLeftHeld(true);

                WindowManager.getWindows().forEach(window -> {
                    if (mouseOver(((CategoryWindow) window).getPosition().x, ((CategoryWindow) window).getPosition().y, ((CategoryWindow) window).getWidth(), ((CategoryWindow) window).getHeight() - 5) && !window.isExpanding())
                        window.handleLeftClick(mouseX, mouseY);
                });

                controlNavigation.handleLeftClick(mouseX, mouseY);
                playerNavigation.handleLeftClick(mouseX, mouseY);
                break;
            case 1:
                mouse.setRightClick(true);
                mouse.setRightHeld(true);

                WindowManager.getWindows().forEach(window -> {
                    if (mouseOver(((CategoryWindow) window).getPosition().x, ((CategoryWindow) window).getPosition().y, ((CategoryWindow) window).getWidth(), ((CategoryWindow) window).getHeight() - 5) && !window.isExpanding())
                        window.handleRightClick(mouseX, mouseY);
                });

                controlNavigation.handleRightClick(mouseX, mouseY);
                playerNavigation.handleRightClick(mouseX, mouseY);
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

        WindowManager.getWindows().forEach(window -> {
            window.handleKeyPress(typedChar, keyCode);
        });

        controlNavigation.handleKeyPress(typedChar, keyCode);
        playerNavigation.handleKeyPress(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        ClickGUI.INSTANCE.disable();
        Cosmos.INSTANCE.getPresetManager().save();

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

    public static class MousePosition {

        private Vec2f mousePosition;
        private boolean leftClick, rightClick, leftHeld, rightHeld;

        public MousePosition(Vec2f mousePosition, boolean leftClick, boolean rightClick, boolean leftHeld, boolean rightHeld) {
            this.mousePosition = mousePosition;
            this.leftClick = leftClick;
            this.rightClick = rightClick;
            this.leftHeld = leftHeld;
            this.rightHeld = rightHeld;
        }

        public boolean isLeftClick() {
            return leftClick;
        }

        public void setLeftClick(boolean in) {
            leftClick = in;
        }

        public boolean isRightClick() {
            return rightClick;
        }

        public void setRightClick(boolean in) {
            rightClick = in;
        }

        public boolean isLeftHeld() {
            return leftHeld;
        }

        public void setLeftHeld(boolean in) {
            leftHeld = in;
        }

        public boolean isRightHeld() {
            return rightHeld;
        }

        public void setRightHeld(boolean in) {
            rightHeld = in;
        }

        public void setMousePosition(Vec2f in) {
            mousePosition = in;
        }

        public Vec2f getMousePosition() {
            return mousePosition;
        }
    }
}