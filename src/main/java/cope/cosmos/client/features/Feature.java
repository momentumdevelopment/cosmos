package cope.cosmos.client.features;

/**
 * Generic feature, used to identify a feature in the client
 * @author linustouchtips
 * @since 06/08/2021
 */
public class Feature {

    // all features should have a name, and optionally a description
    protected String name;
    protected String description;

    public Feature(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Feature(String name) {
        this(name, "");
    }

    /**
     * Gets the name of the feature
     * @return The name of the feature
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the description of the feature
     * @return The description of the feature
     */
    public String getDescription() {
        return description;
    }
}
