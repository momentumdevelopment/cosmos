package cope.cosmos.client.manager.managers;

import cope.cosmos.client.manager.Manager;
import cope.cosmos.client.ui.altgui.AltEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Surge
 * @since 02/06/2022
 */
public class AltManager extends Manager {

    // List of alt entries
    private final List<AltEntry> altEntries = new ArrayList<>();

    public AltManager() {
        super("AltManager", "Manages client's saved alternate accounts");
    }

    /**
     * Gets the alt entries
     * @return The alt entries
     */
    public List<AltEntry> getAltEntries() {
        return altEntries;
    }
}
