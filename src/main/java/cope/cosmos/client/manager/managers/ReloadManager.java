package cope.cosmos.client.manager.managers;

import cope.cosmos.client.events.EntityWorldEvent;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.util.Wrapper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;

@SuppressWarnings("unused")
public class ReloadManager extends Manager implements Wrapper {
    public ReloadManager() {
        super("ReloadManager", "Reloads all modules when loading a new world", 10);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onEntitySpawn(EntityWorldEvent.EntitySpawnEvent event) {
        if (event.getEntity().equals(mc.player)) {
            List<Module> enabledModules = ModuleManager.getModules(Module::isEnabled);

            ModuleManager.getAllModules().forEach(module -> {
                if (!module.isExempt()) {
                    module.disable();
                }
            });

            enabledModules.forEach(module -> {
                if (!module.isExempt()) {
                    module.enable();
                }
            });
        }
    }
}
