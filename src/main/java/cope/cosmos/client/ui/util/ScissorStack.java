package cope.cosmos.client.ui.util;

import cope.cosmos.util.Wrapper;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.Rectangle;
import java.util.LinkedList;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author GiantNuker
 * @since 01/29/2022
 */
public class ScissorStack implements Wrapper {

    // scissor stack
    private final LinkedList<Rectangle> scissorStack = new LinkedList<>();

    /**
     * Applies scissor test to a specified section of the screen
     * @param x Lower x
     * @param y Lower y
     * @param width Upper x
     * @param height Upper y
     */
    public void pushScissor(int x, int y, int width, int height) {
        Rectangle scissor;

        // resolution
        ScaledResolution resolution = new ScaledResolution(mc);

        // scaled
        int sx = x * resolution.getScaleFactor();
        int sy = (resolution.getScaledHeight() - (y + height)) * resolution.getScaleFactor();
        int sWidth = ((x + width) - x) * resolution.getScaleFactor();
        int sHeight = ((y + height) - y) * resolution.getScaleFactor();

        if (!scissorStack.isEmpty()) {
            glDisable(GL_SCISSOR_TEST);

            Rectangle last = scissorStack.getLast();

            int nx = Math.max(sx, last.x);
            int ny = Math.max(sy, last.y);

            int hDiff = sx - nx;
            int nWidth = Math.min(Math.min(last.width + (last.x - sx), last.width), sWidth + hDiff);

            int diff = sy - ny;
            int nHeight = Math.min(Math.min(last.height + (last.y - sy), last.height), hDiff + diff);

            scissor = new Rectangle(nx, ny, nWidth, nHeight);
        }

        else {
            scissor = new Rectangle(sx, sy, sWidth, sHeight);
        }

        glEnable(GL_SCISSOR_TEST);

        if (scissor.width > 0 && scissor.height > 0) {
            glScissor(scissor.x, scissor.y, scissor.width, scissor.height);
        }

        else {
            glScissor(0, 0, 0, 0);
        }

        scissorStack.add(scissor);
    }

    /**
     * Disables scissor test
     */
    public void popScissor() {
        if (!scissorStack.isEmpty()) {
            glDisable(GL_SCISSOR_TEST);
            scissorStack.removeLast();

            if (!scissorStack.isEmpty()) {
                Rectangle scissor = scissorStack.getLast();
                glEnable(GL_SCISSOR_TEST);
                glScissor(scissor.x, scissor.y, scissor.width, scissor.height);
            }
        }
    }
}
