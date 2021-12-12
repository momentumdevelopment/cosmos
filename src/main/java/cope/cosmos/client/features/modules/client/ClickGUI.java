package cope.cosmos.client.features.modules.client;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.SettingUpdateEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

public class ClickGUI extends Module {
	public static ClickGUI INSTANCE;
	
	public ClickGUI() {
		super("ClickGUI", Category.CLIENT, "This screen.");
		setKey(Keyboard.KEY_RSHIFT);
		setExempt(true);
		INSTANCE = this;
	}

	public static Setting<Boolean> pauseGame = new Setting<>("PauseGame", false).setDescription("Pause the game when in GUI");
	public static Setting<Boolean> windowBlur = new Setting<>("WindowBlur", false).setDescription("Blur shader for GUI Panels");
	public static Setting<Boolean> blur = new Setting<>("Blur", false).setDescription("Blur shader for GUI background");

	@Override
	public void onEnable() {
		super.onEnable();

		mc.displayGuiScreen(getCosmos().getWindowGUI());
		Cosmos.EVENT_BUS.register(getCosmos().getWindowGUI());

		// blur shader for background
		if (blur.getValue()) {
			mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/blur.json"));
		}
	}

	@SubscribeEvent
	public void onSettingEnable(SettingUpdateEvent event) {
		if (event.getSetting().equals(blur)) {
			if (blur.getValue()) {
				mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/blur.json"));
			}

			else if (mc.entityRenderer.isShaderActive()) {
				mc.entityRenderer.getShaderGroup().deleteShaderGroup();
			}
		}
	}
}
