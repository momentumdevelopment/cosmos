package cope.cosmos.client.features.modules.client;

import cope.cosmos.client.events.SettingEnableEvent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;

import java.awt.*;

public class ClickGUI extends Module {
	public static ClickGUI INSTANCE;
	
	public ClickGUI() {
		super("ClickGUI", Category.CLIENT, "This screen.");
		setKey(Keyboard.KEY_RSHIFT);
		setExempt(true);
		INSTANCE = this;
	}

	public static Setting<cope.cosmos.client.features.modules.client.ClickGUI.GUI> mode = new Setting<>("Mode", "The mode for the GUI screen", cope.cosmos.client.features.modules.client.ClickGUI.GUI.WINDOW);
	public static Setting<Color> primaryColor = new Setting<>("PrimaryColor", "The primary color for the GUI", new Color(154, 81, 200, 255));
	public static Setting<Color> backgroundColor = new Setting<>("BackgroundColor", "The background color for the GUI", new Color(23, 23, 29, 255));
	public static Setting<Color> accentColor = new Setting<>("AccentColor", "The accent color for the GUI", new Color(35, 35, 45, 255));
	public static Setting<Color> secondaryColor = new Setting<>("SecondaryColor", "The secondary color for the GUI", new Color(12, 12, 17, 255));
	public static Setting<Color> complexionColor = new Setting<>("ComplexionColor", "The complexion color for the GUI", new Color(18, 18, 24, 255));
	public static Setting<Boolean> pauseGame = new Setting<>("PauseGame", "Pause the game when in GUI", false);
	public static Setting<Boolean> windowBlur = new Setting<>("WindowBlur", "Blur shader for GUI Panels", true);
	public static Setting<Integer> panelBlurInt = new Setting<>("BlurIntensity", "Intensity for panel blur shader", 1, 6, 10, 1).setParent(windowBlur);
	public static Setting<Boolean> blur = new Setting<>("Blur", "Blur shader for GUI background", false);

	@Override
	public void onEnable() {
		super.onEnable();

		mc.displayGuiScreen(Cosmos.INSTANCE.getWindowGUI());
		MinecraftForge.EVENT_BUS.register(Cosmos.INSTANCE.getWindowGUI());

		// blur shader for background
		if (blur.getValue()) {
			mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/blur.json"));
		}
	}

	@SubscribeEvent
	public void onSettingEnable(SettingEnableEvent event) {
		if (event.getSetting().equals(blur)) {

			if (blur.getValue()) {
				mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/blur.json"));
			}

			else if (mc.entityRenderer.isShaderActive()) {
				mc.entityRenderer.getShaderGroup().deleteShaderGroup();
			}
		}
	}

	public Color getPrimaryColor() {
		return primaryColor.getValue();
	}

	public Color getBackgroundColor() {
		return backgroundColor.getValue();
	}

	public Color getAccentColor() {
		return accentColor.getValue();
	}

	public Color getSecondaryColor() {
		return secondaryColor.getValue();
	}

	public Color getComplexionColor() {
		return complexionColor.getValue();
	}

	public enum GUI {
		WINDOW(Cosmos.INSTANCE.getWindowGUI()), COSMOS(Cosmos.INSTANCE.getCosmosGUI());

		private final GuiScreen screen;

		GUI(GuiScreen screen) {
			this.screen = screen;
		}

		public GuiScreen getScreen() {
			return screen;
		}
	}
}
