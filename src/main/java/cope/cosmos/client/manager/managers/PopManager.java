package cope.cosmos.client.manager.managers;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.DeathEvent;
import cope.cosmos.client.events.EntityWorldEvent;
import cope.cosmos.client.events.TotemPopEvent;
import cope.cosmos.client.features.modules.misc.Notifier;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.util.Wrapper;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * @author linustouchtips
 * @since 09/22/2021
 */
public class PopManager extends Manager implements Wrapper {

    // map of totem pops
    private final Map<Entity, Integer> totemPops = new HashMap<>();

    public PopManager() {
        super("PopManager", "Keeps track of all the totem pops");
        Cosmos.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTotemPop(TotemPopEvent event) {
        // add the entity to our totem pop, or if they are already in the map -> update the entity's pop info
        totemPops.put(event.getPopEntity(), totemPops.containsKey(event.getPopEntity()) ? totemPops.get(event.getPopEntity()) + 1 : 1);
    }

    @SubscribeEvent
    public void onEntityRemove(EntityWorldEvent.EntityRemoveEvent event) {
        if (totemPops.containsKey(event.getEntity())) {
            // notify the player if necessary
            if (Notifier.INSTANCE.isEnabled() && Notifier.popNotify.getValue()) {
                getCosmos().getChatManager().sendClientMessage(TextFormatting.DARK_PURPLE + event.getEntity().getName() + TextFormatting.RESET + " died after popping " + totemPops.get(event.getEntity()) + " totems!");
            }

            // remove the totem info associated with the entity
            totemPops.remove(event.getEntity());
        }
    }

    /**
     * Gets the totem pops for a given entity
     * @param entity The entity to get the totem pops for
     * @return The totem pops for the given entity
     */
    public int getTotemPops(Entity entity) {
        return totemPops.getOrDefault(entity, 0);
    }
}
