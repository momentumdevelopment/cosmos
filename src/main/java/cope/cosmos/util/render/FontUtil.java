package cope.cosmos.util.render;

import cope.cosmos.client.features.modules.client.Font;
import cope.cosmos.client.font.FontRenderer;
import cope.cosmos.util.Wrapper;

import java.io.InputStream;

public class FontUtil implements Wrapper {

	private static FontRenderer globalFont;

	public static void load() {
		globalFont = new FontRenderer(getFont("hindmadurai", 40));
	}

	public static void drawStringWithShadow(String text, float x, float y, int color) {
		if (Font.INSTANCE.isEnabled()) {
			globalFont.drawStringWithShadow(text, x, y, color);
		} else {
			mc.fontRenderer.drawStringWithShadow(text, x, y, color);
		}
	}

	public static int getStringWidth(String text) {
		if (Font.INSTANCE.isEnabled()) {
			return globalFont.getStringWidth(text);
		}

		return mc.fontRenderer.getStringWidth(text);
	}

	public static int getFontHeight() {
		if (Font.INSTANCE.isEnabled()) {
			return globalFont.FONT_HEIGHT;
		}

		return mc.fontRenderer.FONT_HEIGHT;
	}

	public static int getFontString(String text, float x, float y, int color) {
		if (Font.INSTANCE.isEnabled()) {
			return globalFont.drawStringWithShadow(text, x, y, color);
		}

		return mc.fontRenderer.drawStringWithShadow(text, x, y, color);
	}

	private static java.awt.Font getFont(String fontName, float size) {
		try {
			InputStream inputStream = FontUtil.class.getResourceAsStream("/assets/cosmos/fonts/" + fontName + ".ttf");
			java.awt.Font awtClientFont = java.awt.Font.createFont(0, inputStream);
			awtClientFont = awtClientFont.deriveFont(java.awt.Font.PLAIN, size);
			inputStream.close();

			return awtClientFont;
		} catch (Exception exception) {
			exception.printStackTrace();
			return new java.awt.Font("default", java.awt.Font.PLAIN, (int) size);
		}
	}
}