package cope.cosmos.client.features.modules.movement;

import cope.cosmos.client.events.entity.horse.HorseSaddledEvent;
import cope.cosmos.client.events.entity.horse.HorseSteerEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 02/24/2022
 */
public class EntityControlModule extends Module {
    public static EntityControlModule INSTANCE;

    public EntityControlModule() {
        super("EntityControl", Category.MOVEMENT, "Allows you to steer entities without saddles");
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onHorseSteer(HorseSteerEvent event) {

        // allow any abstract horse to be steered
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onHorseSaddled(HorseSaddledEvent event) {

        // the current riding entity (all entities) are saddled
        event.setCanceled(true);
    }
}
