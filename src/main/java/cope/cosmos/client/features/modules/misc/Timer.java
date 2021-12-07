package cope.cosmos.client.features.modules.misc;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;

/**
 * @author linustouchtips
 * @since 06/08/2021
 */
public class Timer extends Module {
    public static Timer INSTANCE;

    public Timer() {
        super("Timer", Category.MISC, "Allows you to change the client side tick speed");
        INSTANCE = this;
    }

    public static Setting<Double> multiplier = new Setting<>("Multiplier", "Multiplier for the client side tick speed", 0.0, 4.0, 50.0, 1);

    @Override
    public void onUpdate() {
        // update client ticks
        getCosmos().getTickManager().setClientTicks(multiplier.getValue().floatValue());
    }
}
