package cope.cosmos.util.string;

import cope.cosmos.client.features.modules.client.ColorsModule;

import java.awt.*;

public class ColorUtil {

	public static Color getPrimaryColor(int offset) {
		switch (ColorsModule.rainbow.getValue()) {
			case GRADIENT:
				return rainbow(offset, ColorsModule.clientColor.getValue().getAlpha());
			case STATIC:
				return rainbow(1L, ColorsModule.clientColor.getValue().getAlpha());
			case ALPHA:
				return alphaCycle(ColorsModule.clientColor.getValue(), (offset * 2) + 10);
			case NONE:
			default:
				return ColorsModule.clientColor.getValue();
		}
	}

	public static Color getPrimaryColor() {
		switch (ColorsModule.rainbow.getValue()) {
			case GRADIENT:
			case STATIC:
				return rainbow(1L, ColorsModule.clientColor.getValue().getAlpha());
			case ALPHA:
				return alphaCycle(ColorsModule.clientColor.getValue(), 10);
			case NONE:
			default:
				return ColorsModule.clientColor.getValue();
		}
	}

	public static Color getPrimaryAlphaColor(int alpha) {
		return new Color(getPrimaryColor().getRed(), getPrimaryColor().getGreen(), getPrimaryColor().getBlue(), alpha);
	}

	public static Color alphaCycle(Color clientColor, int count) {
		float[] hsb = new float[3];
		Color.RGBtoHSB(clientColor.getRed(), clientColor.getGreen(), clientColor.getBlue(), hsb);
		float brightness = Math.abs(((float) (System.currentTimeMillis() % 2000L) / 1000 + 50F / (float) count * 2) % 2 - 1);
		brightness = 0.5F + 0.5F * brightness;
		hsb[2] = brightness % 2;
		return new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
	}

	public static Color rainbow(long offset, int alpha) {
		float hue = (float) (((double) System.currentTimeMillis() * (ColorsModule.speed.getValue() / 10) + (double) (offset * 500L)) % (30000 / (ColorsModule.difference.getValue() / 100)) / (30000 / (ColorsModule.difference.getValue() / 20F)));
		int rgb = Color.HSBtoRGB(hue, ColorsModule.saturation.getValue().floatValue(), ColorsModule.brightness.getValue().floatValue());
		int red = rgb >> 16 & 255;
		int green = rgb >> 8 & 255;
		int blue = rgb & 255;
		return new Color(red, green, blue, alpha);
	}
}
