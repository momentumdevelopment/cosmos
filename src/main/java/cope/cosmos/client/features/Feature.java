package cope.cosmos.client.features;

/**
 * Generic feature, used to identify a feature in the client
 * @author linustouchtips
 * @since 06/08/2021
 */
public class Feature {

    // all features should have a name, and optionally a description
    protected String name;
    protected String[] aliases;
    protected String description;

    public Feature(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Feature(String name) {
        this(name, "");
    }

    /**
     * Checks if a given name matches this feature
     * @param in The given name
     * @return Whether the given name matches this feature
     */
    public boolean equals(String in) {

        // main name matches
        if (name.equalsIgnoreCase(in)) {
            return true;
        }

        // alias matches
        else {
            if (aliases != null) {
                for (String alias : aliases) {
                    if (alias.equalsIgnoreCase(in)) {
                        return true;
                    }
                }
            }
        }

        // none match
        return false;
    }

    /**
     * Checks if a given text starts with this feature's name/aliases
     * @param in The given text
     * @return Whether them given text starts with this feature's name/aliases
     */
    public int startsWith(String in) {

        // main name matches
        if (name.toLowerCase().startsWith(in.toLowerCase())) {
            return 1000;
        }

        // alias matches
        else {
            if (aliases != null) {
                for (int i = 0; i < aliases.length; i++) {
                    if (aliases[i].toLowerCase().startsWith(in.toLowerCase())) {
                        return i;
                    }
                }
            }
        }

        // none match
        return -1;
    }

    /**
     * Gets the name of the feature
     * @return The name of the feature
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the aliases
     * @param in The aliases
     */
    public void setAliases(String... in) {
        aliases = in;
    }

    /**
     * Gets the aliases
     * @return The aliases
     */
    public String[] getAliases() {
        return aliases;
    }

    /**
     * Gets the description of the feature
     * @return The description of the feature
     */
    public String getDescription() {
        return description;
    }
}
