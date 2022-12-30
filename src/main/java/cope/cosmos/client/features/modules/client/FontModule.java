package cope.cosmos.client.features.modules.client;

import cope.cosmos.client.events.client.SettingUpdateEvent;
import cope.cosmos.client.events.render.gui.RenderFontEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

/**
 * @author linustouchtips
 * @since 07/12/2021
 */
public class FontModule extends Module {
	public static FontModule INSTANCE;
	
	public FontModule() {
		super("Font", new String[] {"CustomFont"}, Category.CLIENT, "Allows you to customize the client font.");
		INSTANCE = this;
		setDrawn(false);
		setExempt(true);
	}

	// **************************** general ****************************

	public static Setting<FontType> style = new Setting<>("Style", FontType.PLAIN)
			.setAlias("FontType")
			.setDescription("Style of the font");

	public static Setting<Boolean> antiAlias = new Setting<>("AntiAlias", true)
			.setDescription("Smooths font");

	public static Setting<Boolean> vanilla = new Setting<>("Vanilla", false)
			.setDescription("Overrides the minecraft vanilla font");

	@SubscribeEvent
	public void onSettingUpdate(SettingUpdateEvent event) {

		// style change
		if (event.getSetting().equals(style)) {

			// current font
			String font = getCosmos().getFontManager().getFont();

			// reload font with new style
			getCosmos().getFontManager().loadFont(font, style.getValue().getType());
		}
	}

	@SubscribeEvent
	public void onFontRender(RenderFontEvent event) {
		if (vanilla.getValue()) {

			// override vanilla font rendering
			event.setCanceled(true);
		}
	}

	/**
	 * Gets the font type identifier
	 * @return The font type identifier
	 */
	public FontType getType(String name) {

		// matching type
		FontType type = null;

		// search all values
		for (FontType fontType : FontType.values()) {

			// name matches
			if (fontType.toString().equalsIgnoreCase(name)) {

				// found match
				type = fontType;
				break;
			}
		}

		return type;
	}

	public enum FontType {

		/**
		 * Plain font
		 */
		PLAIN(Font.PLAIN),

		/**
		 * Bold font
		 */
		BOLD(Font.PLAIN),

		/**
		 * Italicised font
		 */
		ITALICS(Font.PLAIN);

		// font type identifier
		private int type;

		FontType(int type) {
			this.type = type;
		}

		/**
		 * Gets the font type identifier
		 * @return The font type identifier
		 */
		public int getType() {
			return type;
		}
	}
}
