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

    public static Setting<Boolean> stopSprint = new Setting<>("StopSprint", "If to cancel sprint packets", true);
    public static Setting<Boolean> stopJump = new Setting<>("StopJump", "Spoof your on ground state", true);

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (stopJump.getValue() && event.getPacket() instanceof CPacketPlayer) {
            ((ICPacketPlayer) event.getPacket()).setOnGround(true);
        }

        else if (event.getPacket() instanceof CPacketEntityAction) {
            if (stopSprint.getValue() && ((CPacketEntityAction) event.getPacket()).getAction().equals(CPacketEntityAction.Action.START_SPRINTING)) {
                event.setCanceled(true);
            }
        }
    }
}
