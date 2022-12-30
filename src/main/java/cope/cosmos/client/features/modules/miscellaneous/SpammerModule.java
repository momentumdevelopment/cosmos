package cope.cosmos.client.features.modules.miscellaneous;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;

/**
 * @author linustouchtips
 * @since 10/26/2022
 */
public class SpammerModule extends Module {
    public static SpammerModule INSTANCE;

    public SpammerModule() {
        super("Spammer", Category.MISCELLANEOUS, "Spams in chat");
        INSTANCE = this;
    }
}
