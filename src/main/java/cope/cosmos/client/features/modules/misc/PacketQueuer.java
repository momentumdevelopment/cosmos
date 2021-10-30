package cope.cosmos.client.features.modules.misc;

import cope.cosmos.asm.mixins.accessor.ICPacketPlayer;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.util.client.ChatUtil;
import cope.cosmos.util.world.AngleUtil;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Comparator;

public class PacketQueuer extends Module {
    public PacketQueuer() {
        super("PacketQueuer", Category.MISC, "Logs packets");
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketPlayer) {
            /*
            if (((ICPacketPlayer) (event.getPacket())).isMoving()) {
                ChatUtil.sendMessage("X: " + ((CPacketPlayer) event.getPacket()).getX(0));
                ChatUtil.sendMessage("Y: " + ((CPacketPlayer) event.getPacket()).getY(0));
                ChatUtil.sendMessage("Z: " + ((CPacketPlayer) event.getPacket()).getZ(0));
            }
             */

            /*
            ChatUtil.sendMessage("OnGround: " + ((CPacketPlayer) event.getPacket()).isOnGround());
             */
        }

        /*
        try {
            if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
                ChatUtil.sendMessage("Direction: " + ((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getDirection());
                ChatUtil.sendMessage("X: " + ((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getFacingX() + ", " + mc.player.posX);
                ChatUtil.sendMessage("Y: " + ((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getFacingY() + ", " + mc.player.posY);
                ChatUtil.sendMessage("Z: " + ((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getFacingZ() + ", " + mc.player.posZ);
            }
        } catch (Exception ignored) {

        }
         */
    }
}
