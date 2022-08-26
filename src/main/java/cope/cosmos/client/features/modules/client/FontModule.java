package cope.cosmos.client.features.modules.client;

import cope.cosmos.client.events.render.gui.RenderFontEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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

	public static Setting<Boolean> antiAlias = new Setting<>("AntiAlias", true)
			.setDescription("Smooths font");

	public static Setting<Boolean> vanilla = new Setting<>("Vanilla", false)
			.setDescription("Overrides the minecraft vanilla font");

	@SubscribeEvent
	public void onFontRender(RenderFontEvent event) {
		if (vanilla.getValue()) {

			// override vanilla font rendering
			event.setCanceled(true);
		}
	}
}
