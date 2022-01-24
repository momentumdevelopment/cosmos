package cope.cosmos.client.ui.clickgui.window;

import cope.cosmos.client.features.modules.client.ClickGUI;
import cope.cosmos.util.client.ColorUtil;
import cope.cosmos.util.render.RenderUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class ScrollableWindow extends Window {

    private float scaledY;
    private float previousY;
    private float scaledHeight;

    private float lowerBound;
    private float scroll;

    // whether or not we are manually scrolling
    private boolean manualScroll;

    public ScrollableWindow(String name, Vec2f position, float width, float height, boolean pinned) {
        super(name, position, width, height, pinned);

        // initialize as the same height
        lowerBound = getHeight() - getBar();
    }

    public ScrollableWindow(String name, ResourceLocation icon, Vec2f position, float width, float height, boolean pinned) {
        super(name, icon, position, width, height, pinned);

        // initialize as the same height
        lowerBound = getHeight() - getBar();
    }

    @Override
    public void drawWindow() {
        super.drawWindow();

        // our upperbound is the window height
        float upperBound = getHeight() - getBar();

        // difference between the bounds
        float boundDifference = lowerBound - (upperBound - 23);

        // make sure the scroll doesn't go farther than our bounds
        scroll = MathHelper.clamp(scroll, 0, MathHelper.clamp(boundDifference, 0, Float.MAX_VALUE));

        // scroll, but bounds are ignored
        float unboundScroll = MathHelper.clamp(scroll, 0, boundDifference);

        // scale our scroll bar's vertical position
        scaledHeight = MathHelper.clamp(upperBound / lowerBound, 0, 1) * (upperBound - 3);

        // update the scroll bar's y position
        scaledY = MathHelper.clamp(unboundScroll / boundDifference, 0, 1) * (upperBound - 3 - scaledHeight);

        // check if we are dragging bar
        if (mouseOver(getPosition().x + getWidth() - 12, getPosition().y + getBar() + 3 + scaledY, 9, scaledHeight) && getGUI().getMouse().isLeftHeld()) {
            setManualScroll(true);
            setExpandable(false);
        }

        // manual scroll
        if (getManualScroll() && MathHelper.clamp(upperBound / lowerBound, 0, 1) < 1) {
            scaledY += (getGUI().getMouse().getMousePosition().y - previousY);
            scroll = (scaledY / (upperBound - 3 - scaledHeight)) * boundDifference;
        }

        glPushAttrib(GL_SCISSOR_BIT); {
            RenderUtil.scissor((int) (getPosition().x + getWidth() - 13), (int) (getPosition().y + getBar() + 2), (int) (getPosition().x + getWidth() - 2), (int) (getPosition().y + getBar() + upperBound - 3));
            glEnable(GL_SCISSOR_TEST);
        }

        // scroll background & outline
        RenderUtil.drawBorderRect(getPosition().x + getWidth() - 12, getPosition().y + getBar() + 3, 9, upperBound - 7, new Color(0, 0, 0, 40), new Color(0, 0, 0, 70));

        // scroll bar
        RenderUtil.drawRect(getPosition().x + getWidth() - 12, getPosition().y + getBar() + 3 + scaledY, 9, scaledHeight, ColorUtil.getPrimaryAlphaColor(130));

        glDisable(GL_SCISSOR_TEST);
        glPopAttrib();

        previousY = getGUI().getMouse().getMousePosition().y;
    }

    @Override
    public void handleLeftClick() {
        super.handleLeftClick();

        if (mouseOver(getPosition().x + getWidth() - 12, getPosition().y + getBar() + 3 + scaledY, 9, scaledHeight)) {
            getCosmos().getSoundManager().playSound("click");
        }
    }

    @Override
    public void handleRightClick() {
        super.handleRightClick();
    }

    @Override
    public void handleScroll(int in) {
        super.handleScroll(in);

        scroll += in * (ClickGUI.invertedScrolling.getValue() ? -0.05 : 0.05);
    }

    @Override
    public void handleKeyPress(char typedCharacter, int key) {
        super.handleKeyPress(typedCharacter, key);
    }

    public void setLowerBound(float in) {
        lowerBound = in;
    }

    public float getLowerBound() {
        return lowerBound;
    }

    public void setScroll(float in) {
        scroll = in;
    }

    public float getScroll() {
        return scroll;
    }

    public void setManualScroll(boolean in) {
        manualScroll = in;
    }

    public boolean getManualScroll() {
        return manualScroll;
    }
}
