package cope.cosmos.util.world;

import cope.cosmos.util.Wrapper;

/**
 * @author linustouchtips
 * @since 08/31/2022
 */
public class WorldUtil implements Wrapper {

    /**
     * Gets the current world's name
     * @return The current world's name
     */
    public static String getWorldName() {

        // check world name
        if (!mc.isSingleplayer() && mc.getCurrentServerData() != null) {

            // world name
            return mc.getCurrentServerData().serverIP;
        }

        else {

            // server name
            return mc.world.getWorldInfo().getWorldName();
        }
    }
}
