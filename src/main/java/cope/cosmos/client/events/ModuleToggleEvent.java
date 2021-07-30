package cope.cosmos.client.events;

import cope.cosmos.client.features.modules.Module;
import net.minecraftforge.fml.common.eventhandler.Event;

public class ModuleToggleEvent extends Event {
	private Module m;
	
	public ModuleToggleEvent(Module m) {
		this.m = m;
	}
	
	public static class ModuleEnableEvent extends ModuleToggleEvent {
		public ModuleEnableEvent(Module m) {
			super(m);
		}
	}
	
	public static class ModuleDisableEvent extends ModuleToggleEvent {
		public ModuleDisableEvent(Module m) {
			super(m);
		}
	}
	
	public Module getModule() {
		return this.m;
	}
}
