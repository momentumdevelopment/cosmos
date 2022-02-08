package cope.cosmos.client.events.entity;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called when a living entity updates
 * @author linustouchtips
 * @since 12/09/2021
 */
@Cancelable
public class LivingUpdateEvent extends Event {

    private final EntityPlayerSP entityPlayerSP;

    // whether or not the player is sprinting
    private final boolean sprinting;

    public LivingUpdateEvent(EntityPlayerSP entityPlayerSP, boolean sprinting) {
        this.entityPlayerSP = entityPlayerSP;
        this.sprinting = sprinting;
    }

    /**
     * Gets the updating player
     * @return The updating player
     */
    public EntityPlayerSP getEntityPlayerSP() {
        return entityPlayerSP;
    }

    /**
     * Gets whether or not the player is sprinting
     * @return Whether or not the player is sprinting
     */
    public boolean isSprinting() {
        return sprinting;
    }
}
