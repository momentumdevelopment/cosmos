package cope.cosmos.client.features.modules.player;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.player.InventoryUtil.*;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;

/**
 * @author aesthetical, cattyn
 * @since 10/27/2021
 */
public class NoFall extends Module {
    public static NoFall INSTANCE;

    public NoFall() {
        super("NoFall", Category.PLAYER, "Attempts to negate fall damage");
        INSTANCE = this;
    }

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.PACKET).setDescription("How to negate fall damage");
    public static Setting<Double> distance = new Setting<>("Distance", 1.0, 2.0, 5.0, 1).setDescription("The minimum fall distance before attempting to prevent fall damage");
    public static Setting<Switch> autoSwitch = new Setting<>("Switch", Switch.NORMAL).setDescription("Mode to use when switching to a water bucket").setVisible(() -> mode.getValue().equals(Mode.WATER));
    public static Setting<Double> glideSpeed = new Setting<>("GlideSpeed", 0.1, 1.5, 5.0, 1).setDescription("The factor to slow down fall speed").setVisible(() -> mode.getValue().equals(Mode.GLIDE));
    public static Setting<Boolean> factorize = new Setting<>("Factorize", false).setDescription("Spoof fall distance").setVisible(() -> mode.getValue().equals(Mode.RUBBERBAND));

    @Override
    public void onUpdate() {
        // make sure our fall distance is past our minimum distance
        if (mc.player.fallDistance >= distance.getValue()) {
            switch (mode.getValue()) {
                case PACKET:
                    // spoof on-ground state
                    mc.player.connection.sendPacket(new CPacketPlayer(true));
                    break;
                case GLIDE:
                    // attempt to fall slower
                    mc.player.motionY /= glideSpeed.getValue();
                    mc.player.connection.sendPacket(new CPacketPlayer(true));
                    break;
                case WATER:
                    // save our previous slot
                    int previousSlot = mc.player.inventory.currentItem;

                    // switch to water bucket
                    InventoryUtil.switchToSlot(Items.WATER_BUCKET, autoSwitch.getValue());

                    // attempt to rotate and place water to cancel fall damage
                    mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw, -90, false));
                    mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);

                    // switchback to previous slot
                    if (previousSlot != -1) {
                        InventoryUtil.switchToSlot(previousSlot, autoSwitch.getValue());
                    }

                    break;
                case RUBBERBAND:
                    // send an out of bounds packet
                    if (mc.player.dimension != 1) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, 0, mc.player.posZ, true));
                    }

                    else {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(0, 64, 0, true));
                    }

                    // spoof fall distance
                    if (factorize.getValue()) {
                        mc.player.fallDistance = 0;
                    }

                    break;
            }
        }
    }

    public enum Mode {
        /**
         * Attempts to spoof our on-ground state
         */
        PACKET,

        /**
         * Attempts to slow down fall speed
         */
        GLIDE,

        /**
         * Attempts to place a water bucket at the player's feet to cancel fall damage
         */
        WATER,

        /**
         * Attempts to rubberband to cancel our motion (i.e. slow down our fall speed)
         */
        RUBBERBAND
    }
}
