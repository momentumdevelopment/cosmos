package cope.cosmos.client.manager.managers;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.manager.Manager;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linustouchtips
 * @since 08/25/2022
 */
public class WaypointManager extends Manager {

    // list of waypoints
    Map<String, Waypoint> waypoints = new ConcurrentHashMap<>();

    public WaypointManager() {
        super("WaypointManager", "Manages the client's waypoints");
        Cosmos.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {

        if (event.player.equals(mc.player))

        // clear on login
        waypoints.clear();
    }

    /**
     * Adds a waypoint
     * @param in The name of the waypoint to add
     * @param waypoint The waypoint to add
     */
    public void addWaypoint(String in, Waypoint waypoint) {
        waypoints.put(in, waypoint);
    }

    /**
     * Removes a waypoint
     * @param in The name of the waypoint to remove
     */
    public void removeWaypoint(String in) {
        waypoints.remove(in);
    }

    /**
     * Gets the list of waypoints
     * @return The list of waypoints
     */
    public Map<String, Waypoint> getWaypoints() {
        return waypoints;
    }

    // holder
    public static class Waypoint {

        // coordinates & type
        private final Vec3d coordinates;
        private final Format format;

        public Waypoint(Vec3d coordinates, Format format) {
            this.coordinates = coordinates;
            this.format = format;
        }

        /**
         * Gets the waypoint's coordinates
         * @return The waypoint's coordinates
         */
        public Vec3d getCoordinates() {
            return coordinates;
        }

        /**
         * Gets the waypoint's format
         * @return The waypoint's format
         */
        public Format getFormat() {
            return format;
        }
    }

    public enum Format {

        /**
         * Waypoint of a player's logout spot
         */
        LOGOUT,

        /**
         * Waypoint of a given coordinates space
         */
        COORDINATE,

        /**
         * Waypoint of the player's death spot
         */
        DEATH
    }
}
