package cope.cosmos.client.manager.managers;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.util.Wrapper;
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

        if (event.player.equals(mc.player)) {

            // clear on login
            waypoints.replaceAll((name, waypoint) -> {

                // remove non-coordinate waypoints
                if (!waypoint.getFormat().equals(Format.COORDINATE)) {
                    return null;
                }

                return waypoint;
            });
        }
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

        // waypoints for the current server
        Map<String, Waypoint> serverWaypoints = waypoints;

        // find waypoints for current server
        waypoints.replaceAll((name, waypoint) -> {

            // remove non-coordinate waypoints
            if (waypoint.isServerCurrent()) {
                return waypoint;
            }

            return null;
        });

        return waypoints;
    }

    // holder
    public static class Waypoint implements Wrapper {

        // coordinates & type
        private final Vec3d coordinates;
        private final String server;
        private final Format format;

        public Waypoint(Vec3d coordinates, String server, Format format) {
            this.coordinates = coordinates;
            this.server = server;
            this.format = format;
        }

        /**
         * Checks if the waypoint's server is current
         * @return Whether the waypoint's server is current
         */
        public boolean isServerCurrent() {
            if (nullCheck()) {

                // check world name
                if (mc.isSingleplayer()) {

                    // world
                    String world = mc.world.getWorldInfo().getWorldName();

                    // check world name
                    return world.equalsIgnoreCase(server);
                }

                else {

                    // ip
                    String ip = mc.getCurrentServerData() != null ? mc.getCurrentServerData().serverIP : "";

                    // check server name
                    return ip.equalsIgnoreCase(server);
                }
            }

            return false;
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
