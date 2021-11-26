package cope.cosmos.client.features.modules.client;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;

import java.awt.*;

public class Colors extends Module {
    public static Colors INSTANCE;

    public Colors() {
        super("Colors", Category.CLIENT, "The universal color for the client");
        setDrawn(false);
        setExempt(true);
        INSTANCE = this;
    }

    public static Setting<Color> color = new Setting<>("Color", "The primary color for the client", new Color(118, 98, 224, 255));

    // rainbow config
    public static Setting<Rainbow> rainbow = new Setting<>("Rainbow", "Add a rainbow effect to the client color", Rainbow.NONE);
    public static Setting<Double> speed = new Setting<>(() -> !rainbow.getValue().equals(Rainbow.NONE), "Speed", "Speed of the rainbow", 0.1, 50.0, 100.0, 1).setParent(rainbow);
    public static Setting<Double> saturation = new Setting<>(() -> !rainbow.getValue().equals(Rainbow.NONE), "Saturation", "Saturation of the rainbow", 0.01, 0.35, 1.0, 2).setParent(rainbow);
    public static Setting<Double> brightness = new Setting<>(() -> !rainbow.getValue().equals(Rainbow.NONE), "Brightness", "Brightness of the rainbow", 0.01, 1.0, 1.0, 2).setParent(rainbow);
    public static Setting<Double> difference = new Setting<>(() -> !rainbow.getValue().equals(Rainbow.NONE), "Difference", "Difference offset of the rainbow", 0.1, 40.0, 100.0, 1).setParent(rainbow);

    public enum Rainbow {
        /**
         * Dynamically updates rainbow based on offset
         */
        GRADIENT,

        /**
         * Cycles through all hue values
         */
        STATIC,

        /**
         * Cycles through all alpha values
         */
        ALPHA,

        /**
         * No rainbow
         */
        NONE
    }
}
