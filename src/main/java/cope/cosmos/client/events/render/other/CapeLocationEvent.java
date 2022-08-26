package cope.cosmos.client.events.render.other;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * @author Surge
 * @since 26/08/2022
 */
@Cancelable
public class CapeLocationEvent extends Event {

    // The location of the cape texture file
    private String location;

    // The name of the player
    private final String playerName;

    public CapeLocationEvent(String playerName) {
        this.playerName = playerName;
    }

    /**
     * Gets the location of the cape texture file
     * @return The location of the cape texture file
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the location of the cape texture file
     * @param location The location of the cape texture file
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Gets the player's name
     * @return The player's name
     */
    public String getPlayerName() {
        return playerName;
    }

}