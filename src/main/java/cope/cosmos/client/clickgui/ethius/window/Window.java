package cope.cosmos.client.clickgui.ethius.window;

import cope.cosmos.client.clickgui.ethius.EthiusGuiScreen;
import cope.cosmos.client.clickgui.ethius.element.Element;
import cope.cosmos.client.clickgui.ethius.feature.WindowFeature;
import cope.cosmos.util.client.ColorUtil;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class Window extends Element {

    protected float x;
    protected float y;
    private float dragX;
    private float dragY;
    private boolean dragging;
    private float resizeX;
    private float resizeY;
    private boolean resizing;
    protected float width;
    protected float height;
    public String info = "";
    public final String title;
    public final WindowAttributes attributes = new WindowAttributes(true, true);
    private final EthiusGuiScreen mainScr;

    private final List<WindowFeature> windowFeatures = new ArrayList<>();

    public Window(int x, int y, int width, int height, String title, EthiusGuiScreen mainScr, WindowFeature... features) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.title = title;
        this.mainScr = mainScr;
        this.windowFeatures.addAll(Arrays.asList(features));
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        glPushMatrix();
        if (dragging) {
            this.x = mouseX - dragX;
            this.y = mouseY - dragY;
        }
        final float infoHeight = FontUtil.getFontHeight() * 0.66f;
        RenderUtil.drawShadowedOutlineRectRB(x, y, width, height + infoHeight, 0x60000000, 5f);
        RenderUtil.scissor(x, y, x + width, y + height + infoHeight);

        RenderUtil.drawRect(x, y, width, 1.5f, ColorUtil.getPrimaryColor().getRGB());
        RenderUtil.drawRect(x, y + 1.5f, width, FontUtil.getFontHeight() + 3f, 0xff17171d);
        RenderUtil.drawRect(x, y + FontUtil.getFontHeight() + 1, width, height - FontUtil.getFontHeight() - 1, 0xff23232c);
        if (this.attributes.isClosable()) {
            RenderUtil.drawPolygon(this.x + this.width - 6, this.y + 7, 3, 360, new Color(0xffe26768));
        }
        FontUtil.drawStringWithShadow(title, x + 2, y + 4f, ColorUtil.getPrimaryColor().brighter().brighter().getRGB());

        {
            final WindowFeature wf = windowFeatures.get(0);
            wf.width = this.width;
            wf.height = this.height - FontUtil.getFontHeight() - 5f;
        }

        float yOffset = 0;
        for (WindowFeature feature: windowFeatures) {
            feature.x = x;
            feature.y = y + 4 + FontUtil.getFontHeight() + yOffset;
            feature.draw(this, mouseX, mouseY);
            yOffset += feature.height + 2;
        }

        RenderUtil.endScissor();

        RenderUtil.drawRect(x, y + height - 2, width, 2 + infoHeight, 0xff23232c);
        if (info != null) {
            glTranslatef(x + 1, y + height + 1, 0);
            glScalef(0.66f, 0.66f, 1f);
            glTranslatef(-x - 1, -(y + height + 1), 0);
            FontUtil.drawStringWithShadow(info, x + 1, y + height + 1, 0xffffffff);
            glTranslatef(x + 1, y + height + 1, 0);
            glScalef(1f / 0.66f, 1f / 0.66f, 1f);
            glTranslatef(-x - 1, -(y + height + 1), 0);
            info = null;
        }

        if (this.attributes.isResizable()) {
            RenderUtil.drawLine2d(x + width - 4, y + height + infoHeight - 1, x + width - 1, y + height + infoHeight - 4, 1f, ColorUtil.getPrimaryColor());
            RenderUtil.drawLine2d(x + width - 7, y + height + infoHeight - 1, x + width - 1, y + height + infoHeight - 7, 1f, ColorUtil.getPrimaryColor());
            RenderUtil.drawLine2d(x + width - 10, y + height + infoHeight - 1, x + width - 1, y + height + infoHeight - 10, 1f, ColorUtil.getPrimaryColor());
            if (resizing) {
                this.width = mouseX - this.x + resizeX;
                this.height = mouseY - this.y + resizeY - infoHeight;
            }
        }
        glPopMatrix();
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseX >= x + width - 10 && mouseX <= x + width - 1 && mouseY >= y + height + FontUtil.getFontHeight() * 0.75f - 10 && mouseY <= y + height + FontUtil.getFontHeight() * 0.75f - 1) {
            if (this.attributes.isResizable()) {
                this.resizeX = mouseX - (x + width - 10);
                this.resizeY = mouseY - (y + height + FontUtil.getFontHeight() * 0.75f - 10);
                this.resizing = true;
            }
            return true;
        }
        if (mouseX >= x + width - 10 && mouseX <= x + width - 4 && mouseY >= y + 4 && mouseY <= y + 10) {
            if (this.attributes.isClosable()) {
                mainScr.getListElements().remove(this);
            }
        }
        for (WindowFeature feature: windowFeatures) {
            if (feature.mouseClicked(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            if (mouseButton == 0) {
                setupDrag(mouseX, mouseY);
                return true;
            }
        }
        return false;
    }

    @Override
    public void setupDrag(float mouseX, float mouseY) {
        dragging = true;
        dragX = mouseX - x;
        dragY = mouseY - y;
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            dragging = false;
            resizing = false;
        }
        for (WindowFeature feature: windowFeatures) {
            if (feature.mouseReleased(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        for (WindowFeature feature: windowFeatures) {
            feature.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public void mouseScrolled(float mouseX, float mouseY, int amount) {
        for (WindowFeature feature: windowFeatures) {
            feature.mouseScrolled(mouseX, mouseY, amount);
        }
    }

}
