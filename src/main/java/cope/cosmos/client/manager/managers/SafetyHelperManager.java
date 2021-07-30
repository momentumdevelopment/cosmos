package cope.cosmos.client.manager.managers;

import cope.cosmos.client.events.SettingEnableEvent;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.features.modules.combat.Surround.SurroundVectors;
import cope.cosmos.client.features.modules.movement.PacketFlight.Mode;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.util.Wrapper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SafetyHelperManager extends Manager implements Wrapper {
    public SafetyHelperManager() {
        super("SafetyHelperManager", "Makes sure certain features are toggled safely", 12);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void initialize(Manager manager) {
        manager = new SafetyHelperManager();
    }

    @SuppressWarnings({ "unused", "unchecked", "cast" })
    @SubscribeEvent
    public void onSettingChange(SettingEnableEvent event) {
        if (!mc.isIntegratedServerRunning() && mc.getCurrentServerData() != null) {
            switch (mc.getCurrentServerData().serverIP.toLowerCase()) {
                case "crystalpvp.cc":
                case "us.crystalpvp.cc":
                    if (event.getSetting().getModule().getName().equals("PacketFlight") && event.getSetting().getName().equals("Mode") && !event.getSetting().getValue().equals(Mode.PATCH))
                        pushSafetyNotification("PacketFlight is patched on this server! Using modes other than 'Patch' may cause issues.");

                    else if (event.getSetting().getModule().getName().equals("Offhand") && event.getSetting().getName().equals("Health") && ((Setting<Double>) event.getSetting()).getValue() < 14)
                        pushSafetyNotification("Zero tick AutoCrystal's are enabled on this server! Using a health less than 14 may cause totem fails.");

                    break;
                case "2b2tpvp.net":
                    if (event.getSetting().getModule().getName().equals("Surround") && event.getSetting().getName().equals("Surround") && event.getSetting().getValue().equals(SurroundVectors.BASE))
                        pushSafetyNotification("Mode 'Base' for surround is patched on this server!");

                    else if (event.getSetting().getModule().getName().equals("Burrow"))
                        pushSafetyNotification("Burrow is patched on this server!");

                    break;
                case "2b2t.org":
                    if (event.getSetting().getModule().getName().equals("Surround") && event.getSetting().getName().equals("Surround") && event.getSetting().getValue().equals(SurroundVectors.BASE))
                        pushSafetyNotification("Mode 'Base' for surround is patched on this server!");

                    break;
                case "9b9t.com":
                    break;
            }
        }
    }

    public void pushSafetyNotification(String message) {

    }
}
