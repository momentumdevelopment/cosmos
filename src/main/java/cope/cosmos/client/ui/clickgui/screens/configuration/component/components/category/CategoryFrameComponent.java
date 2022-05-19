package cope.cosmos.client.ui.clickgui.screens.configuration.component.components.category;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.ui.util.animation.Animation;
import cope.cosmos.client.ui.clickgui.screens.configuration.component.ClickType;
import cope.cosmos.client.ui.clickgui.screens.configuration.component.components.FrameComponent;
import cope.cosmos.client.ui.clickgui.screens.configuration.component.components.module.ModuleComponent;
import cope.cosmos.util.Wrapper;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.string.StringFormatter;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.glScaled;

/**
 * @author linustouchtips
 * @since 01/29/2022
 */
public class CategoryFrameComponent extends FrameComponent<Category> implements Wrapper {

    // module features associated with this frame
    private final List<ModuleComponent> moduleComponents = new ArrayList<>();

    // default height of the frame
    private float height = 196;

    // expanding state
    private boolean open = true;
    private boolean expand;

    // offset between features
    private double featureOffset;

    // scroll length
    private float scroll;

    // animation
    private int hoverAnimation;
    private final Animation animation = new Animation(300, true);

    // WHAT THE BLOOP
    private final JFrame frame = new JFrame();

    public CategoryFrameComponent(Category value, Vec2f position) {
        super(value, position);

        // add all associated modules to our feature list
        getCosmos().getModuleManager().getAllModules().forEach(module -> {
            if (module.getCategory().equals(getValue())) {
                moduleComponents.add(new ModuleComponent(this, module));
            }
        });

        // filler features
        for (int i = 0; i < 100; i++) {
            moduleComponents.add(new ModuleComponent(this, null));
        }
    }

    @Override
    public void drawComponent() {
        // expand height
        if (open) {
            long interactingWindows = getGUI().getCategoryFrameComponents()
                    .stream()
                    .filter(categoryFrameComponent -> !categoryFrameComponent.equals(this))
                    .filter(categoryFrameComponent -> categoryFrameComponent.isExpanding() || categoryFrameComponent.isDragging())
                    .count();

            if (interactingWindows <= 0) {
                if (isMouseOver(getPosition().x, getPosition().y + TITLE + height + 2, WIDTH, 4) && getMouse().isLeftHeld()) {
                    setExpanding(true);
                }

                if (isExpanding()) {
                    height = MathHelper.clamp((getMouse().getPosition().y - getPosition().y) - TITLE - 2, 0, 350);
                }
            }
        }

        super.drawComponent();

        // make sure the scroll doesn't go farther than our bounds
        double scaledComponentOffset = getComponentOffset() - 1400;
        scroll = (float) MathHelper.clamp(scroll, -Math.max(0, scaledComponentOffset - height), 0);

        // window title
        glScaled(1.05, 1.05, 1.05); {
            String windowTitle = StringFormatter.formatEnum(getValue());

            // scaled position
            float scaledX = (getPosition().x + (((getPosition().x + WIDTH) - getPosition().x) / 2) - (FontUtil.getStringWidth(windowTitle) / 2F) - 2) * 0.95238095F;
            float scaledY = (getPosition().y + TITLE - 14) * 0.95238095F;

            // draw text
            FontUtil.drawStringWithShadow(windowTitle, scaledX, scaledY, new Color(255, 255, 255).getRGB());
        }

        glScaled(0.95238095, 0.95238095, 0.95238095);

        // height scales with animation factor
        if (animation.getAnimationFactor() > 0) {

            // scissor out the area
            getScissorStack().pushScissor((int) getPosition().x, (int) getPosition().y + TITLE + 2, WIDTH, (int) (height * animation.getAnimationFactor()));

            // module space
            RenderUtil.drawRect(getPosition().x, getPosition().y + TITLE + 2, WIDTH, (float) (height * animation.getAnimationFactor()), new Color(23, 23, 29, 255));

            featureOffset = 0;
            moduleComponents.forEach(moduleComponent -> {
                moduleComponent.drawComponent();
            });

            // pop scissor
            getScissorStack().popScissor();
        }

        // frame takes up whole screen
        ScaledResolution resolution = new ScaledResolution(mc);
        frame.setSize(resolution.getScaledWidth(), resolution.getScaledHeight());

        // hover alpha animation
        if (isMouseOver(getPosition().x, getPosition().y + TITLE + height + 2, WIDTH, 4)) {
            if (hoverAnimation < 25) {
                hoverAnimation += 5;
            }

            // "resize" cursor
            frame.setCursor(new Cursor(Cursor.S_RESIZE_CURSOR));
        }

        else if (!isMouseOver(getPosition().x, getPosition().y + TITLE + height + 2, WIDTH, 4)) {
            if (hoverAnimation > 0) {
                hoverAnimation -= 5;
            }

            // default cursor
            frame.setCursor(Cursor.getDefaultCursor());
        }

        // lower bar
        RenderUtil.drawRect(getPosition().x, (float) (getPosition().y + TITLE + (height * animation.getAnimationFactor()) + 2), WIDTH, 4, new Color(23 + hoverAnimation, 23 + hoverAnimation, 29 + hoverAnimation, 255));

        // update our previous mouse position
        updatePreviousPosition();
    }

    @Override
    public void onClick(ClickType in) {
        super.onClick(in);

        if (!isInteracting()) {

            // toggle open/close state
            if (in.equals(ClickType.RIGHT) && isMouseOver(getPosition().x, getPosition().y, WIDTH, TITLE)) {
                open = !open;
                animation.setState(open);

                // play a sound to make the user happy :)
                getCosmos().getSoundManager().playSound("click");
            }

            if (open) {
                moduleComponents.forEach(moduleComponent -> {
                    moduleComponent.onClick(in);
                });
            }
        }
    }

    @Override
    public void onType(int in) {
        super.onType(in);

        if (open) {
            moduleComponents.forEach(moduleComponent -> {
                moduleComponent.onType(in);
            });
        }
    }


    @Override
    public void onScroll(int in) {
        super.onScroll(in);

        if (open) {

            // update scroll offset
            scroll += in * 0.05;

            moduleComponents.forEach(moduleComponent -> {
                moduleComponent.onScroll(in);
            });
        }
    }

    /**
     * Gets the scroll length of the frame
     * @return The scroll length of the frame
     */
    public float getScroll() {
        return scroll;
    }

    /**
     * Gets the height of frame
     * @return The height of the frame
     */
    public float getHeight() {
        return height;
    }

    /**
     * Sets the height of the frame
     * @param in The new height of the frame
     */
    public void setHeight(float in) {
        height = in;
    }

    /**
     * Checks if the user is interacting with the frame
     * @return Whether the user is interacting with the frame
     */
    public boolean isInteracting() {
        return isExpanding() || isDragging();
    }

    /**
     * Gets the expanding state of the frame
     * @return The expanding state of the frame
     */
    public boolean isExpanding() {
        return expand;
    }

    /**
     * Sets the expanding state of the frame
     * @param in The new expanding state of the frame
     */
    public void setExpanding(boolean in) {
        expand = in;
    }

    /**
     * Gets the feature offset
     * @return The feature offset
     */
    public double getComponentOffset() {
        return featureOffset;
    }

    /**
     * Adds a specified amount to the feature offset
     * @param in Amount to add to the feature offset
     */
    public void addComponentOffset(double in) {
        featureOffset += in;
    }

    /**
     * Sets the category frame open or closed state
     * @param in The category frame open state
     */
    public void setOpen(boolean in) {
        open = in;
        animation.setState(in);
    }
}
