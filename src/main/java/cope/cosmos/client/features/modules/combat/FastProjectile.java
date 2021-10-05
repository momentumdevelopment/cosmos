package cope.cosmos.client.features.modules.combat;

import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.util.player.InventoryUtil;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FastProjectile extends Module {
    public static FastProjectile INSTANCE;

    public FastProjectile() {
        super("FastProjectile", Category.COMBAT, "Allows your projectiles to do more damage");
        INSTANCE = this;
    }
    
    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketPlayerDigging && ((CPacketPlayerDigging) event.getPacket()).getAction().equals(CPacketPlayerDigging.Action.RELEASE_USE_ITEM)) {
            if (InventoryUtil.isHolding(Items.BOW) && mc.player.getItemInUseMaxCount() >= 20) {
                for (int ticks = 0; ticks < 10; ticks++) {
                    double sin = -Math.sin(Math.toRadians(mc.player.rotationYaw));
                    double cos = Math.cos(Math.toRadians(mc.player.rotationYaw));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 0.1, mc.player.posZ, false));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + sin * 100, mc.player.posY, mc.player.posZ + cos * 100, true));
                }
            }
        }
    }
}
