package cope.cosmos.client.features.setting;

import cope.cosmos.client.events.SettingEnableEvent;
import cope.cosmos.client.features.Feature;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.util.Wrapper;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class Setting<T> extends Feature implements Wrapper {
	
	private T min;
	private T value;
	private T max;
	
	private int scale;
	private int index;

	private Module module;

	private Supplier<Boolean> visible;

	private Setting<?> parentSetting;
	private final List<Setting<?>> subSettings = new ArrayList<>();
	
	public Setting(String name, String description, T value) {
		super(name, description);
		this.value = value;
	}
	
	public Setting(Supplier<Boolean> visible, String name, String description, T value) {
		super(name, description);
		this.visible = visible;
		this.value = value;
	}
	
	public Setting(String name, String description, T min, T value, T max, int scale) {
		super(name, description);
		this.min = min;
		this.value = value;
		this.max = max;
		this.scale = scale;
	}
	
	public Setting(Supplier<Boolean> visible, String name, String description, T min, T value, T max, int scale) {
		super(name, description);
		this.visible = visible;
		this.min = min;
		this.value = value;
		this.max = max;
		this.scale = scale;
	}
	
	public T getMin() {
		return min;
	}
	
	public T getValue() {
		return value;
	}
	
	public void setValue(T in) {
		value = in;

		if (nullCheck()) {
			SettingEnableEvent settingEnableEvent = new SettingEnableEvent(this);
			MinecraftForge.EVENT_BUS.post(settingEnableEvent);
		}
	}
	
	public T getMax() {
		return max;
	}

	public int getRoundingScale() {
		return scale;
	}

	public Module getModule() {
		return module;
	}

	public Setting<T> setModule(Module in) {
		module = in;
		return this;
	}

	public boolean isVisible() {
		return visible != null ? visible.get() : true;
	}

	public boolean hasParent() {
		return parentSetting != null;
	}

	public Setting<?> getParentSetting() {
		return parentSetting;
	}

	public void setParentSetting(Setting<?> in) {
		parentSetting = in;
	}

	public Setting<T> setParent(Setting<?> in) {
		in.getSubSettings().add(this);
		parentSetting = in;
		return this;
	}

	public List<Setting<?>> getSubSettings() {
		return subSettings;
	}

	public T getNextMode() {
		if (value instanceof Enum<?>) {
			Enum<?> enumVal = (Enum<?>) value;
			String[] values = Arrays.stream(enumVal.getClass().getEnumConstants()).map(Enum::name).toArray(String[]::new);
			index = index + 1 > values.length - 1 ? 0 : index + 1;
			return (T) Enum.valueOf(enumVal.getClass(), values[index]);
		}

		return null;
	}

	public T getPreviousMode() {
		if (value instanceof Enum<?>) {
			Enum<?> enumVal = (Enum<?>) value;
			String[] values = Arrays.stream(enumVal.getClass().getEnumConstants()).map(Enum::name).toArray(String[]::new);
			index = index - 1 < 0 ? values.length - 1 : index - 1;
			return (T) Enum.valueOf(enumVal.getClass(), values[index]);
		}

		return null;
	}
	
	public static String formatEnum(Enum<?> enumIn) {
		String enumName = enumIn.name();
		if (!enumName.contains("_")) {
			char firstChar = enumName.charAt(0);
			String suffixChars = enumName.split(String.valueOf(firstChar), 2)[1];
			return String.valueOf(firstChar).toUpperCase() + suffixChars.toLowerCase();
		}

		String[] names = enumName.split("_");
		StringBuilder nameToReturn = new StringBuilder();

		for (String s : names) {
			char firstChar = s.charAt(0);
			String suffixChars = s.split(String.valueOf(firstChar), 2)[1];
			nameToReturn.append(String.valueOf(firstChar).toUpperCase()).append(suffixChars.toLowerCase());
		}

		return nameToReturn.toString();
	}
}
