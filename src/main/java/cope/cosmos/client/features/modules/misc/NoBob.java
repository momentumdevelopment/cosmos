package cope.cosmos.client.features.modules.misc;

import cope.cosmos.client.events.render.player.CrosshairBobEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author Wolfsurge
 */
public class NoBob extends Module {

    public NoBob() {
        super("NoBob", Category.MISC, "Lets you use View Bobbing without the crosshair 'bobbing' as well");
    }

    @SubscribeEvent
    public void onCrosshairBob(CrosshairBobEvent event) {
        event.setCanceled(true);
    }

}
