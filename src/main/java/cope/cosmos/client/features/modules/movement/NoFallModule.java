package cope.cosmos.client.features.modules.movement;

import cope.cosmos.asm.mixins.accessor.ICPacketPlayer;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.InventoryManager.Switch;
import cope.cosmos.util.holder.Rotation;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author aesthetical, cattyn
 * @since 10/27/2021
 */
public class NoFallModule extends Module {
    public static NoFallModule INSTANCE;

    public NoFallModule() {
        super("NoFall", new String[] {"AntiFall", "NoFallDamage"},  Category.MOVEMENT, "Attempts to negate fall damage");
        INSTANCE = this;
    }

    // **************************** general ****************************

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.PACKET)
            .setDescription("How to negate fall damage");

    public static Setting<Double> distance = new Setting<>("Distance", 1.0, 2.0, 5.0, 1)
            .setDescription("The minimum fall distance before attempting to prevent fall damage");

    public static Setting<Switch> autoSwitch = new Setting<>("Switch", Switch.NORMAL)
            .setAlias("AutoSwitch", "Swap", "AutoSwap")
            .setDescription("Mode to use when switching to a water bucket")
            .setVisible(() -> mode.getValue().equals(Mode.WATER));

    // **************************** speeds ****************************

    public static Setting<Double> glideSpeed = new Setting<>("GlideSpeed", 0.1, 1.5, 5.0, 1)
            .setDescription("The factor to slow down fall speed")
            .setVisible(() -> mode.getValue().equals(Mode.GLIDE));

    // **************************** anticheat ****************************

    public static Setting<Boolean> factorize = new Setting<>("Factorize", false)
            .setDescription("Spoof fall distance")
            .setVisible(() -> mode.getValue().equals(Mode.RUBBERBAND));

    @Override
    public void onUpdate() {

        // make sure our fall distance is past our minimum distance
        if (shouldNegateFallDamage() && !mc.player.isOverWater()) {

            // attempt to negate fall damage
            switch (mode.getValue()) {
                case GLIDE:
                    // attempt to fall slower
                    mc.player.motionY /= glideSpeed.getValue();
                    break;
                case WATER:
                    // save our previous slot
                    int previousSlot = mc.player.inventory.currentItem;

                    // switch to water bucket
                    getCosmos().getInventoryManager().switchToItem(Items.WATER_BUCKET, autoSwitch.getValue());

                    // attempt to rotate and place water to cancel fall damage
                    getCosmos().getRotationManager().setRotation(new Rotation(mc.player.rotationYaw, 90F));
                    mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);

                    // switchback to previous slot
                    if (previousSlot != -1) {
                        getCosmos().getInventoryManager().switchToSlot(previousSlot, autoSwitch.getValue());
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

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {

        // if we are sending a movement packet and we are falling
        if (event.getPacket() instanceof CPacketPlayer && shouldNegateFallDamage()) {

            // we also only want to spoof our packet if we are using modes PACKET or GLIDE
            if (mode.getValue().equals(Mode.PACKET) || mode.getValue().equals(Mode.GLIDE)) {

                // spoof our onGround state
                ((ICPacketPlayer) event.getPacket()).setOnGround(true);
            }
        }
    }

    /**
     * Checks if we should start to negate fall damage
     * @return if the fall distance is greater/equal to distance
     */
    private boolean shouldNegateFallDamage() {
        return mc.player.fallDistance >= distance.getValue();
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
