package cope.cosmos.client.features.modules.player;

import cope.cosmos.asm.mixins.accessor.ICPacketPlayer;
import cope.cosmos.asm.mixins.accessor.IEntityPlayerSP;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author aesthetical, linustouchtips
 * @since 10/27/2021
 */
public class AntiHunger extends Module {
    public static AntiHunger INSTANCE;

    public AntiHunger() {
        super("AntiHunger", Category.PLAYER, "Attempts to negate hunger loss");
        INSTANCE = this;
    }

    public static Setting<Boolean> stopSprint = new Setting<>("StopSprint", true).setDescription("If to cancel sprint packets");
    public static Setting<Boolean> stopJump = new Setting<>("StopJump", true).setDescription("Spoof your on ground state");

    // previous sprint state
    private boolean previousSprint;

    @Override
    public void onEnable() {
        super.onEnable();

        // if we are sprinting, we need to stop sprinting
        if (mc.player.isSprinting() || ((IEntityPlayerSP) (mc.player)).getServerSprintState()) {
            previousSprint = true;
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
        }
    }

    @Override
    public void onDisable() {
        super.onEnable();

        // reset our sprint state
        if (previousSprint) {
            previousSprint = false;
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        // sets all packets to be considered as onGround, to prevent the server from knowing when we are jumping
        if (stopJump.getValue() && event.getPacket() instanceof CPacketPlayer) {
            // compatibility with elytras
            if (!mc.player.isElytraFlying()) {
                ((ICPacketPlayer) event.getPacket()).setOnGround(true);
            }
        }

        // stops the client from sending sprint packets, to prevent the server from knowing when we are sprinting
        if (stopSprint.getValue() && event.getPacket() instanceof CPacketEntityAction) {
            if (((CPacketEntityAction) event.getPacket()).getAction().equals(CPacketEntityAction.Action.START_SPRINTING) || ((CPacketEntityAction) event.getPacket()).getAction().equals(CPacketEntityAction.Action.STOP_SPRINTING)) {
                event.setCanceled(true);
            }
        }
    }
}
