package cope.cosmos.util.render;

import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static net.minecraft.client.renderer.GlStateManager.*;
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
        pushMatrix();
        tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        setup = true;
        return this;
    }

    public RenderBuilder depth(boolean depth) {
        if (depth) {
            disableDepth();
            depthMask(false);
        }

        this.depth = depth;
        return this;
    }

    public RenderBuilder blend() {
        enableBlend();
        blend = true;
        return this;
    }

    public RenderBuilder texture() {
        disableTexture2D();
        texture = true;
        return this;
    }

    public RenderBuilder line(float width) {
        GL11.glLineWidth(width);
        return this;
    }

    public RenderBuilder cull(boolean cull) {
        if (cull)
            disableCull();

        this.cull = cull;
        return this;
    }

    public RenderBuilder alpha(boolean alpha) {
        if (alpha)
            disableAlpha();

        this.alpha = alpha;
        return this;
    }

    public RenderBuilder shade(boolean shade) {
        if (shade)
            shadeModel(GL_SMOOTH);

        this.shade = shade;
        return this;
    }

    public RenderBuilder build() {
        if (depth) {
            depthMask(true);
            enableDepth();
        }

        if (texture)
            enableTexture2D();

        if (blend)
            disableBlend();

        if (cull)
            enableCull();

        if (alpha)
            enableAlpha();

        if (shade)
            shadeModel(GL_FLAT);

        if (setup) {
            glDisable(GL_LINE_SMOOTH);
            popMatrix();
        }

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
