package cope.cosmos.client.features.modules.misc;

import cope.cosmos.asm.mixins.accessor.ICPacketCloseWindow;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import net.minecraft.network.play.client.CPacketCloseWindow;

@SuppressWarnings("unused")
public class XCarry extends Module {
    public static XCarry INSTANCE;

    public XCarry() {
        super("XCarry", Category.MISC, "Prevents the server from knowing when you open your inventory");
        INSTANCE = this;
    }

    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketCloseWindow)
            event.setCanceled(((ICPacketCloseWindow) event.getPacket()).getWindowID() == mc.player.inventoryContainer.windowId);
    }
}
