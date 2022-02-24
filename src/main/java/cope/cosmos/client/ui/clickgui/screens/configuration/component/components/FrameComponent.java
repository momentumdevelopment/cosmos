package cope.cosmos.client.ui.clickgui.screens.configuration.component.components;

import cope.cosmos.client.ui.clickgui.screens.configuration.component.ClickType;
import cope.cosmos.client.ui.clickgui.screens.DrawableComponent;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.string.ColorUtil;
import net.minecraft.util.math.Vec2f;

import java.awt.*;

/**
 * @author linustouchtips
 * @since 01/29/2022
 */
public class FrameComponent<T> extends DrawableComponent {

    // immutable frame traits
    public static final int WIDTH = 100;
    public static final int TITLE = 20;

    private final T value;

    // position
    private Vec2f position;
    private Vec2f previousPosition;

    // interaction states
    private boolean drag;

    // hover animation
    private int hoverAnimation;

    public FrameComponent(T value, Vec2f position) {
        this.value = value;
        this.position = position;
    }

    @Override
    public void drawComponent() {
        long interactingWindows = getGUI().getCategoryFrameComponents()
                .stream()
                .filter(categoryFrameComponent -> !categoryFrameComponent.equals(this))
                .filter(categoryFrameComponent -> categoryFrameComponent.isExpanding() || categoryFrameComponent.isDragging())
                .count();

        // dragging
        if (interactingWindows <= 0) {
            if (isMouseOver(position.x, position.y, WIDTH, TITLE) && getMouse().isLeftHeld()) {
                setDragging(true);
            }

            // update position to drag
            if (isDragging()) {
                setPosition(new Vec2f(position.x + (getMouse().getPosition().x - previousPosition.x), position.y + (getMouse().getPosition().y - previousPosition.y)));
            }
        }

        // hover alpha animation
        if (isMouseOver(position.x, position.y, WIDTH, TITLE) && hoverAnimation < 25) {
            hoverAnimation += 5;
        }

        else if (!isMouseOver(position.x, position.y, WIDTH, TITLE) && hoverAnimation > 0) {
            hoverAnimation -= 5;
        }

        // title
        RenderUtil.drawRect(position.x, position.y, WIDTH, TITLE, new Color(23 + hoverAnimation, 23 + hoverAnimation, 29 + hoverAnimation, 255));
        RenderUtil.drawRect(position.x, position.y + TITLE, WIDTH, 2, ColorUtil.getPrimaryColor());
    }

    @Override
    public void onClick(ClickType in) {

    }

    @Override
    public void onType(int in) {

    }

    @Override
    public void onScroll(int in) {

    }

    public int getWidth() {
        return WIDTH;
    }

    public int getTitle() {
        return TITLE;
    }

    /**
     * Gets the frame value
     * @return The frame value
     */
    public T getValue() {
        return value;
    }

    /**
     * Gets the position of the frame
     * @return The position of the frame
     */
    public Vec2f getPosition() {
        return position;
    }

    /**
     * Sets the position of the frame
     * @param in The new position of the frame
     */
    public void setPosition(Vec2f in) {
        position = in;
    }

    /**
     * Gets the previous position of the frame
     * @return The previous position of the frame
     */
    public Vec2f getPreviousPosition() {
        return previousPosition;
    }

    /**
     * Updates the previous position of the frame
     */
    public void updatePreviousPosition() {
        previousPosition = new Vec2f(getMouse().getPosition().x, getMouse().getPosition().y);
    }

    /**
     * Gets the dragging state of the frame
     * @return The dragging state of the frame
     */
    public boolean isDragging() {
        return drag;
    }

    /**
     * Sets the dragging state of the frame
     * @param in The new dragging state of the frame
     */
    public void setDragging(boolean in) {
        drag = in;
    }
}
