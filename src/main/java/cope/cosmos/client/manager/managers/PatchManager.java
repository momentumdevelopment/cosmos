package cope.cosmos.client.manager.managers;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.client.ModuleToggleEvent;
import cope.cosmos.client.events.client.SettingUpdateEvent;
import cope.cosmos.client.events.entity.EntityWorldEvent;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.combat.SelfFillModule;
import cope.cosmos.client.features.modules.movement.ElytraFlightModule;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.client.manager.managers.NotificationManager.Notification;
import cope.cosmos.client.manager.managers.NotificationManager.Type;
import cope.cosmos.util.Wrapper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class PatchManager extends Manager implements Wrapper {

    private final Map<Patch, PatchState> patchMap = new ConcurrentHashMap<>();

    public PatchManager() {
        super("PatchManager", "Makes sure certain features are toggled safely");
        Cosmos.EVENT_BUS.register(this);

        // set the patches on initialization
        updatePatches();
    }

    @SubscribeEvent
    public void onEntitySpawn(EntityWorldEvent.EntitySpawnEvent event) {
        if (event.getEntity().equals(mc.player)) {
            // reset our patch list each time we spawn
            patchMap.clear();
            updatePatches();
        }
    }

    @SubscribeEvent
    public void onModuleEnable(ModuleToggleEvent.ModuleEnableEvent event) {
        if (!mc.isIntegratedServerRunning() && mc.getCurrentServerData() != null && mc.player.ticksExisted >= 40) {
            patchMap.forEach((patch, patchState) -> {
                if (patch.getModule() != null && patch.getModule().equals(event.getModule()) && patch.getServers().contains(mc.getCurrentServerData().serverIP.toLowerCase())) {
                    switch (patchState) {
                        case PATCHED:
                            pushPatchSafety(patch.getModule().getName() + " is likely patched on " + mc.getCurrentServerData().serverIP.toLowerCase() + "!");
                            break;
                        case UNPATCHED:
                            pushPatchSafety(patch.getModule().getName() + " is likely not patched on " + mc.getCurrentServerData().serverIP.toLowerCase() + "!");
                            break;
                        case LIMITED:
                            pushPatchSafety(patch.getModule().getName() + " is likely limited on " + mc.getCurrentServerData().serverIP.toLowerCase() + "!");
                            break;
                        case INCONSISTENT:
                            pushPatchSafety(patch.getModule().getName() + " is likely inconsistent on " + mc.getCurrentServerData().serverIP.toLowerCase() + "!");
                            break;
                    }

                    patchMap.remove(patch);
                }
            });
        }
    }

    @SubscribeEvent
    public void onSettingChange(SettingUpdateEvent event) {
        if (!mc.isIntegratedServerRunning() && mc.getCurrentServerData() != null && mc.player.ticksExisted >= 40) {
            patchMap.forEach((patch, patchState) -> {
                if (patch.getSetting() != null && patch.getSetting().equals(event.getSetting()) && patch.getState() && patch.getServers().contains(mc.getCurrentServerData().serverIP.toLowerCase())) {
                    switch (patchState) {
                        case PATCHED:
                            pushPatchSafety(event.getSetting().getName() + " is likely patched on " + mc.getCurrentServerData().serverIP.toLowerCase() + "!");
                            break;
                        case UNPATCHED:
                            pushPatchSafety(event.getSetting().getName() + " is likely not patched on " + mc.getCurrentServerData().serverIP.toLowerCase() + "!");
                            break;
                        case LIMITED:
                            pushPatchSafety(event.getSetting().getName() + " is likely limited on " + mc.getCurrentServerData().serverIP.toLowerCase() + "!");
                            break;
                        case INCONSISTENT:
                            pushPatchSafety(event.getSetting().getName() + " is likely inconsistent on " + mc.getCurrentServerData().serverIP.toLowerCase() + "!");
                            break;
                    }

                    patchMap.remove(patch);
                }
            });
        }
    }

    public void updatePatches() {
        patchMap.put(new Patch(SelfFillModule.INSTANCE, "crystalpvp.cc", "us.crystalpvp.cc", "constantiam.org", "2b2tpvp.net", "strict.2b2tpvp.net", "eliteanarchy.net"), PatchState.PATCHED);
        patchMap.put(new Patch(ElytraFlightModule.INSTANCE, "2b2t.org", "constantiam.org"), PatchState.PATCHED);
        // patchMap.put(new Patch(SurroundModule.mode, SurroundModule.mode.getValue().equals(SurroundVectors.BASE), "2b2t.org", "strict.2b2tpvp.net"), PatchState.PATCHED);
    }

    public void pushPatchSafety(String message) {
        if (mc.currentScreen == null) {
            Notification patchError = new Notification(message, Type.WARNING);
            Cosmos.INSTANCE.getNotificationManager().addNotification(patchError);
        }

        /*
        else if (mc.currentScreen.equals(Cosmos.INSTANCE.getWindowGUI())) {
            ErrorWindow patchError = new ErrorWindow("Patch Safety", message, new Vec2f(100, 100));
            Cosmos.INSTANCE.getWindowGUI().getManager().createWindow(patchError);
        }
         */
    }

    public enum PatchState {
        PATCHED, UNPATCHED, INCONSISTENT, LIMITED
    }

    public static class Patch {

        private Module module;

        private Setting<?> setting;
        private boolean state;

        private final List<String> patchedServers = new ArrayList<>();

        public Patch(Module module, String... servers) {
            this.module = module;
            patchedServers.addAll(Arrays.asList(servers));
        }

        public Patch(Setting<?> setting, boolean state, String... servers) {
            this.setting = setting;
            this.state = state;
            patchedServers.addAll(Arrays.asList(servers));
        }

        public Module getModule() {
            return module;
        }

        public Setting<?> getSetting() {
            return setting;
        }

        public boolean getState() {
            return state;
        }

        public List<String> getServers() {
            return patchedServers;
        }
    }
}
