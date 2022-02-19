package cope.cosmos.util.render;

import cope.cosmos.client.features.modules.client.FontModule;
import cope.cosmos.font.FontRenderer;
import cope.cosmos.util.Wrapper;

import java.awt.Font;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FontUtil implements Wrapper {

	private static FontRenderer globalFont;

	public static void load() {
		globalFont = new FontRenderer(getFont("hindmadurai", 40));
	}

	public static void drawStringWithShadow(String text, float x, float y, int color) {
		if (FontModule.INSTANCE.isEnabled()) {
			globalFont.drawStringWithShadow(text, x, y, color);
		}

		else {
			mc.fontRenderer.drawStringWithShadow(text, x, y, color);
		}
	}

	public static void drawCenteredStringWithShadow(String text, float x, float y, int color) {
		if (FontModule.INSTANCE.isEnabled()) {
			globalFont.drawStringWithShadow(text, x - globalFont.getStringWidth(text) / 2f + 0.75f, y - globalFont.getHeight() / 2f + 2f, color);
		}

		else {
			mc.fontRenderer.drawStringWithShadow(text, x - mc.fontRenderer.getStringWidth(text) / 2f, y - ((float) mc.fontRenderer.FONT_HEIGHT) / 2f, color);
		}
	}

	public static int getStringWidth(String text) {
		if (FontModule.INSTANCE.isEnabled()) {
			return globalFont.getStringWidth(text);
		}

		return mc.fontRenderer.getStringWidth(text);
	}

	public static float getFontHeight() {
		return mc.fontRenderer.FONT_HEIGHT;
	}

	public static List<String> wrapWords(String text, double width) {
		ArrayList<String> finalWords = new ArrayList<>();

		if (getStringWidth(text) > width) {
			String[] words = text.split(" ");
			StringBuilder currentWord = new StringBuilder();
			char lastColorCode = 65535;

			for (String word : words) {
				for (int innerIndex = 0; innerIndex < word.toCharArray().length; innerIndex++) {
					char c = word.toCharArray()[innerIndex];

					if (c == '\u00a7' && innerIndex < word.toCharArray().length - 1) {
						lastColorCode = word.toCharArray()[innerIndex + 1];
					}
				}

				if (getStringWidth(currentWord + word + " ") < width) {
					currentWord.append(word).append(" ");
				} else {
					finalWords.add(currentWord.toString());
					currentWord = new StringBuilder("\u00a7" + lastColorCode + word + " ");
				}
			}

			if (currentWord.length() > 0) {
				if (getStringWidth(currentWord.toString()) < width) {
					finalWords.add("\u00a7" + lastColorCode + currentWord + " ");
					currentWord = new StringBuilder();
				} else {
					finalWords.addAll(formatString(currentWord.toString(), width));
				}
			}
		} else {
			finalWords.add(text);
		}

		return finalWords;
	}

	public static List<String> formatString(String string, double width) {
		ArrayList<String> finalWords = new ArrayList<>();
		StringBuilder currentWord = new StringBuilder();
		char lastColorCode = 65535;
		char[] chars = string.toCharArray();

		for (int index = 0; index < chars.length; index++) {
			char c = chars[index];

			if (c == '\u00a7' && index < chars.length - 1) {
				lastColorCode = chars[index + 1];
			}

			if (getStringWidth(currentWord.toString() + c) < width) {
				currentWord.append(c);
			} else {
				finalWords.add(currentWord.toString());
				currentWord = new StringBuilder("\u00a7" + lastColorCode + c);
			}
		}

		if (currentWord.length() > 0) {
			finalWords.add(currentWord.toString());
		}

		return finalWords;
	}

	public static int getFontString(String text, float x, float y, int color) {
		if (FontModule.INSTANCE.isEnabled()) {
			return globalFont.drawStringWithShadow(text, x, y, color);
		}

		return mc.fontRenderer.drawStringWithShadow(text, x, y, color);
	}

	private static Font getFont(String fontName, float size) {
		try {
			InputStream inputStream = FontUtil.class.getResourceAsStream("/assets/cosmos/fonts/" + fontName + ".ttf");

			if (inputStream != null) {
				Font awtClientFont = Font.createFont(0, inputStream);
				awtClientFont = awtClientFont.deriveFont(Font.PLAIN, size);
				inputStream.close();
				return awtClientFont;
			}

			// default
			return new Font("default", Font.PLAIN, (int) size);

		} catch (Exception exception) {
			exception.printStackTrace();
			return new Font("default", Font.PLAIN, (int) size);
		}
	}
}