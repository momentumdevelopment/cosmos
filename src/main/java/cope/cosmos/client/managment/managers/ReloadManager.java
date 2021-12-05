package cope.cosmos.client.managment.managers;

import cope.cosmos.client.events.EntityWorldEvent;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.managment.Manager;
import cope.cosmos.utility.IUtility;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

@SuppressWarnings("unused")
public class ReloadManager extends Manager implements IUtility {
    public ReloadManager() {
        super("ReloadManager", "Reloads all modules when loading a new world");
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
