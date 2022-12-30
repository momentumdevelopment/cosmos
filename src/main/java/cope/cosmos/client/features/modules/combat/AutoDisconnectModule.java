package cope.cosmos.client.features.modules.combat;

import cope.cosmos.client.events.network.ConnectEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.math.Timer;
import cope.cosmos.util.math.Timer.Format;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.player.PlayerUtil;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraft.init.Items;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 02/23/2022
 */
public class AutoDisconnectModule extends Module {
    public static AutoDisconnectModule INSTANCE;

    public AutoDisconnectModule() {
        super("AutoDisconnect", new String[] {"AutoLog", "AutoLogout"}, Category.COMBAT, "Automatically disconnects from servers when in danger");
        INSTANCE = this;
    }

    // **************************** general settings ****************************

    public static Setting<Double> health = new Setting<>("Health", 0.0D, 2.0D, 36.0D, 1)
            .setDescription("Health considered as critical health");

    public static Setting<Double> totems = new Setting<>("Totems", 0.0D, 0.0D, 10.0D, 0)
            .setDescription("Totem count considered as critical count");

    // timer to wait after disconnecting
    private final Timer disconnectTimer = new Timer();

    @Override
    public void onTick() {

        // disconnect timer has waited at least 5 seconds
        if (disconnectTimer.passedTime(5, Format.SECONDS)) {

            // disconnect if health is too low
            if (PlayerUtil.getHealth() <= health.getValue()) {
                disconnectClient();
                disconnectTimer.resetTime();
            }

            // disconnect if totem count is too low
            else if (InventoryUtil.getItemCount(Items.TOTEM_OF_UNDYING) <= totems.getValue()) {
                disconnectClient();
                disconnectTimer.resetTime();
            }
        }
    }

    @SubscribeEvent
    public void onConnect(ConnectEvent event) {

        // we just connected, give it some time before we disconnect again
        disconnectTimer.resetTime();
    }

    /**
     * Disconnects the client from the current server and takes the user back to the Multiplayer Server Selector screen
     */
    public void disconnectClient() {

        // disable the module
        disable(false);

        // screen to return to
        GuiScreen disconnectScreen;
        if (mc.isSingleplayer()) {
            disconnectScreen = new GuiWorldSelection(new GuiMainMenu());
        }

        else {
            disconnectScreen = new GuiMultiplayer(new GuiMainMenu());
        }

        // disconnect and unload the world
        mc.world.sendQuittingDisconnectingPacket();
        mc.loadWorld(null);

        // take the player back to the main menu
        mc.displayGuiScreen(disconnectScreen);
    }
}
