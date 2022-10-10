package cope.cosmos.client.features.modules.miscellaneous;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;

/**
 * @author linustouchtips
 * @since 06/08/2021
 */
public class TimerModule extends Module {
    public static TimerModule INSTANCE;

    public TimerModule() {
        super("Timer", Category.MISCELLANEOUS, "Allows you to change the client side tick speed");
        INSTANCE = this;
    }

    // **************************** speeds ****************************

    public static Setting<Double> multiplier = new Setting<>("Multiplier", 0.1, 4.0, 50.0, 1)
            .setAlias("Ticks")
            .setDescription("Multiplier for the client side tick speed");

    @Override
    public void onUpdate() {

        // update client ticks
        getCosmos().getTickManager().setClientTicks(multiplier.getValue().floatValue());
    }
}
