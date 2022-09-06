package cope.cosmos.client.events.entity.player;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called when the player turns
 * @author aesthetical
 * @since 08/29/2022
 */
@Cancelable
public class PlayerTurnEvent extends Event {

    // rotations
    private final float yaw, pitch;

    public PlayerTurnEvent(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    /**
     * Gets the turned yaw
     * @return The turned yaw
     */
    public float getYaw() {
        return yaw;
    }

    /**
     * Gets the turned pitch
     * @return The turned pitch
     */
    public float getPitch() {
        return pitch;
    }
}