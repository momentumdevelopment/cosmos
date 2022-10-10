package cope.cosmos.client.features.modules.visual;

import cope.cosmos.client.events.render.gui.TabListSizeEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 12/24/2021
 */
public class ExtraTabModule extends Module {
    public static ExtraTabModule INSTANCE;

    public ExtraTabModule() {
        super("ExtraTab", Category.VISUAL, "Extends the tablist's maximum length");
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onTabList(TabListSizeEvent event) {

        // remove limit on size
        event.setCanceled(true);
    }
}
