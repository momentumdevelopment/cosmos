package cope.cosmos.client.features.modules.client;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;

import java.awt.*;

public class ColorsModule extends Module {
    public static ColorsModule INSTANCE;

    public ColorsModule() {
        super("Colors", Category.CLIENT, "The universal color for the client");
        INSTANCE = this;
        setDrawn(false);
        setExempt(true);

        // enable by default
        enable(true);
    }

    public static Setting<Color> color = new Setting<>("Color", new Color(118, 98, 224, 255)).setDescription("The primary color for the client");

    // rainbow config
    public static Setting<Rainbow> rainbow = new Setting<>("Rainbow", Rainbow.NONE).setDescription("Add a rainbow effect to the client color");
    public static Setting<Double> speed = new Setting<>("Speed", 0.1, 50.0, 100.0, 1).setParent(rainbow).setDescription("Speed of the rainbow").setVisible(() -> !rainbow.getValue().equals(Rainbow.NONE));
    public static Setting<Double> saturation = new Setting<>("Saturation", 0.01, 0.35, 1.0, 2).setParent(rainbow).setDescription("Saturation of the rainbow").setVisible(() -> !rainbow.getValue().equals(Rainbow.NONE));
    public static Setting<Double> brightness = new Setting<>("Brightness", 0.01, 1.0, 1.0, 2).setParent(rainbow).setDescription( "Brightness of the rainbow").setVisible(() -> !rainbow.getValue().equals(Rainbow.NONE));
    public static Setting<Double> difference = new Setting<>("Difference", 0.1, 40.0, 100.0, 1).setParent(rainbow).setDescription("Difference offset of the rainbow").setVisible(() -> !rainbow.getValue().equals(Rainbow.NONE));

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
