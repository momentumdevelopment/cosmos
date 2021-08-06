package cope.cosmos.client.clickgui.windowed.window;

import cope.cosmos.util.render.RenderUtil;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;

public class ScrollableWindow extends Window {

    private float lowerBound = getHeight() - getBar() + 75;
    private float scroll;

    public ScrollableWindow(String name, Vec2f position) {
        super(name, position);
    }

    @Override
    public void drawWindow() {
        super.drawWindow();

        // our upperbound is the window height
        float upperBound = getHeight() - getBar();

        // make sure the scroll doesn't go farther than our bounds
        scroll = MathHelper.clamp(scroll, 0, lowerBound);

        // scale our scroll bar's vertical position
        float scaledHeight = MathHelper.clamp((upperBound / lowerBound), 0, 1) * (upperBound - 3);
        float scaledY = MathHelper.clamp((scroll / lowerBound), 0, 1) * (upperBound - 7 - scaledHeight);

        glPushAttrib(GL_SCISSOR_BIT); {
            RenderUtil.scissor((int) (getPosition().x + getWidth() - 13), (int) (getPosition().y + getBar() + 2), (int) (getPosition().x + getWidth() - 2), (int) (getPosition().y + getBar() + upperBound - 3));
            glEnable(GL_SCISSOR_TEST);
        }

        // scroll background & outline
        RenderUtil.drawBorderRect(getPosition().x + getWidth() - 12, getPosition().y + getBar() + 3, 9, upperBound - 7, new Color(0, 0, 0, 40), new Color(0, 0, 0, 70));

        // scroll bar
        RenderUtil.drawRect(getPosition().x + getWidth() - 12, getPosition().y + getBar() + 3 + scaledY, 9, scaledHeight, new Color(255, 0, 0, 130));

        glDisable(GL_SCISSOR_TEST);
        glPopAttrib();
    }

    @Override
    public void handleScroll(int scroll) {
        super.handleScroll(scroll);
        this.scroll += scroll * 0.05F;
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
}
