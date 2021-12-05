package cope.cosmos.client.managment;

import cope.cosmos.utility.IUtility;

public class Manager implements IUtility {

    private final String name;
    private final String description;

    public Manager(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // runs every ticks (i.e. 20 times a second)
    public void onUpdate() {

    }

    // runs on the separate module thread (i.e. every cpu tick)
    public void onThread() {

    }

    // runs on the game overlay tick (i.e. once every frame)
    public void onRender2D() {

    }

    // runs on the global render tick (i.e. once every frame)
    public void onRender3D() {

    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
