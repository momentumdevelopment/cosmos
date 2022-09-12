package cope.cosmos.client.manager.managers;

import cope.cosmos.client.manager.Manager;
import cope.cosmos.client.ui.altgui.AltEntry;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Surge
 * @since 02/06/2022
 */
public class AltManager extends Manager {

    // List of alt entries
    private final Set<AltEntry> altEntries = new HashSet<>();

    public AltManager() {
        super("AltManager", "Manages client's saved alternate accounts");
    }

    /**
     * Gets the index of alt entries
     * @param in The in
     * @return Returns alt entry
     */
    public AltEntry indexOf(int in) {
        return (AltEntry) altEntries.toArray()[in];
    }

    /**
     * Gets the alt entries
     * @return The alt entries
     */
    public Set<AltEntry> getAltEntries() {
        return altEntries;
    }
}
