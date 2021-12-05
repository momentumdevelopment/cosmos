package cope.cosmos.util.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.BlockPos;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class RenderBuilder {

    // gl
    private boolean setup = false;
    private boolean depth = false;
    private boolean blend = false;
    private boolean texture = false;
    private boolean cull = false;
    private boolean alpha = false;
    private boolean shade = false;

    // box
    private BlockPos blockPos = BlockPos.ORIGIN;
    private Box box = Box.FILL;

    private double height = 0;
    private double length = 0;
    private double width = 0;

    private Color color = Color.WHITE;

    public RenderBuilder setup() {
        GlStateManager.pushMatrix();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        setup = true;
        return this;
    }

    public RenderBuilder depth(boolean in) {
        if (in) {
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
        }

        depth = in;
        return this;
    }

    public RenderBuilder blend() {
        GlStateManager.enableBlend();
        blend = true;
        return this;
    }

    public RenderBuilder texture() {
        GlStateManager.disableTexture2D();
        texture = true;
        return this;
    }

    public RenderBuilder line(float width) {
        GlStateManager.glLineWidth(width);
        return this;
    }

    public RenderBuilder cull(boolean in) {
        if (cull) {
            GlStateManager.disableCull();
        }

        cull = in;
        return this;
    }

    public RenderBuilder alpha(boolean in) {
        if (alpha) {
            GlStateManager.disableAlpha();
        }

        alpha = in;
        return this;
    }

    public RenderBuilder shade(boolean in) {
        if (in) {
            GlStateManager.shadeModel(GL_SMOOTH);
        }

        shade = in;
        return this;
    }

    public RenderBuilder build() {
        if (depth) {
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
        }

        if (texture) {
            GlStateManager.enableTexture2D();
        }

        if (blend) {
            GlStateManager.disableBlend();
        }

        if (cull) {
            GlStateManager.enableCull();
        }

        if (alpha) {
            GlStateManager.enableAlpha();
        }

        if (shade) {
            GlStateManager.shadeModel(GL_FLAT);
        }

        if (setup) {
            glDisable(GL_LINE_SMOOTH);
            GlStateManager.popMatrix();
        }

        return this;
    }

    public RenderBuilder position(BlockPos in) {
        blockPos = in;
        return this;
    }

    public RenderBuilder height(double in) {
        height = in;
        return this;
    }

    public RenderBuilder width(double in) {
        width = in;
        return this;
    }

    public RenderBuilder length(double in) {
        length = in;
        return this;
    }

    public RenderBuilder color(Color in) {
        color = in;
        return this;
    }

    public RenderBuilder box(Box in) {
        box = in;
        return this;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }

    public double getLength() {
        return length;
    }

    public Color getColor() {
        return color;
    }

    public Box getBox() {
        return box;
    }

    public enum Box {
        FILL, OUTLINE, BOTH, GLOW, REVERSE, CLAW, NONE
    }
}
