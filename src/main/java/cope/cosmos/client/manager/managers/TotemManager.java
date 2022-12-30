package cope.cosmos.client.manager.managers;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.combat.TotemPopEvent;
import cope.cosmos.client.events.entity.EntityWorldEvent;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.util.Wrapper;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * @author linustouchtips
 * @since 09/22/2021
 */
public class TotemManager extends Manager implements Wrapper {

    // map of totem pops
    private final Map<Entity, Integer> totemPops = new HashMap<>();

    public TotemManager() {
        super("TotemManager", "Keeps track of all the totem pops");
        Cosmos.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTotemPop(TotemPopEvent event) {

        // add the entity to our totem pop, or if they are already in the map -> update the entity's pop info
        totemPops.put(event.getPopEntity(), totemPops.containsKey(event.getPopEntity()) ? totemPops.get(event.getPopEntity()) + 1 : 1);
    }

    @SubscribeEvent
    public void onRemoveEntity(EntityWorldEvent.EntityRemoveEvent event) {

        // reset on death
        totemPops.remove(event.getEntity());
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {

        // clear on logout
        totemPops.remove(event.player);
    }

    /**
     * Gets the totem pops for a given entity
     * @param entity The entity to get the totem pops for
     * @return The totem pops for the given entity
     */
    public int getTotemPops(Entity entity) {
        return totemPops.getOrDefault(entity, 0);
    }

    /**
     * Removes all pops associated with an entity
     * @param entity The entity to remove
     */
    public void removePops(Entity entity) {
        totemPops.remove(entity);
    }
}
