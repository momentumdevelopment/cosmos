package cope.cosmos.client.manager.managers;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.render.other.CapeLocationEvent;
import cope.cosmos.client.manager.Manager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Surge
 * @since 26/08/2022
 *
 * Actual cape textures probably could be a bit better, I'm not that good at graphic design.
 */
public class CapeManager extends Manager {

    // Map of player names and their subsequent cape type
    private final Map<String, CapeType> capedPlayers = new HashMap<>();

    public CapeManager() {
        super("CapeManager", "Manages whether players in the current world should have a cape");

        try {
            // We can use a different system for adding / removing capes, I just think a discord bot handling it is pretty neat
            BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("https://CosmosCapeBot.wolfsurge.repl.co/capes").openStream()));

            // Personally, I dislike doing it this way, but it seems the easiest.
            // Could alternatively be done with an array or list to add all lines, then iterating through that
            // But that's just pointless imo
            String line;

            while ((line = reader.readLine()) != null) {

                // Get data -> PlayerName:CapeType
                String[] data = line.split(":");

                // Add to map
                capedPlayers.put(data[0], CapeType.valueOf(data[1].toUpperCase(Locale.getDefault())));
            }

            // Close reader
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Cosmos.EVENT_BUS.register(this);
    }

    /**
     * Gets the map of caped player
     * @return The map of caped player
     */
    public Map<String, CapeType> getCapedPlayers() {
        return capedPlayers;
    }

    public enum CapeType {

        /**
         * Normal cape
         */
        NORMAL("textures/cape/normal.png"),

        /**
         * Contributor cape
         */
        CONTRIBUTOR("textures/cape/contributor.png");

        // The path of the cape texture
        private final String path;

        CapeType(String path) {
            this.path = path;
        }

        /**
         * Gets the path of the cape texture
         * @return The path of the cape texture
         */
        public String getPath() {
            return path;
        }
    }

}
