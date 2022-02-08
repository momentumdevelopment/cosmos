package cope.cosmos.client.events.client;

import cope.cosmos.client.features.modules.Module;
import net.minecraftforge.fml.common.eventhandler.Event;

public class ModuleToggleEvent extends Event {

	private final Module module;
	
	public ModuleToggleEvent(Module module) {
		this.module = module;
	}
	
	public static class ModuleEnableEvent extends ModuleToggleEvent {
		public ModuleEnableEvent(Module module) {
			super(module);
		}
	}
	
	public static class ModuleDisableEvent extends ModuleToggleEvent {
		public ModuleDisableEvent(Module module) {
			super(module);
		}
	}
	
	public Module getModule() {
		return module;
	}
}
