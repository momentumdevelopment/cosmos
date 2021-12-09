package cope.cosmos.client.features.setting;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.SettingUpdateEvent;
import cope.cosmos.client.features.Feature;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.util.Wrapper;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author bon55, linustouchtips
 * @since 05/05/2021
 * @param <T> The value type for the setting
 */
@SuppressWarnings("unused")
public class Setting<T> extends Feature implements Wrapper {

	// value for the setting
	private T value;

	// min and max values for number settings
	private T min;
	private T max;

	// the number scale (i.e. decimal places) for number settings setting
	private int scale;

	// the parent module to this setting
	private Module module;

	// visibility of the setting in the GUI
	private Supplier<Boolean> visible;

	// the parent setting to this setting
	private Setting<?> parentSetting;

	// list of settings that this setting serves as a parent to
	private final List<Setting<?>> subSettings = new ArrayList<>();
	
	public Setting(String name, T value) {
		super(name);
		this.value = value;

		Cosmos.EVENT_BUS.subscribe(this);
	}
	
	public Setting(String name, T min, T value, T max, int scale) {
		super(name);
		this.min = min;
		this.value = value;
		this.max = max;
		this.scale = scale;

		Cosmos.EVENT_BUS.subscribe(this);
	}

	/**
	 * Gets the minimum value in a number setting
	 * @return The minimum value in the number setting
	 */
	public T getMin() {
		return min;
	}

	/**
	 * Sets the current value of the setting
	 * @param in The new current value of the setting
	 */
	public void setValue(T in) {
		value = in;

		// post the setting update event
		if (nullCheck()) {
			SettingUpdateEvent settingUpdateEvent = new SettingUpdateEvent(this);
			Cosmos.EVENT_BUS.dispatch(settingUpdateEvent);
		}
	}

	/**
	 * Gets the current value of the setting
	 * @return The current value of the setting
	 */
	public T getValue() {
		return value;
	}

	/**
	 * Sets the minimum value in a number setting
	 * @param in The new minimum value in the number setting
	 */
	public void setMin(T in) {
		min = in;
	}

	/**
	 * Gets the maximum value in a number setting
	 * @return The maximum value in the number setting
	 */
	public T getMax() {
		return max;
	}

	/**
	 * Sets the maximum value in a number setting
	 * @param in The new maximum value in the number setting
	 */
	public void setMax(T in) {
		max = in;
	}

	/**
	 * Gets the rounding scale in a number setting
	 * @return The rounding scale in the number setting
	 */
	public int getRoundingScale() {
		return scale;
	}

	/**
	 * Sets the current module of the setting
	 * @param in The new current module of the setting
	 */
	public void setModule(Module in) {
		module = in;
	}

	/**
	 * Gets the current module of the setting
	 * @return The current module of the setting
	 */
	public Module getModule() {
		return module;
	}

	/**
	 * Checks if the setting is visible
	 * @return Whether the setting is visible
	 */
	public boolean isVisible() {
		return visible != null ? visible.get() : true;
	}

	/**
	 * Sets the visibility of the setting
	 * @param in The new visibility of the setting
	 * @return The setting
	 */
	public Setting<T> setVisible(Supplier<Boolean> in) {
		// update our visibility
		visible = in;

		// builder
		return this;
	}

	/**
	 * Checks if the setting has a parent
	 * @return Whether the setting has a parent
	 */
	public boolean hasParent() {
		return parentSetting != null;
	}

	/**
	 * Gets the parent of the setting
	 * @return The parent of the setting
	 */
	public Setting<?> getParentSetting() {
		return parentSetting;
	}

	/**
	 * Sets the parent of the setting
	 * @param in The new parent of the setting
	 * @return The setting
	 */
	public Setting<T> setParent(Setting<?> in) {
		// add this setting to the parent's subsetting list
		in.getSubSettings().add(this);

		// update our parent setting
		parentSetting = in;

		// builder
		return this;
	}

	/**
	 * Gets a list of the subsettings for the setting
	 * @return A list of the subsettings for the setting
	 */
	public List<Setting<?>> getSubSettings() {
		return subSettings;
	}

	/**
	 * Sets the description of the setting
	 * @param in The new description of the setting
	 * @return The setting
	 */
	public Setting<T> setDescription(String in) {
		// update description
		description = in;

		// builder
		return this;
	}

	@Override
	public String getDescription() {
		return description != null ? description : "";
	}
}
