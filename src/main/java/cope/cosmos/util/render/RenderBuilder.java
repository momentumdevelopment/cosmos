package cope.cosmos.util.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author linustouchtips
 * @since 05/06/2021
 */
public class RenderBuilder {

    // gl
    private boolean setup, depth, blend, texture, cull, alpha, shade;

    // box
    private AxisAlignedBB axisAlignedBB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
    private Box box = Box.FILL;

    // dimensions
    private double height, length, width;

    // color
    private Color color = Color.WHITE;

    /**
     * Sets up default rendering
     * @return This builder
     */
    public RenderBuilder setup() {
        GlStateManager.pushMatrix();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        setup = true;
        return this;
    }

    /**
     * Disables depth
     * @param in Whether to disable depth
     * @return This builder
     */
    public RenderBuilder depth(boolean in) {
        if (in) {
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
        }

        depth = in;
        return this;
    }

    /**
     * Enables texture blending
     * @return This builder
     */
    public RenderBuilder blend() {
        GlStateManager.enableBlend();
        blend = true;
        return this;
    }

    /**
     * Disables textures
     * @return This builder
     */
    public RenderBuilder texture() {
        GlStateManager.disableTexture2D();
        texture = true;
        return this;
    }

    /**
     * Sets the line width
     * @param width The line width
     * @return This builder
     */
    public RenderBuilder line(float width) {
        GlStateManager.glLineWidth(width);
        return this;
    }

    /**
     * Disables culling (light in occluded environments)
     * @param in Whether to disable culling
     * @return This builder
     */
    public RenderBuilder cull(boolean in) {
        if (cull) {
            GlStateManager.disableCull();
        }

        cull = in;
        return this;
    }

    /**
     * Disables alpha
     * @param in Whether to disable alpha
     * @return This builder
     */
    public RenderBuilder alpha(boolean in) {
        if (alpha) {
            GlStateManager.disableAlpha();
        }

        alpha = in;
        return this;
    }

    /**
     * Shades the model
     * @param in Whether to shade the model
     * @return This builder
     */
    public RenderBuilder shade(boolean in) {
        if (in) {
            GlStateManager.shadeModel(GL_SMOOTH);
        }

        shade = in;
        return this;
    }

    /**
     * Resets all states
     * @return This builder
     */
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

    /**
     * Sets the position of the box
     * @param in The block position of the box
     * @return This builder
     */
    public RenderBuilder position(BlockPos in) {
        position(new AxisAlignedBB(in.getX(), in.getY(), in.getZ(), in.getX() + 1, in.getY() + 1, in.getZ() + 1));
        return this;
    }

    /**
     * Sets the position of the box
     * @param in The vector position of the box
     * @return This builder
     */
    public RenderBuilder position(Vec3d in) {
        position(new AxisAlignedBB(in.x, in.y, in.z, in.x + 1, in.y + 1, in.z + 1));
        return this;
    }

    /**
     * Sets the position of the box
     * @param in The axis box position of the box
     * @return This builder
     */
    public RenderBuilder position(AxisAlignedBB in) {
        axisAlignedBB = in;
        return this;
    }

    /**
     * Sets the height of the box
     * @param in The height of the box
     * @return This builder
     */
    public RenderBuilder height(double in) {
        height = in;
        return this;
    }

    /**
     * Sets the width of the box
     * @param in The width of the box
     * @return This builder
     */
    public RenderBuilder width(double in) {
        width = in;
        return this;
    }

    /**
     * Sets the length of the box
     * @param in The length of the box
     * @return This builder
     */
    public RenderBuilder length(double in) {
        length = in;
        return this;
    }

    /**
     * Sets the color of the box
     * @param in The color of the box
     * @return This builder
     */
    public RenderBuilder color(Color in) {
        color = in;
        return this;
    }

    /**
     * Sets the render mode of the box
     * @param in The render mode of the box
     * @return This builder
     */
    public RenderBuilder box(Box in) {
        box = in;
        return this;
    }

    /**
     * Gets the position of the box
     * @return The position of the box
     */
    public AxisAlignedBB getAxisAlignedBB() {
        return axisAlignedBB;
    }

    /**
     * Gets the height of the box
     * @return The height of the box
     */
    public double getHeight() {
        return height;
    }

    /**
     * Gets the width of the box
     * @return The width of the box
     */
    public double getWidth() {
        return width;
    }

    /**
     * Gets the length of the box
     * @return The length of the box
     */
    public double getLength() {
        return length;
    }

    /**
     * Gets the color of the box
     * @return The color of the box
     */
    public Color getColor() {
        return color;
    }

    /**
     * Gets the render mode of the box
     * @return The render mode of the box
     */
    public Box getBox() {
        return box;
    }

    public enum Box {

        /**
         * Fills in the position
         */
        FILL,

        /**
         * Outlines the position
         */
        OUTLINE,

        /**
         * Fills and outlines the position
         */
        BOTH,

        /**
         * Fades the fill on the position
         */
        GLOW,

        /**
         * Reverse glow
         */
        REVERSE,

        /**
         * Outlines the corner of the position
         */
        CLAW,

        /**
         * No Render
         */
        NONE
    }
}
