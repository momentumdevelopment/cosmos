package cope.cosmos.client.events;

import cope.cosmos.client.features.setting.Setting;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class SettingEnableEvent extends Event {

    private final Setting<?> setting;

    public SettingEnableEvent(Setting<?> setting) {
        this.setting = setting;
    }

    public Setting<?> getSetting() {
        return setting;
    }
}
