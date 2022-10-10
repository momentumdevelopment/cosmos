package cope.cosmos.client.features.modules.misc;

import cope.cosmos.asm.mixins.accessor.ICPacketCloseWindow;
import cope.cosmos.asm.mixins.accessor.ICPacketPlayer;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 07/21/2021
 */
public class XCarryModule extends Module {
    public static XCarryModule INSTANCE;

    public XCarryModule() {
        super("XCarry", new String[] {"ExtraSlots", "SecretClose"}, Category.MISC, "Prevents the server from knowing when you open your inventory");
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

        if (event.getPacket() instanceof CPacketPlayer) {

            if (((ICPacketPlayer) event.getPacket()).isMoving()) {

                double diff = ((CPacketPlayer) event.getPacket()).getY(-1) - mc.player.posY;

                getCosmos().getChatManager().sendClientMessage(diff, 1);
            }
        }
    }
}
