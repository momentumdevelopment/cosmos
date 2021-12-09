package cope.cosmos.client.manager;

import cope.cosmos.util.Wrapper;

public class Manager implements Wrapper {

    private final String name;
    private final String description;

    public Manager(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // runs every update tick (i.e. 20 times a second)
    public void onUpdate() {

    }

    // runs every tick (i.e. 40 times a second)
    public void onTick() {

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
