package cope.cosmos.client.features.modules.world;

import cope.cosmos.client.events.entity.player.interact.DualInteractEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 12/30/2021
 */
public class MultiTaskModule extends Module {
    public static MultiTaskModule INSTANCE;

    public MultiTaskModule() {
        super("MultiTask", new String[] {"DualUse"}, Category.WORLD, "Allows you to use your offhand and mine at the same time");
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onDualInteract(DualInteractEvent event) {

        // prevent the client from knowing you are already interacting
        event.setCanceled(true);
    }
}
