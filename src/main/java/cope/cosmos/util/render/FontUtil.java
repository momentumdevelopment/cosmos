package cope.cosmos.util.render;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.Cosmos.ClientType;
import cope.cosmos.client.features.modules.client.FontModule;
import cope.cosmos.font.FontRenderer;
import cope.cosmos.util.Wrapper;
import cope.cosmos.util.file.FileSystemUtil;

import java.awt.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author linustouchtips
 * @since 04/18/2021
 */
public class FontUtil implements Wrapper {

	// client font
	private static FontRenderer font;

	/**
	 * Loads a given font
	 * @param in The given font
	 */
	public static void loadFont(String in) {
		font = new FontRenderer(loadFont(in, 40));
	}

	/**
	 * Attempts to load a given font
	 * @param in The given font
	 * @param size The size of the font
	 * @return The loaded font
	 */
	private static Font loadFont(String in, float size) {
		try {

			// font stream
			InputStream fontStream = new FileInputStream(FileSystemUtil.FONTS.resolve(in).toFile());

			// if the client font exists
			if (fontStream != null) {

				// creates and derives the font
				Font clientFont = Font.createFont(0, fontStream);
				clientFont = clientFont.deriveFont(Font.PLAIN, size);

				// close stream
				fontStream.close();
				return clientFont;
			}

			// default
			return new Font("default", Font.PLAIN, (int) size);

		} catch (Exception exception) {

			// print exception
			if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
				exception.printStackTrace();
			}

			// notify
			if (font != null && font.nullCheck()) {

				// unrecognized gamemode exception
				Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Font!", ChatFormatting.RED + "Please enter a valid font.");
			}

			// load default
			return new Font("hindmadurai", Font.PLAIN, (int) size);
		}
	}

	public static void drawStringWithShadow(String text, float x, float y, int color) {
		if (FontModule.INSTANCE.isEnabled()) {
			font.drawStringWithShadow(text, x, y, color);
		}

		else {
			mc.fontRenderer.drawStringWithShadow(text, x, y, color);
		}
	}

	public static void drawCenteredStringWithShadow(String text, float x, float y, int color) {
		if (FontModule.INSTANCE.isEnabled()) {
			font.drawStringWithShadow(text, x - font.getStringWidth(text) / 2f + 0.75f, y - font.getHeight() / 2f + 2f, color);
		}

		else {
			mc.fontRenderer.drawStringWithShadow(text, x - mc.fontRenderer.getStringWidth(text) / 2f, y - ((float) mc.fontRenderer.FONT_HEIGHT) / 2f, color);
		}
	}

	public static int getStringWidth(String text) {
		if (FontModule.INSTANCE.isEnabled()) {
			return font.getStringWidth(text);
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
			return font.drawStringWithShadow(text, x, y, color);
		}

		return mc.fontRenderer.drawStringWithShadow(text, x, y, color);
	}

	/**
	 * Gets the current font
	 * @return The current font
	 */
	public static String getFont() {
		return font.getName();
	}
}