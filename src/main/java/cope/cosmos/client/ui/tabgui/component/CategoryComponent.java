package cope.cosmos.client.ui.tabgui.component;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.ui.util.animation.Animation;
import cope.cosmos.client.ui.util.ScissorStack;
import cope.cosmos.util.Wrapper;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.string.ColorUtil;
import cope.cosmos.util.string.StringFormatter;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Surge
 * @since 29/03/2022
 */
public class CategoryComponent implements Wrapper {

    // The category of the component
    private final Category category;

    // X position of the component
    private final float x;

    // Y position of the component
    private final float y;

    // Whether this component is the current selected component
    private boolean selected;

    // The selected animation
    private final Animation selectedAnimation = new Animation(300, false);

    // The animation that plays when we expand the category
    private final Animation expandAnimation = new Animation(300, false);

    // The list of module components in this category
    private final List<ModuleComponent> moduleComponents = new ArrayList<>();

    // For scissoring
    private final ScissorStack scissorStack = new ScissorStack();

    // The current selected module component
    private ModuleComponent currentSelected;
    private int currentSelectedIndex = 0;

    public CategoryComponent(float x, float y, Category category) {
        this.x = x;
        this.y = y;
        this.category = category;

        // Max width of module components
        float maxWidth = 4;
        for (Module module : getCosmos().getModuleManager().getModules(module -> module.getCategory().equals(category))) {

            // If module name is longer than max width, set max width to that. We are using Minecraft's font renderer as it is bigger than
            // the client's font.
            if (mc.fontRenderer.getStringWidth(module.getName()) + 16 > maxWidth) {
                maxWidth = mc.fontRenderer.getStringWidth(module.getName()) + 16;
            }
        }

        // Add module components
        float yOffset = 0;
        for (Module module : getCosmos().getModuleManager().getModules(module -> module.getCategory().equals(category))) {
            // Add module component
            moduleComponents.add(new ModuleComponent(x + 73, y + yOffset, maxWidth, module));

            // Increase y offset
            yOffset += 15;
        }

        // Set current selected module component to the first in the list
        currentSelected = moduleComponents.get(0);
        currentSelected.setSelected(true);
    }

    public void render() {
        // Background
        RenderUtil.drawRect(x, y, 70, 15, new Color(23, 23, 29, 255).getRGB());

        // Selected background
        RenderUtil.drawRect(x, y, (float) (70 * selectedAnimation.getAnimationFactor()), 15, new Color(30, 30, 35, 255).getRGB());

        // Render name
        FontUtil.drawStringWithShadow(StringFormatter.formatEnum(category), x + 6, y + 4, (selected ? ColorUtil.getPrimaryColor().getRGB() : -1));

        // If expanded, render module components
        if (expandAnimation.getAnimationFactor() > 0) {
            // Scissor (for animation)
            scissorStack.pushScissor((int) (x + 73), (int) y, (int) (100 * expandAnimation.getAnimationFactor()), (int) ((moduleComponents.size() * 15 + 1) * expandAnimation.getAnimationFactor()));

            // Render module components
            moduleComponents.forEach(moduleComponent -> {
                moduleComponent.render();
            });

            // Stop scissoring
            scissorStack.popScissor();
        }
    }

    /**
     * Called when we input a key
     * @param event The key input event
     */
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        // Pressing down arrow
        if (Keyboard.getEventKey() == Keyboard.KEY_DOWN) {
            currentSelected.setSelected(false);

            // Increase current selected index
            currentSelectedIndex++;

            // If current selected index is greater than the size of the list, set it to 0
            if (currentSelectedIndex >= moduleComponents.size()) {
                currentSelectedIndex = 0;
            }

            // Set current selected module component
            currentSelected = moduleComponents.get(currentSelectedIndex);
            currentSelected.setSelected(true);
        }

        // Pressing up arrow
        else if (Keyboard.getEventKey() == Keyboard.KEY_UP) {
            currentSelected.setSelected(false);

            // Decrease current selected index
            currentSelectedIndex--;

            // If current selected index is less than 0, set it to the last module component
            if (currentSelectedIndex < 0) {
                currentSelectedIndex = moduleComponents.size() - 1;
            }

            // Set current selected
            currentSelected = moduleComponents.get(currentSelectedIndex);
            currentSelected.setSelected(true);
        }

        // Pressing right arrow
        else if (Keyboard.getEventKey() == Keyboard.KEY_RIGHT) {
            currentSelected.onRightArrow();
        }

        // Pressing left arrow
        else if (Keyboard.getEventKey() == Keyboard.KEY_LEFT) {
            setExpanded(false);
        }
    }

    /**
     * Sets the selected state of the category
     * @param selected The new selected state
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
        selectedAnimation.setState(selected);
    }

    /**
     * Sets the expanded state of the category
     * @param expanded Whether the category is expanded or not
     */
    public void setExpanded(boolean expanded) {
        expandAnimation.setState(expanded);
    }

    /**
     * Gets the expanding animation
     * @return The expanding animation
     */
    public Animation getExpandAnimation() {
        return expandAnimation;
    }
}
