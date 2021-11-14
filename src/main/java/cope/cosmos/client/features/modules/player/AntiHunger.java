package cope.cosmos.client.features.modules.player;

import cope.cosmos.asm.mixins.accessor.ICPacketPlayer;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AntiHunger extends Module {
    public static AntiHunger INSTANCE;

    public AntiHunger() {
        super("AntiHunger", Category.PLAYER, "Attempts to negate hunger loss");
        INSTANCE = this;
    }

    public static final Setting<Boolean> stopSprint = new Setting<>("StopSprint", "If to cancel sprint packets", true);
    public static final Setting<Boolean> stopJump = new Setting<>("StopJump", "Spoof your on ground state", true);


    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketPlayer) {
            if (!stopJump.getValue()) {
                return;
            }

            ((ICPacketPlayer) event.getPacket()).setOnGround(true);
        }

        else if (event.getPacket() instanceof CPacketEntityAction) {
            if (!stopSprint.getValue()) {
                return;
            }

            if (((CPacketEntityAction) event.getPacket()).getAction() == CPacketEntityAction.Action.START_SPRINTING) {
                event.setCanceled(true);
            }
        }
    }
}
