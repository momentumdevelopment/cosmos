package cope.cosmos.client.features.modules.misc;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;

public class Timer extends Module {
    public Timer() {
        super("Timer", Category.MISC, "Allows you to change the client side tick speed");
    }

    public static Setting<Double> multiplier = new Setting<>("Multiplier", "Multiplier for the client side tick speed", 0.0, 4.0, 50.0, 1);

    @Override
    public void onUpdate() {
        Cosmos.INSTANCE.getTickManager().setClientTicks(multiplier.getValue());
    }
}
