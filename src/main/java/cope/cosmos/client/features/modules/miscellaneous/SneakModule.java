package cope.cosmos.client.features.modules.miscellaneous;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;

/**
 * @author linustouchtips
 * @since 10/24/2022
 */
public class SneakModule extends Module {
    public static SneakModule INSTANCE;

    public SneakModule() {
        super("Sneak", new String[] {"SafeWalk"}, Category.MISCELLANEOUS, "Automatically sneaks");
        INSTANCE = this;
    }
}
