package cope.cosmos.client.features.modules.misc;

import cope.cosmos.asm.mixins.accessor.ICPacketCloseWindow;
import cope.cosmos.client.events.entity.EntityWorldEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketConfirmTransaction;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.network.status.server.SPacketPong;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 07/21/2021
 */
public class XCarryModule extends Module {
    public static XCarryModule INSTANCE;

    public XCarryModule() {
        super("XCarry", Category.MISC, "Prevents the server from knowing when you open your inventory");
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketCloseWindow) {

            // prevent the client from sending the packet that lets the server know when you've closed your inventory
            if (((ICPacketCloseWindow) event.getPacket()).getWindowID() == mc.player.inventoryContainer.windowId) {
                event.setCanceled(true);
            }
        }
    }
}
