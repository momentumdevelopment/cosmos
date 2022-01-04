package cope.cosmos.util.client;

import cope.cosmos.client.features.modules.client.Colors;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ColorUtil {

	public static Color getPrimaryColor(int offset) {
		switch (Colors.rainbow.getValue()) {
			case GRADIENT:
				return rainbow(offset, Colors.color.getValue().getAlpha());
			case STATIC:
				return rainbow(1L, Colors.color.getValue().getAlpha());
			case ALPHA:
				return alphaCycle(Colors.color.getValue(), (offset * 2) + 10);
			case NONE:
			default:
				return Colors.color.getValue();
		}
	}

	public static Color getPrimaryColor() {
		switch (Colors.rainbow.getValue()) {
			case GRADIENT:
			case STATIC:
				return rainbow(1L, Colors.color.getValue().getAlpha());
			case ALPHA:
				return alphaCycle(Colors.color.getValue(), 10);
			case NONE:
			default:
				return Colors.color.getValue();
		}
	}

	public static Color getPrimaryAlphaColor(int alpha) {
		return new Color(getPrimaryColor().getRed(), getPrimaryColor().getGreen(), getPrimaryColor().getBlue(), alpha);
	}

	public static Color alphaCycle(Color color, int count) {
		float[] hsb = new float[3];
		Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
		float brightness = Math.abs(((float) (System.currentTimeMillis() % 2000L) / 1000 + 50F / (float) count * 2) % 2 - 1);
		brightness = 0.5F + 0.5F * brightness;
		hsb[2] = brightness % 2;
		return new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
	}

	public static Color rainbow(long offset, int alpha) {
		float hue = (float) (((double) System.currentTimeMillis() * (Colors.speed.getValue() / 10) + (double) (offset * 500L)) % (30000 / (Colors.difference.getValue() / 100)) / (30000 / (Colors.difference.getValue() / 20F)));
		int rgb = Color.HSBtoRGB(hue, Colors.saturation.getValue().floatValue(), Colors.brightness.getValue().floatValue());
		int red = rgb >> 16 & 255;
		int green = rgb >> 8 & 255;
		int blue = rgb & 255;
		return new Color(red, green, blue, alpha);
	}
}
