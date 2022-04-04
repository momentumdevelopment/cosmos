package cope.cosmos.client.features.modules.player;

import cope.cosmos.asm.mixins.accessor.ISPacketPlayerPosLook;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 12/28/2021
 */
public class NoRotateModule extends Module {
    public static NoRotateModule INSTANCE;

    public NoRotateModule() {
        super("NoRotate", Category.PLAYER, "Prevents the server from rotating you");
        INSTANCE = this;
    }

    // **************************** anticheat ****************************

    public static Setting<Boolean> strict = new Setting<>("Strict", false)
            .setDescription("Confirms packets to simulate rotating back");

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {

        // packet for "rubberband"
        if (event.getPacket() instanceof SPacketPlayerPosLook) {

            // rotations the server wants us to use
            float packetYaw = ((SPacketPlayerPosLook) event.getPacket()).getYaw();
            float packetPitch = ((SPacketPlayerPosLook) event.getPacket()).getPitch();

            if (packetYaw != mc.player.rotationYaw || packetPitch != mc.player.rotationPitch) {

                // override the packet's rotations
                ((ISPacketPlayerPosLook) event.getPacket()).setYaw(mc.player.rotationYaw);
                ((ISPacketPlayerPosLook) event.getPacket()).setPitch(mc.player.rotationPitch);

                if (strict.getValue()) {

                    // check if the yaw difference is greater than 55, this speed flags NCP Updated
                    float yawDifference = Math.abs(mc.player.rotationYaw - packetYaw);
                    if (yawDifference >= 55) {

                        // split the yaw, and send a half rotation packet
                        float splitYaw = (packetYaw - mc.player.rotationYaw) / 2;
                        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(splitYaw, mc.player.rotationPitch, mc.player.onGround));
                    }

                    // send packet to confirm our rotation
                    mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw, mc.player.rotationPitch, mc.player.onGround));
                }
            }
        }
    }
}
