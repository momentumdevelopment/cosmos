package cope.cosmos.client.features.modules.client;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.client.SettingUpdateEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Bind;
import cope.cosmos.client.features.setting.Setting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import static cope.cosmos.client.features.setting.Bind.Device;

/**
 * @author bon55, linustouchtips
 * @since 05/05/2021
 */
public class ClickGUIModule extends Module {
	public static ClickGUIModule INSTANCE;
	
	public ClickGUIModule() {
		super("ClickGUI", new String[] {"GUI", "UI"}, Category.CLIENT, "This screen.");
		INSTANCE = this;
		getBind().setValue(new Bind(Keyboard.KEY_RSHIFT, Device.KEYBOARD));
		setExempt(true);
	}

	// **************************** general ****************************

	public static Setting<Boolean> pauseGame = new Setting<>("PauseGame", false)
			.setDescription("Pause the game when in GUI");

	public static Setting<Boolean> blur = new Setting<>("Blur", false)
			.setAlias("KawaseBlur", "BlurShader")
			.setDescription("Blur shader for GUI background");

	@Override
	public void onTick() {

		// custom toggling
		if (isEnabled() && mc.currentScreen == null) {

			// open gui
			mc.displayGuiScreen(getCosmos().getClickGUI());
			mc.currentScreen = getCosmos().getClickGUI();
			Cosmos.EVENT_BUS.register(getCosmos().getClickGUI());

			// open frames
			getCosmos().getClickGUI().getCategoryFrameComponents().forEach(categoryFrameComponent -> {
				categoryFrameComponent.setOpen(true);
			});

			// blur shader for background
			if (blur.getValue()) {
				mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/blur.json"));
			}
		}
	}

	@SubscribeEvent
	public void onSettingEnable(SettingUpdateEvent event) {
		if (event.getSetting().equals(blur)) {

			// blur shader for background
			if (blur.getValue()) {
				mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/blur.json"));
			}

			else if (mc.entityRenderer.isShaderActive()) {
				mc.entityRenderer.getShaderGroup().deleteShaderGroup();
			}
		}
	}
}
