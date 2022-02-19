package cope.cosmos.client.ui.clickgui.component;

import java.util.Arrays;

/**
 * @author linustouchtips
 * @since 01/29/2022
 */
public enum ClickType {

    /**
     * Left mouse click
     */
    LEFT(0),

    /**
     * Right mouse click
     */
    RIGHT(1),

    /**
     * Middle mouse click
     */
    MIDDLE(2);

    // button identity
    private final int identifier;

    ClickType(int identifier) {
        this.identifier = identifier;
    }

    /**
     * Gets the identifier of the click type
     * @return The identifier of the click type
     */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * Gets the click type based on the button identifier
     * @param in The button identifier
     * @return The click type associated with the button identifier
     */
    public static ClickType getByIdentifier(int in) {
        return Arrays.stream(values())
                .filter(value -> value.getIdentifier() == in)
                .findFirst()
                .orElse(null);
    }
}
