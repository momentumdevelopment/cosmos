package cope.cosmos.client.features.modules.player;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.movement.PacketFlightModule;
import cope.cosmos.client.features.setting.Setting;
import net.minecraft.network.play.client.CPacketPlayer;

/**
 * @author aesthetical, linustouchtips
 * @since 11/21/2021
 */
public class AntiVoidModule extends Module {
    public static AntiVoidModule INSTANCE;

    public AntiVoidModule() {
        super("AntiVoid", Category.PLAYER, "Prevents you from getting stuck in the void");
        INSTANCE = this;
    }

    // **************************** general ****************************

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.SOLID)
            .setDescription("How to stop you from falling into the void");

    @Override
    public void onTick() {

        // can't void if spectator or if packetfly is on
        if (!mc.player.isSpectator() && !PacketFlightModule.INSTANCE.isEnabled()) {

            // void
            if (mc.player.posY < 1) {

                // notify the player that we are attempting to get out of the void
                getCosmos().getChatManager().sendClientMessage("[AntiVoid] " + ChatFormatting.RED + "Attempting to get player out of void!", -6980085);

                switch (mode.getValue()) {
                    case SOLID:

                        // stop all vertical motion
                        mc.player.motionY = 0;
                        break;
                    case TRAMPOLINE:

                        // attempt to float up out of the void
                        mc.player.motionY = 0.5;
                        break;
                    case GLIDE:

                        // fall slower
                        if (mc.player.motionY < 0) {
                            mc.player.motionY /= 3;
                        }

                        break;
                    case RUBBERBAND:

                        // stop motion
                        mc.player.setVelocity(0, 0, 0);

                        // attempt to rubberband out of the void
                        // mc.player.setPosition(mc.player.posX, mc.player.posY + 10, mc.player.posZ);
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 10, mc.player.posZ, false));
                        break;
                }
            }
        }
    }

    public enum Mode {

        /**
         * Makes the void block completely solid
         */
        SOLID,

        /**
         * Stops all vertical motion, freezes the player
         */
        SOLID_STRICT,

        /**
         * Attempts to jump up out of the void
         */
        TRAMPOLINE,

        /**
         * Slows down vertical movement
         */
        GLIDE,

        /**
         * Attempts to rubberband out of the void
         */
        RUBBERBAND
    }
}
