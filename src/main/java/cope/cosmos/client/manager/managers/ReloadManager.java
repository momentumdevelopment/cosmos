package cope.cosmos.client.manager.managers;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.entity.EntityWorldEvent;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.util.Wrapper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

/**
 * @author linustouchtips
 * @since 06/08/2021
 */
public class ReloadManager extends Manager implements Wrapper {
    public ReloadManager() {
        super("ReloadManager", "Reloads all modules when loading a new world");
        Cosmos.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onEntitySpawn(EntityWorldEvent.EntitySpawnEvent event) {

        // on spawn
        if (event.getEntity().equals(mc.player)) {

            // previously enabled modules
            List<Module> enabledModules = getCosmos().getModuleManager().getModules(Module::isEnabled);

            // disable all modules
            getCosmos().getModuleManager().getAllModules().forEach(module -> {

                // exempt property prevents modules from being reloaded
                if (!module.isExempt()) {
                    module.disable(false);
                }
            });

            // re-enable previously enabled modules
            enabledModules.forEach(module -> {

                // exempt property prevents modules from being reloaded
                if (!module.isExempt()) {
                    module.enable(false);
                }
            });
        }
    }
}
