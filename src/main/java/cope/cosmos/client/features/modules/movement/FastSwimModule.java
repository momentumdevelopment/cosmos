package cope.cosmos.client.features.modules.movement;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;

/**
 * @author linustouchtips
 * @since 10/02/2022
 */
public class FastSwimModule extends Module {
    public static FastSwimModule INSTANCE;

    public FastSwimModule() {
        super("FastSwim", Category.MOVEMENT, "Allows you to swim faster");
        INSTANCE = this;
    }


}
