package cope.cosmos.client.manager;

import cope.cosmos.client.features.Feature;
import cope.cosmos.util.Wrapper;

/**
 * @author linustouchtips
 * @since 06/08/2021
 */
public class Manager extends Feature implements Wrapper {

    public Manager(String name, String description) {
        super(name, description);
    }

    /**
     * Runs every update ticks (i.e. 20 times a second)
     */
    public void onUpdate() {

    }

    /**
     * Runs every tick (i.e. 40 times a second)
     */
    public void onTick() {

    }

    /**
     * Runs on the separate module thread (i.e. every cpu tick)
     */
    public void onThread() {

    }

    /**
     * Runs on the game overlay tick (i.e. once every frame)
     */
    public void onRender2D() {

    }

    /**
     * Runs on the global render tick (i.e. once every frame)
     */
    public void onRender3D() {

    }
}
