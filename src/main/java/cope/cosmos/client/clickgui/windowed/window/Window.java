package cope.cosmos.client.clickgui.windowed.window;

import cope.cosmos.client.clickgui.util.Util;
import cope.cosmos.util.Wrapper;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

@SuppressWarnings("unused")
public class Window implements Util, Wrapper {

    private final String name;

    private Vec2f position;

    private final float bar = 14;
    private float height = 200;
    private float width = 300;

    private boolean dragging = false;
    private boolean expanding = false;

    private Vec2f previousMousePosition = Vec2f.MIN;

    public Window(String name, Vec2f position) {
        this.name = name;
        this.position = position;
    }

    public void drawWindow() {
        // check if we are dragging our window and update position accordingly
        if (mouseOver(position.x, position.y, width, bar) && getGUI().getMouse().isLeftHeld())
            setDragging(true);

        if (isDragging())
            setPosition(new Vec2f(position.x + (getGUI().getMouse().getMousePosition().x - previousMousePosition.x), position.y + (getGUI().getMouse().getMousePosition().y - previousMousePosition.y)));

        // check if we are expanding our window and update our dimensions accordingly
        if (mouseOver(position.x + width - 10, position.y + height - 10, 10, 10) && getGUI().getMouse().isLeftHeld())
            setExpanding(true);

        if (isExpanding()) {
            // make sure the window isn't expanded past a certain point
            height = MathHelper.clamp(getGUI().getMouse().getMousePosition().y - position.y, 100, new ScaledResolution(mc).getScaledHeight());
            width = MathHelper.clamp(getGUI().getMouse().getMousePosition().x - position.x, 150, new ScaledResolution(mc).getScaledWidth());
        }

        glPushMatrix();

        // window background
        RenderUtil.drawRect(position.x, position.y, width, height, new Color(0, 0, 0, 90));

        // title bar
        RenderUtil.drawRect(position.x, position.y, width, bar, new Color(255, 0, 0, 130));
        FontUtil.drawStringWithShadow(name, position.x + 5, position.y + 3, -1);

        // window outline
        RenderUtil.drawRect(position.x, position.y + bar, 1, height - bar - 1, new Color(255, 0, 0, 130));
        RenderUtil.drawRect(position.x + width - 1, position.y + bar, 1, height - bar - 1, new Color(255, 0, 0, 130));
        RenderUtil.drawRect(position.x, position.y + height - 1, width, 1, new Color(255, 0, 0, 130));

        glPopMatrix();

        previousMousePosition = new Vec2f(getGUI().getMouse().getMousePosition().x, getGUI().getMouse().getMousePosition().y);
    }

    public void handleLeftClick() {

    }

    public void handleRightClick() {

    }

    public void handleScroll(int scroll) {

    }

    public void handleKeyPress(char typedCharacter, int key) {

    }

    public void setPosition(Vec2f in) {
        position = in;
    }

    public Vec2f getPosition() {
        return position;
    }

    public float getBar() {
        return bar;
    }

    public void setHeight(float in) {
        height = in;
    }

    public float getHeight() {
        return height;
    }

    public void setWidth(float in) {
        width = in;
    }

    public float getWidth() {
        return width;
    }

    public void setDragging(boolean in) {
        dragging = in;
    }

    public boolean isDragging() {
        return dragging;
    }

    public void setExpanding(boolean in) {
        expanding = in;
    }

    public boolean isExpanding() {
        return expanding;
    }
}
