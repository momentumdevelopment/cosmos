package cope.cosmos.util.render;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.features.modules.client.FontModule;
import cope.cosmos.util.Wrapper;

/**
 * @author linustouchtips
 * @since 04/18/2021
 */
public class FontUtil implements Wrapper {

	/**
	 * Renders a given text
	 * @param text The given text
	 * @param x The x position
	 * @param y The y position
	 * @param color The color of the text
	 * @return The color of the text
	 */
	public static int drawString(String text, float x, float y, int color) {
		return FontModule.INSTANCE.isEnabled() ? Cosmos.INSTANCE.getFontManager().getFontRenderer().drawString(text, x, y, color, false, FontModule.antiAlias.getValue()) : mc.fontRenderer.drawString(text, (int) x, (int) y, color);
	}

	/**
	 * Renders a given text with a shadow
	 * @param text The given text
	 * @param x The x position
	 * @param y The y position
	 * @param color The color of the text
	 * @return The color of the text
	 */
	public static int drawStringWithShadow(String text, float x, float y, int color) {
		return FontModule.INSTANCE.isEnabled() ? Cosmos.INSTANCE.getFontManager().getFontRenderer().drawStringWithShadow(text, x, y, color, FontModule.antiAlias.getValue()) : mc.fontRenderer.drawStringWithShadow(text, x, y, color);
	}

	/**
	 * Gets a given text's width
	 * @param text The given text
	 * @return The given text's width
	 */
	public static int getStringWidth(String text) {
		return FontModule.INSTANCE.isEnabled() ? Cosmos.INSTANCE.getFontManager().getFontRenderer().getStringWidth(text) : mc.fontRenderer.getStringWidth(text);
	}

	/**
	 * Gets the current font's height
	 * @return The current font's height
	 */
	public static float getFontHeight() {
		return mc.fontRenderer.FONT_HEIGHT;
	}
}