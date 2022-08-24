package cope.cosmos.client.features.modules.combat;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.InventoryUtil;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

/**
 * @author linustouchtips
 * @since 08/22/2022
 */
public class AutoBowReleaseModule extends Module {
    public static AutoBowReleaseModule INSTANCE;

    public AutoBowReleaseModule() {
        super("AutoBowRelease", new String[] {"FastBow", "BowRelease", "BowSpam"}, Category.COMBAT, "Automatically releases a drawn bow");
        INSTANCE = this;
    }

    // **************************** general ****************************

    public static Setting<Double> ticks = new Setting<>("Ticks", 3.0, 3.0, 20.0, 0)
            .setDescription("Ticks to draw the bow");

    @Override
    public void onTick() {

        // make sure we are holding a bow and drawing it
        if (InventoryUtil.isHolding(Items.BOW) && mc.player.isHandActive()) {

            // make sure we've held it for at least a minimum of specified ticks
            if (mc.player.getItemInUseMaxCount() > ticks.getValue()) {

                // release bow packets
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                // mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(mc.player.getActiveHand()));
                mc.player.stopActiveHand();
            }
        }
    }
}
