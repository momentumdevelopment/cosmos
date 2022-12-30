package cope.cosmos.client.features.modules.movement;

import cope.cosmos.client.events.entity.player.interact.RightClickItemEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.exploits.PacketFlightModule;
import cope.cosmos.client.features.modules.visual.FreecamModule;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.MotionUtil;
import cope.cosmos.util.player.PlayerUtil;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author aesthetical, linustouchtips
 * @since 09/06/2022
 */
public class TickShiftModule extends Module {
    public static TickShiftModule INSTANCE;

    public TickShiftModule() {
        super("TickShift", Category.MOVEMENT, "Speeds up ticks", () -> String.valueOf(packets));
        INSTANCE = this;
    }

    // **************************** general ****************************

    public static Setting<Double> ticks = new Setting<>("Ticks", 1.0, 20.0, 120.0, 0)
            .setDescription("Boost ticks");

    public static Setting<Boolean> motion = new Setting<>("Motion", true)
            .setDescription("Speeds up motion");

    public static Setting<Double> speed = new Setting<>("Speed", 0.5, 1.0, 5.0, 1)
            .setDescription("Boost speed")
            .setVisible(() -> motion.getValue());

    public static Setting<Boolean> items = new Setting<>("Items", false)
            .setDescription("Speeds up item use");

    // max packets we can send
    private static int packets;

    @Override
    public void onUpdate() {

        // incompatibilities
        if (PlayerUtil.isFlying() || PacketFlightModule.INSTANCE.isEnabled() || FreecamModule.INSTANCE.isEnabled()) {
            getCosmos().getTickManager().setClientTicks(1);
            return;
        }

        // check if player is moving
        if (MotionUtil.isMoving() || !mc.player.onGround) {

            // reduce allowed packets
            packets--;

            // clamp packets
            if (packets <= 0) {
                packets = 0;

                // reset timer
                if (motion.getValue()) {
                    getCosmos().getTickManager().setClientTicks(1);
                }
            }

            // increase timer
            else if (motion.getValue()) {

                // check if we are max allowed packets per boost
                if (packets >= ticks.getValue() - 1) {

                    // update timer
                    getCosmos().getTickManager().setClientTicks(1 + speed.getValue().floatValue());
                }
            }
        }

        else {

            // increase allowed packets
            packets++;

            // clamp
            if (packets > ticks.getValue()) {
                packets = ticks.getValue().intValue();
            }
        }
    }

    @SubscribeEvent
    public void onRightClickItem(RightClickItemEvent event) {

        // speed up item use
        if (items.getValue()) {

            // only potions for now, may add a whitelist in the future
            if (event.getItemStack().getItem().equals(Items.POTIONITEM)) {

                // ticks needed to use the item
                int use = event.getItemStack().getMaxItemUseDuration();

                // check if we have more stored packets than needed for the item use
                if (packets > use) {

                    // send packets to finish using the item
                    for (int i = 0; i < use; i++) {

                        // send player packet, this tells the server to move forward one tick
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, mc.player.onGround));
                    }

                    // cancel eating animation and skip to the item finish state
                    event.setCanceled(true);
                    event.getItemStack().getItem().onItemUseFinish(event.getItemStack(), mc.world, mc.player);

                    // reduce stored packets
                    packets -= use;
                }
            }
        }
    }
}