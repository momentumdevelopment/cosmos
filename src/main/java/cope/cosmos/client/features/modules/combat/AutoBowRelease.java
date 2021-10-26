package cope.cosmos.client.features.modules.combat;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.system.Timer;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumHand;

public class AutoBowRelease extends Module {
    public static final Setting<Integer> ticks = new Setting<>("Ticks", "The maximum item use count before automatically releasing", 0, 4, 20, 1);
    public static final Setting<Integer> delay = new Setting<>("Delay", "The delay in ticks before trying to release your bow again", 0, 1, 15, 1);
    public static final Setting<Boolean> packet = new Setting<>("Packet", "If to send a stop item use packet", true);
    public static final Setting<Boolean> offhand = new Setting<>("Offhand", "If to automatically release your bow if it is in your offhand", true);

    private final Timer timer = new Timer();

    public AutoBowRelease() {
        super("AutoBowRelease", Category.COMBAT, "Automatically releases your bow for you");
    }

    @Override
    public void onUpdate() {
        if (mc.player.isHandActive() && mc.player.getActiveItemStack().getItem() == Items.BOW) {
            if (!offhand.getValue() && mc.player.getActiveHand() == EnumHand.OFF_HAND) {
                return;
            }

            if (mc.player.getItemInUseMaxCount() >= ticks.getValue() && this.timer.passed(delay.getValue().longValue(), Timer.Format.TICKS)) {
                this.timer.reset();

                if (packet.getValue()) {
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, mc.player.getPosition(), mc.player.getHorizontalFacing()));
                }

                mc.player.stopActiveHand();
            }
        }
    }
}
