package cope.cosmos.client.features.modules.misc;

import cope.cosmos.client.events.TabListSizeEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 12/24/2021
 */
public class ExtraTab extends Module {
    public static ExtraTab INSTANCE;

    public ExtraTab() {
        super("ExtraTab", Category.MISC, "Extends the tablist's maximum length");
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onTabList(TabListSizeEvent event) {
        // remove limit on size
        event.setCanceled(true);
    }
}
