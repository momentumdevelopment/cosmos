package cope.cosmos.client.ui.tabgui;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.ui.tabgui.component.CategoryComponent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Surge
 * @since 29/03/2022
 */
public class TabGUI {

    // List of all the categories
    private final List<CategoryComponent> categoryComponents = new ArrayList<>();

    // The current category that is selected
    private CategoryComponent currentSelected;
    private int currentSelectedIndex = 0;

    public TabGUI() {

        // Register to event bus
        Cosmos.EVENT_BUS.register(this);

        float yOffset = 0;
        for (Category category : Category.values()) {

            // Don't add hidden category
            if (category.equals(Category.HIDDEN)) {
                continue;
            }

            // Add category
            categoryComponents.add(new CategoryComponent(4, 16 + yOffset, category));

            // Increase y offset
            yOffset += 15;
        }

        // Set selected to first category
        currentSelected = categoryComponents.get(0);
        currentSelected.setSelected(true);
    }

    public void render() {

        // Render categories
        categoryComponents.forEach(categoryComponent -> {
            categoryComponent.render();
        });
    }

    public void onKeyPress(InputEvent.KeyInputEvent event) {

        // Check we are inputting a key
        if (Keyboard.getEventKeyState()) {

            // Make sure our current category is open
            if (currentSelected.getExpandAnimation().getAnimationFactor() < 1) {

                // Pressing down arrow
                if (Keyboard.getEventKey() == Keyboard.KEY_DOWN) {
                    currentSelected.setSelected(false);

                    // Increase current selected index
                    currentSelectedIndex++;

                    // If we are at the end of the list, go back to the beginning
                    if (currentSelectedIndex >= categoryComponents.size()) {
                        currentSelectedIndex = 0;
                    }

                    // Set current selected to next category
                    currentSelected = categoryComponents.get(currentSelectedIndex);
                    currentSelected.setSelected(true);
                }

                // Pressing up arrow
                else if (Keyboard.getEventKey() == Keyboard.KEY_UP) {
                    currentSelected.setSelected(false);

                    // Decrease current selected index
                    currentSelectedIndex--;

                    // If we are at the beginning of the list, go to the end
                    if (currentSelectedIndex < 0) {
                        currentSelectedIndex = categoryComponents.size() - 1;
                    }

                    // Set current selected to next category
                    currentSelected = categoryComponents.get(currentSelectedIndex);
                    currentSelected.setSelected(true);
                }

                // Pressing right arrow
                else if (Keyboard.getEventKey() == Keyboard.KEY_RIGHT) {

                    // Set current selected to expanded
                    currentSelected.setExpanded(true);
                }
            }

            // Handle category input
            else {
                currentSelected.onKeyInput(event);
            }
        }
    }

}
