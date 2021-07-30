package cope.cosmos.util.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.BlockPos;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class RenderBuilder {

    private boolean setup = false;
    private boolean depth = false;
    private boolean blend = false;
    private boolean texture = false;
    private boolean cull = false;
    private boolean alpha = false;
    private boolean shade = false;

    private BlockPos blockPos = BlockPos.ORIGIN;
    private double height = 0;
    private double length = 0;
    private double width = 0;
    private Color color = new Color(255, 255, 255, 255);
    private Box box = Box.FILL;

    public RenderBuilder setup() {
        GlStateManager.pushMatrix();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ZERO, GL_ONE);
        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        setup = true;
        return this;
    }

    public RenderBuilder depth(boolean depth) {
        if (depth) {
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
        }

        this.depth = depth;
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
        glLineWidth(width);
        return this;
    }

    public RenderBuilder cull(boolean cull) {
        if (cull)
            GlStateManager.disableCull();

        this.cull = cull;
        return this;
    }

    public RenderBuilder alpha(boolean alpha) {
        if (alpha)
            GlStateManager.disableAlpha();

        this.alpha = alpha;
        return this;
    }

    public RenderBuilder shade(boolean shade) {
        if (shade)
            GlStateManager.shadeModel(GL_SMOOTH);

        this.shade = shade;
        return this;
    }

    public RenderBuilder build() {
        if (depth) {
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
        }

        if (texture)
            GlStateManager.enableTexture2D();

        if (blend)
            GlStateManager.disableBlend();

        if (setup) {
            glDisable(GL_LINE_SMOOTH);
            GlStateManager.popMatrix();
        }

        if (cull)
            GlStateManager.enableCull();

        if (alpha)
            GlStateManager.enableAlpha();

        if (shade)
            GlStateManager.shadeModel(GL_FLAT);

        return this;
    }

    public RenderBuilder position(BlockPos blockPos) {
        this.blockPos = blockPos;
        return this;
    }

    public RenderBuilder height(double height) {
        this.height = height;
        return this;
    }

    public RenderBuilder width(double width) {
        this.width = width;
        return this;
    }

    public RenderBuilder length(double length) {
        this.length = length;
        return this;
    }

    public RenderBuilder color(Color color) {
        this.color = color;
        return this;
    }

    public RenderBuilder box(Box box) {
        this.box = box;
        return this;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public double getHeight() {
        return this.height;
    }

    public double getWidth() {
        return this.width;
    }

    public double getLength() {
        return this.length;
    }

    public Color getColor() {
        return this.color;
    }

    public Box getBox() {
        return this.box;
    }

    public enum Box {
        FILL, OUTLINE, BOTH, GLOW, REVERSE, CLAW, NONE
    }
}
