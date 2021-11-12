package cope.cosmos.client.clickgui.windowed.taskbar;

import cope.cosmos.client.clickgui.util.GUIUtil;
import cope.cosmos.client.clickgui.windowed.window.Window;
import cope.cosmos.util.Wrapper;
import cope.cosmos.util.client.ColorUtil;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec2f;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.lwjgl.opengl.GL11.*;

public class Taskbar implements Wrapper, GUIUtil {

    private final List<Icon> icons = new CopyOnWriteArrayList<>();

    private float offset;

    public void drawTaskbar() {
        // background
        RenderUtil.drawRect(0, new ScaledResolution(mc).getScaledHeight() - 34, new ScaledResolution(mc).getScaledWidth(), 34, new Color(0, 0, 0, 90));
        RenderUtil.drawRect(107, new ScaledResolution(mc).getScaledHeight() - 32, 2, 30, ColorUtil.getPrimaryAlphaColor(120));

        // logo
        glPushMatrix();
        glColor4d(1, 1, 1, 1);
        mc.getTextureManager().bindTexture(new ResourceLocation("cosmos", "textures/imgs/logotransparent.png"));
        GuiScreen.drawModalRectWithCustomSizedTexture(2, new ScaledResolution(mc).getScaledHeight() - 31, 0, 0, 104, 28, 104, 28);
        glPopMatrix();

        // add all pinned windows
        icons.clear();
        getManager().getPinnedWindows().forEach(window -> {
            icons.add(new Icon(window));
        });

        offset = 0;
        icons.forEach(icon -> {
            icon.drawIcon(new Vec2f(120 + offset, new ScaledResolution(mc).getScaledHeight() - 30));
            offset += 38;
        });
    }

    public void handleLeftClick() {
        icons.forEach(icon -> {
            icon.handleLeftClick();
        });
    }

    public void handleRightClick() {
        icons.forEach(icon -> {
            icon.handleRightClick();
        });
    }

    public static class Icon implements GUIUtil {

        private final Window window;

        private int hoverAnimation;

        private Vec2f position;
        private boolean open = false;

        public Icon(Window window) {
            this.window = window;
        }

        public void drawIcon(Vec2f position) {
            setPosition(position);

            if (getManager().getWindows().contains(window)) {
                RenderUtil.drawRect(position.x + 2, position.y + 27, 26, 2, ColorUtil.getPrimaryAlphaColor(120));
            }

            // hover animation
            if (mouseOver(position.x, position.y, 28, 28)) {
                if (hoverAnimation < 25) {
                    hoverAnimation += 5;
                }

                if (!open) {
                    String hoverText = window.getName();

                    // hover text
                    RenderUtil.drawBorderRect(position.x - (FontUtil.getStringWidth(hoverText) / 2F) + 12, position.y - 19, FontUtil.getStringWidth(hoverText) + 4, FontUtil.getFontHeight() + 3, new Color(0, 0, 0, 90), new Color(0, 0, 0, 120));
                    FontUtil.drawStringWithShadow(hoverText, position.x - (FontUtil.getStringWidth(hoverText) / 2F) + 14, position.y - 17, -1);
                }
            }

            else if (!mouseOver(position.x, position.y, 28, 28) && hoverAnimation > 0)
                hoverAnimation -= 5;

            if (open) {
                RenderUtil.drawRect(getPosition().x + 14, getPosition().y - (FontUtil.getFontHeight() * 2) - 44, FontUtil.getStringWidth("Close") + 4, (FontUtil.getFontHeight() * 2) + 4, new Color(0, 0, 0, 90));
            }

            // draws the icon
            glPushMatrix();
            glColor4d(1, 1, 1, 1);
            mc.getTextureManager().bindTexture(getIcon());
            GuiScreen.drawModalRectWithCustomSizedTexture((int) position.x, (int) position.y, 0, 0, 28, 28, 28, 28);
            glPopMatrix();
        }

        public void handleLeftClick() {
            if (mouseOver(getPosition().x, getPosition().y, 28, 28)) {
                if (!getManager().getWindows().contains(window)) {
                    getManager().createWindow(window);
                }
            }
        }

        public void handleRightClick() {
            if (mouseOver(getPosition().x, getPosition().y, 28, 28)) {
                open = !open;
            }
        }

        public void setPosition(Vec2f in) {
            position = in;
        }

        public Vec2f getPosition() {
            return position;
        }

        public ResourceLocation getIcon() {
            return window.getIcon();
        }
    }
}