package cope.cosmos.client.events.combat;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called when an entity dies in the world
 * @author linustouchtips
 * @since 12/12/2021
 */
public class DeathEvent extends Event {

    // entity that just died
    private final Entity entity;

    public DeathEvent(Entity entity) {
        this.entity = entity;
    }

    /**
     * Gets the entity that just died
     * @return The entity that just died
     */
    public Entity getEntity() {
        return entity;
    }
}
