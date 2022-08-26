package cope.cosmos.client.ui.tabgui.component;

import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.ui.util.animation.Animation;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.string.ColorUtil;

import java.awt.*;

/**
 * @author Surge
 * @since 29/03/2022
 */
public class ModuleComponent {

    // The module of the component
    private final Module module;

    // The x position of the component
    private final float x;

    // The y position of the component
    private final float y;

    // The width of the component
    private final float width;

    // The selection animation
    private final Animation animation = new Animation(300, false);

    public ModuleComponent(float x, float y, float width, Module module) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.module = module;
    }

    public void render() {
        // Background
        RenderUtil.drawRect(x, y, width, 15, new Color(23, 23, 29, 255).getRGB());

        // Selection animation
        RenderUtil.drawRect(x, y, (float) (width * animation.getAnimationFactor()), 15, new Color(30, 30, 35, 255).getRGB());

        // Draw name
        FontUtil.drawStringWithShadow(module.getName(), x + 6, y + 4, (module.isEnabled() ? ColorUtil.getPrimaryColor().getRGB() : -1));
    }

    /**
     * Handles the action that will occur when we press the right arrow
     * whilst hovering over the component
     */
    public void onRightArrow() {
        // Toggle module's enabled state
        module.toggle();
    }

    /**
     * Sets the component's selection animation state
     * @param selected The new selection animation state
     */
    public void setSelected(boolean selected) {
        animation.setState(selected);
    }
}
