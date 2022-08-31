package cope.cosmos.client.features.modules.client;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.PersistentModule;
import cope.cosmos.client.features.setting.Setting;

import java.awt.*;

/**
 * @author linustouchtips
 * @since 08/13/2021
 */
public class ColorsModule extends PersistentModule {
    public static ColorsModule INSTANCE;

    public ColorsModule() {
        super("Colors", Category.CLIENT, "The universal color for the client");
        INSTANCE = this;
        setDrawn(false);
        setExempt(true);
    }

    // **************************** color ****************************

    public static Setting<Color> clientColor = new Setting<>("ClientColor", new Color(118, 98, 224, 255))
            .setAlias("Color")
            .setDescription("The primary color for the client");

    // **************************** rainbow ****************************

    public static Setting<Rainbow> rainbow = new Setting<>("Rainbow", Rainbow.NONE)
            .setDescription("Add a rainbow effect to the client color");

    public static Setting<Double> speed = new Setting<>("RainbowSpeed", 0.1, 50.0, 100.0, 1)
            .setDescription("Speed of the rainbow")
            .setVisible(() -> !rainbow.getValue().equals(Rainbow.NONE));

    public static Setting<Double> saturation = new Setting<>("RainbowSaturation", 0.01, 0.35, 1.0, 2)
            .setDescription("Saturation of the rainbow")
            .setVisible(() -> !rainbow.getValue().equals(Rainbow.NONE));

    public static Setting<Double> brightness = new Setting<>("RainbowBrightness", 0.01, 1.0, 1.0, 2)
            .setDescription( "Brightness of the rainbow")
            .setVisible(() -> !rainbow.getValue().equals(Rainbow.NONE));

    public static Setting<Double> difference = new Setting<>("RainbowDifference", 0.1, 40.0, 100.0, 1)
            .setDescription("Difference offset of the rainbow")
            .setVisible(() -> !rainbow.getValue().equals(Rainbow.NONE));

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
