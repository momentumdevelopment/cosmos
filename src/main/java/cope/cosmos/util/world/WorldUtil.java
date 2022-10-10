package cope.cosmos.util.world;

import cope.cosmos.util.Wrapper;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author linustouchtips
 * @since 08/31/2022
 */
public class WorldUtil implements Wrapper {

    /**
     * Checks if a given player is a fakeplayer
     * @param in The given player
     * @return Whether the given player is a fakeplayer
     */
    public static boolean isFakePlayer(EntityPlayer in) {

        // make sure the connection exists
        if (mc.getConnection() != null) {

            // check if player connection exists
            return mc.getConnection().getPlayerInfo(in.getUniqueID()) == null && in.getEntityId() > -100;
        }

        return false;
    }

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
