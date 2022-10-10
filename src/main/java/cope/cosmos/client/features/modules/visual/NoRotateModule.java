package cope.cosmos.client.features.modules.visual;

import cope.cosmos.asm.mixins.accessor.INetHandlerPlayClient;
import cope.cosmos.asm.mixins.accessor.ISPacketPlayerPosLook;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.holder.Rotation;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips, ZimTheDestroyer
 * @since 12/28/2021
 */
public class NoRotateModule extends Module {
    public static NoRotateModule INSTANCE;

    public NoRotateModule() {
        super("NoRotate", Category.VISUAL, "Prevents the server from rotating you");
        INSTANCE = this;
    }

    // **************************** anticheat ****************************

    public static Setting<Boolean> strict = new Setting<>("Strict", false)
            .setDescription("Confirms packets to simulate rotating back");

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {

        if (nullCheck()) {

            // if the client is not done loading the surrounding terrain, DO NOT CANCEL MOVEMENT PACKETS!!!!
            if (!((INetHandlerPlayClient) mc.player.connection).isDoneLoadingTerrain()) {
                return;
            }

            // packet for "rubberband"
            if (event.getPacket() instanceof SPacketPlayerPosLook) {

                // rotations the server wants us to use
                float packetYaw = MathHelper.wrapDegrees(((SPacketPlayerPosLook) event.getPacket()).getYaw());
                float packetPitch = MathHelper.wrapDegrees(((SPacketPlayerPosLook) event.getPacket()).getPitch());

                // previous server rotations
                Rotation serverRotation = getCosmos().getRotationManager().getServerRotation();

                // final yaw
                float yaw = MathHelper.wrapDegrees(serverRotation.getYaw());

                // client rotations
                float clientYaw = MathHelper.wrapDegrees(serverRotation.getYaw());
                float clientPitch = MathHelper.wrapDegrees(serverRotation.getPitch());

                if (packetYaw != clientYaw || packetPitch != clientPitch) {

                    // override the packet's rotations
                    ((ISPacketPlayerPosLook) event.getPacket()).setYaw(mc.player.rotationYaw);
                    ((ISPacketPlayerPosLook) event.getPacket()).setPitch(mc.player.rotationPitch);

                    if (strict.getValue()) {

                        // difference between current and upcoming rotation
                        float angleDifference = packetYaw - clientYaw;

                        // should never be over 180 since the angles are at max 180 and if it's greater than 180 this means we'll be doing a less than ideal turn
                        // (i.e current = 180, required = -180 -> the turn will be 360 degrees instead of just no turn since 180 and -180 are equivalent)
                        // at worst scenario, current = 90, required = -90 creates a turn of 180 degrees, so this will be our max
                        if (Math.abs(angleDifference) > 180) {

                            // adjust yaw, since this is not the true angle difference until we rotate again
                            float adjust = angleDifference > 0 ? -360 : 360;
                            angleDifference += adjust;
                        }

                        // use absolute angle diff
                        // rotating too fast
                        if (Math.abs(angleDifference) > 20) {

                            // ideal rotation direction, so we don't turn in the wrong direction
                            int rotationDirection = angleDifference > 0 ? 1 : -1;

                            // add max angle
                            clientYaw += 20 * rotationDirection;

                            // add our rotation to our client rotations, AutoCrystal has priority over all other rotations
                            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(clientYaw, clientPitch, mc.player.onGround));
                        }

                        // send packet to confirm our rotation
                        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(yaw, clientPitch, mc.player.onGround));
                    }
                }
            }
        }
    }
}
