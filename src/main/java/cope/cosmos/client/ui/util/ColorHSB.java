package cope.cosmos.client.ui.util;

import java.awt.*;

/**
 * Holder class for HSB color values
 * @author linustouchtips
 * @since 06/05/2021
 */
public class ColorHSB {

    // hsb values
    private double hue, saturation, brightness, transparency;

    public ColorHSB(double hue, double saturation, double brightness, double transparency) {
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
        this.transparency = transparency;
    }

    public ColorHSB(Color color) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);

        // this() constructor has to be first call
        hue = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];
        transparency = color.getAlpha() / 255F;
    }

    /**
     * Gets the hue
     * @return The hue
     */
    public double getHue() {
        return hue;
    }

    /**
     * Sets the hue
     * @param in The new hue
     */
    public void setHue(double in) {
        hue = in;
    }

    /**
     * Gets the saturation
     * @return The saturation
     */
    public double getSaturation() {
        return saturation;
    }

    /**
     * Sets the saturation
     * @param in The new saturation
     */
    public void setSaturation(double in) {
        saturation = in;
    }

    /**
     * Gets the brightness
     * @return The brightness
     */
    public double getBrightness() {
        return brightness;
    }

    /**
     * Sets the brightness
     * @param in The new brightness
     */
    public void setBrightness(double in) {
        brightness = in;
    }

    /**
     * Gets the transparency
     * @return The transparency
     */
    public double getTransparency() {
        return transparency;
    }

    /**
     * Sets the transparency
     * @param in The new transparency
     */
    public void setTransparency(double in) {
        transparency = in;
    }
}
