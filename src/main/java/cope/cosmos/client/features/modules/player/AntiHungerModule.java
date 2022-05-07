package cope.cosmos.client.features.modules.player;

import cope.cosmos.asm.mixins.accessor.ICPacketPlayer;
import cope.cosmos.asm.mixins.accessor.IEntityPlayerSP;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author aesthetical, linustouchtips
 * @since 10/27/2021
 */
public class AntiHungerModule extends Module {
    public static AntiHungerModule INSTANCE;

    public AntiHungerModule() {
        super("AntiHunger", Category.PLAYER, "Attempts to negate hunger loss");
        INSTANCE = this;
    }

    // **************************** general ****************************

    public static Setting<Boolean> stopSprint = new Setting<>("StopSprint", true)
            .setDescription("If to cancel sprint packets");

    public static Setting<Boolean> groundSpoof = new Setting<>("GroundSpoof", true)
            .setDescription("Spoof your on ground state");

    // previous sprint state
    private boolean previousSprint;

    @Override
    public void onEnable() {

        // if we are sprinting, we need to stop sprinting
        if (mc.player.isSprinting() || ((IEntityPlayerSP) (mc.player)).getServerSprintState()) {
            previousSprint = true;
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
        }

        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // reset our sprint state
        if (previousSprint) {
            previousSprint = false;
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {

        // packet for jumping
        if (event.getPacket() instanceof CPacketPlayer) {

            if (groundSpoof.getValue()) {

                // compatibility with elytras
                if (!mc.player.isRiding() && !mc.player.isElytraFlying()) {

                    // sets all packets to be considered as onGround, to prevent the server from knowing when we are jumping
                    ((ICPacketPlayer) event.getPacket()).setOnGround(true);
                }
            }
        }

        // packet for sprinting
        else if (event.getPacket() instanceof CPacketEntityAction) {

            // FIX: issue #216 - old if statement was incorrectly casted to CPacketEntityAction when packet was actually CPacketAnimation
            CPacketEntityAction packet = (CPacketEntityAction) event.getPacket();

            // if we are starting to sprint - sprinting looses hunger faster
            if (packet.getAction().equals(Action.START_SPRINTING) || packet.getAction().equals(Action.STOP_SPRINTING)) {

                // start or stop packet
                if (stopSprint.getValue()) {

                    // stops the client from sending sprint packets, to prevent the server from knowing when we are sprinting
                    event.setCanceled(true);
                }
            }
        }
    }
}
