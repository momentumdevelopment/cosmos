package cope.cosmos.client.features.modules.miscellaneous;

import cope.cosmos.asm.mixins.accessor.ICPacketCloseWindow;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 07/21/2021
 */
public class XCarryModule extends Module {
    public static XCarryModule INSTANCE;

    public XCarryModule() {
        super("XCarry", new String[] {"ExtraSlots", "SecretClose"}, Category.MISCELLANEOUS, "Prevents the server from knowing when you open your inventory");
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {

        // packet for closing windows
        if (event.getPacket() instanceof CPacketCloseWindow) {

            // prevent the client from sending the packet that lets the server know when you've closed your inventory
            if (((ICPacketCloseWindow) event.getPacket()).getWindowID() == mc.player.inventoryContainer.windowId) {
                event.setCanceled(true);
            }
        }
    }
}
