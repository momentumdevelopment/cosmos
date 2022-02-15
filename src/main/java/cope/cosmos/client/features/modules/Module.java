package cope.cosmos.client.features.modules;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.client.ModuleToggleEvent.ModuleDisableEvent;
import cope.cosmos.client.events.client.ModuleToggleEvent.ModuleEnableEvent;
import cope.cosmos.client.features.Feature;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.ui.util.Animation;
import cope.cosmos.util.Wrapper;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author bon55
 * @since 05/05/2021
 */
public class Module extends Feature implements Wrapper {

	// enabled state
	private boolean enabled;

	// drawn state -> visible in the arraylist
	private boolean drawn;

	// exempt from being reloaded
	private boolean exempt;

	// module key bind, pressing this key will toggle the enable state
	private int key;

	// the module Category
	private final Category category;

	// the module arraylist info
	private Supplier<String> info;

	// the module two-way animation manager
	private final Animation animation;

	// all the module's settings
	private final List<Setting<?>> settings = new ArrayList<>();

	public Module(String name, Category category, String description) {
		super(name, description);

		this.category = category;

		// add all associated settings in the class
		Arrays.stream(getClass().getDeclaredFields())
				.filter(field -> Setting.class.isAssignableFrom(field.getType()))
				.forEach(field -> {
					field.setAccessible(true);
					try {
						Setting<?> setting = ((Setting<?>) field.get(this));

						// set the setting's current module as this module
						setting.setModule(this);

						// add it this module's settings
						settings.add(setting);
					} catch (IllegalArgumentException | IllegalAccessException exception) {
						exception.printStackTrace();
					}
				});

		// default module state
		drawn = true;
		key = Keyboard.KEY_NONE;
		animation = new Animation(150, enabled);
	}

	public Module(String name, Category category, String description, Supplier<String> info) {
		this(name, category, description);

		// add module arraylist info
		this.info = info;
	}

	/**
	 * Switches the enabled state
	 */
	public void toggle() {
		if (enabled) {
			disable(true);
		}

		else {
			enable(true);
		}
	}

	/**
	 * Enables the module, subscribing it to the event bus and allowing it to function
	 * @param in Allows the enable event to run
	 */
	public void enable(boolean in) {
		if (!enabled) {
			// set the enabled state to true
			enabled = true;

			Cosmos.EVENT_BUS.register(this);

			if (nullCheck() || getCosmos().getNullSafeFeatures().contains(this)) {
				// runs onEnable callbacks
				if (in) {
					ModuleEnableEvent event = new ModuleEnableEvent(this);
					Cosmos.EVENT_BUS.post(event);
				}

				try {
					onEnable();
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		}
	}

	/**
	 * Disables the module, unsubscribing it from event bus and stopping it from functioning
	 *  * @param in Allows the enable event to run
	 */
	public void disable(boolean in) {
		if (enabled) {
			// sets the enabled state to false
			enabled = false;

			// run the onDisable event
			if (nullCheck() || getCosmos().getNullSafeFeatures().contains(this)) {
				// runs onDisable callbacks
				if (in) {
					ModuleDisableEvent event = new ModuleDisableEvent(this);
					Cosmos.EVENT_BUS.post(event);
				}

				try {
					onDisable();
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}

			Cosmos.EVENT_BUS.unregister(this);
		}
	}

	/**
	 * Runs when the module is enabled
	 */
	public void onEnable() {
		// toggle animation
		getAnimation().setState(true);

		// reset world timer
		getCosmos().getTickManager().setClientTicks(1);
	}

	/**
	 * Runs when the module is disabled
	 */
	public void onDisable() {
		// toggle animation
		getAnimation().setState(false);

		// reset world timer
		getCosmos().getTickManager().setClientTicks(1);
	}

	/**
	 * Runs every update ticks (i.e. 20 times a second)
	 */
	public void onUpdate() {

	}

	/**
	 * Runs every tick (i.e. 40 times a second)
 	 */
	public void onTick() {

	}

	/**
	 * Runs on the separate module thread (i.e. every cpu tick)
	 */
	public void onThread() {

	}

	/**
	 * Runs on the game overlay tick (i.e. once every frame)
	 */
	public void onRender2D() {

	}

	/**
	 * Runs on the global render tick (i.e. once every frame)
	 */
	public void onRender3D() {

	}

	/**
	 * Gets whether or not the module is enabled
	 * @return Whether or not the module is enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets the module's drawn state
	 * @param in The module's new drawn state
	 */
	public void setDrawn(boolean in) {
		drawn = in;
	}

	/**
	 * Gets the module's drawn state
	 * @return The module's drawn state
	 */
	public boolean isDrawn() {
		return drawn;
	}

	/**
	 * Sets the module's exempt state
	 * @param in The module's new exempt state
	 */
	public void setExempt(boolean in) {
		exempt = in;
	}

	/**
	 * Gets the module's exempt state
	 * @return The module's exempt state
	 */
	public boolean isExempt() {
		return exempt;
	}

	/**
	 * Sets the module's keybind
	 * @param in The module's new keybind
	 */
	public void setKey(int in) {
		key = in;
	}

	/**
	 * Gets the module's keybind
	 * @return The module's keybind
	 */
	public int getKey() {
		return key;
	}

	/**
	 * Gets the {@link Category} category associated with this module
	 * @return The category associated with this module
	 */
	public Category getCategory() {
		return category;
	}

	/**
	 * Gets the arraylist info
	 * @return The arraylist info
	 */
	public String getInfo() {
		return info != null ? info.get() : "";
	}

	/**
	 * Gets the two-way animation
	 * @return The two-way animation
	 */
	public Animation getAnimation() {
		return animation;
	}

	/**
	 * Gets a list of the module's settings
	 * @return List of the module's settings
	 */
	public List<Setting<?>> getSettings() {
		return settings;
	}

	/**
	 * Checks whether or not the current module is functioning
	 * @return Whether or not the current module is functioning
	 */
	public boolean isActive() {
		return isEnabled();
	}
}