package cope.cosmos.client.features.modules;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.ModuleToggleEvent.ModuleDisableEvent;
import cope.cosmos.client.events.ModuleToggleEvent.ModuleEnableEvent;
import cope.cosmos.client.features.Feature;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.AnimationManager;
import cope.cosmos.util.Wrapper;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class Module extends Feature implements Wrapper {

	private boolean enabled;
	private boolean drawn;
	private boolean exempt;
	private int key;
	private Supplier<String> info;
	private final Category category;

	private final List<Setting<?>> settings = new ArrayList<>();

	private final AnimationManager animation;

	public Module(String name, Category category, String description) {
		super(name, description);
		this.category = category;

		// get all associated settings
		setFields();

		// default module state
		drawn = true;
		exempt = false;
		key = Keyboard.KEY_NONE;
		animation = new AnimationManager(150, this.enabled);
	}

	public Module(String name, Category category, String description, Supplier<String> info) {
		super(name, description);
		this.category = category;
		this.info = info;

		// get all associated settings
		setFields();

		// default module state
		drawn = true;
		exempt = false;
		key = Keyboard.KEY_NONE;
		animation = new AnimationManager(150, this.enabled);
	}

	private void setFields() {
		Arrays.stream(this.getClass().getDeclaredFields())
				.filter(field -> Setting.class.isAssignableFrom(field.getType()))
				.forEach(field -> {
					field.setAccessible(true);
					try {
						settings.add(((Setting<?>) field.get(this)).setModule(this));
					} catch (IllegalArgumentException | IllegalAccessException exception) {
						exception.printStackTrace();
					}
				});
	}

	public void toggle() {
		if (enabled) {
			disable();
		}

		else {
			enable();
		}
	}

	public void enable() {
		if (!enabled) {
			enabled = true;
			MinecraftForge.EVENT_BUS.register(this);
			if (nullCheck() || Cosmos.INSTANCE.getNullSafeMods().contains(this)) {
				// runs onEnable callbacks
				ModuleEnableEvent event = new ModuleEnableEvent(this);
				MinecraftForge.EVENT_BUS.post(event);

				try {
					onEnable();
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		}
	}

	public void disable() {
		if (enabled) {
			enabled = false;
			if (nullCheck() || Cosmos.INSTANCE.getNullSafeMods().contains(this)) {
				// runs onDisable callbacks
				ModuleDisableEvent event = new ModuleDisableEvent(this);
				MinecraftForge.EVENT_BUS.post(event);

				try {
					onDisable();
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}

			MinecraftForge.EVENT_BUS.unregister(this);
		}
	}

	public void onEnable() {
		// toggle animation & reset world timer
		animation.setState(true);
		Cosmos.INSTANCE.getTickManager().setClientTicks(1);
	}

	public void onDisable() {
		// toggle animation & reset world timer
		animation.setState(false);
		Cosmos.INSTANCE.getTickManager().setClientTicks(1);
	}

	// runs every ticks (i.e. 20 times a second)
	public void onUpdate() {

	}

	// runs on the separate module thread (i.e. every cpu tick)
	public void onThread() {

	}

	// runs on the game overlay tick (i.e. once every frame)
	public void onRender2D() {

	}

	// runs on the global render tick (i.e. once every frame)
	public void onRender3D() {

	}

	public String getName() {
		return name;
	}

	public Category getCategory() {
		return category;
	}

	public String getDescription() {
		return description;
	}

	public String getInfo() {
		return info != null ? info.get() : "";
	}

	public List<Setting<?>> getSettings() {
		return settings;
	}

	public int getKey() {
		return key;
	}

	public void setKey(int in) {
		key = in;
	}

	public AnimationManager getAnimation() {
		return animation;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setDrawn(boolean in) {
		drawn = in;
	}

	public boolean isDrawn() {
		return drawn;
	}

	public void setExempt(boolean in) {
		exempt = in;
	}

	public boolean isExempt() {
		return exempt;
	}

	public boolean isActive() {
		return isEnabled();
	}
}