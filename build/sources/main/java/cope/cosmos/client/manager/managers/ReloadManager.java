package cope.cosmos.client.manager.managers;

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

    @Override
    public void initialize(Manager manager) {
        manager = new ReloadManager();
    }

    @SubscribeEvent
    public void onJoinWorld(TickEvent.ClientTickEvent event) {
        if (nullCheck() && mc.player.ticksExisted == 10) {
            List<Module> enabledModules = ModuleManager.getModules(Module::isEnabled);

            ModuleManager.getAllModules().forEach(module -> {
                if (!module.isExempt())
                    module.disable();
            });

            enabledModules.forEach(module -> {
                if (!module.isExempt())
                    module.enable();
            });
        }
    }
}
