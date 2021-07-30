package cope.cosmos.client.manager;

public class Manager {

    private final String name;
    private final String description;
    private final int identifier;

    public Manager(String name, String description, int identifier) {
        this.name = name;
        this.description = description;
        this.identifier = identifier;
    }

    public void initialize(Manager manager) {}

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public int getIdentifier() {
        return this.identifier;
    }
}
