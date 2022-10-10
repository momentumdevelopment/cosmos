package cope.cosmos.client.features.modules.world;

import cope.cosmos.asm.mixins.accessor.IMinecraft;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.world.SneakBlocks;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.List;

/**
 * @author linustouchtips
 * @since 06/08/2021
 */
public class FastUseModule extends Module {
    public static FastUseModule INSTANCE;

    public FastUseModule() {
        super("FastUse", new String[] {"FastPlace", "EXPFast", "FastXP", "QuickEXP"}, Category.WORLD, "Allows you to place items and blocks faster");
        INSTANCE = this;
    }

    // **************************** general ****************************

    public static Setting<Type> type = new Setting<>("Type", Type.WHITELIST)
            .setAlias("Mode")
            .setDescription("Valid items");

    public static Setting<List<Item>> whiteList = new Setting<>("WhiteList", Arrays.asList(
            Items.EXPERIENCE_BOTTLE,
            Items.SNOWBALL,
            Items.EGG
    ))
            .setDescription("Valid item whitelist");

    public static Setting<List<Item>> blackList = new Setting<>("BlackList", Arrays.asList(
            Items.ENDER_EYE,
            Items.ENDER_PEARL
    ))
            .setDescription("Item blacklist");

    public static Setting<Double> speed = new Setting<>("Speed", 0.0, 4.0, 4.0, 0)
            .setDescription("Place speed");

    // **************************** anticheat ****************************

    public static Setting<Boolean> ghostFix = new Setting<>("GhostFix", false)
            .setDescription("Fixes the item ghost issue on some servers");

    public static Setting<Boolean> fastDrop = new Setting<>("FastDrop", false)
            .setDescription("Drops items faster");


    @Override
    public void onUpdate() {

        // make sure we're holding a valid item
        if (isValid()) {

            // remove exp pickup cooldown
            mc.player.xpCooldown = 0;

            // fixes ghost items from being spawned
            if (ghostFix.getValue()) {

                // spam the use packet, NCP flags for CPacketPlayerTryUseItemOnBlock too fast so we can use CPacketPlayerTryUseItem instead
                if (mc.gameSettings.keyBindUseItem.isKeyDown()) {
                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
                }
            }

            else {

                // set our vanilla right click delay timer to 0
                ((IMinecraft) mc).setRightClickDelayTimer(speed.getMax().intValue() - speed.getValue().intValue());
            }
        }

        // drops items faster
        if (fastDrop.getValue()) {

            // spam the drop item packet
            if (mc.gameSettings.keyBindDrop.isKeyDown()) {
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
            }
        }
    }

    @Override
    public boolean isActive() {
        return isEnabled() && isValid() && mc.player.isHandActive();
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {

        // packet for block placements
        if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {

            // cancel place on block packets
            if (ghostFix.getValue()) {

                // check if the held item is valid
                if (isValid()) {

                    // interacting position
                    BlockPos position = ((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getPos();

                    // interacting block
                    Block interact = mc.world.getBlockState(position).getBlock();

                    // make sure we are not interacting with a sneak block
                    if (SneakBlocks.contains(interact) && !mc.player.isSneaking()) {
                        return;
                    }

                    event.setCanceled(true);
                }
            }
        }
    }

    /**
     * Checks if the player is holding an item that needs fast usage
     * @return Whether the player is holding an item that needs fast usage
     */
    public boolean isValid() {

        // holding item
        Item item = mc.player.getHeldItemMainhand().getItem();

        // check if item is in the whitelist
        if (type.getValue().equals(Type.WHITELIST)) {
            return whiteList.getValue().contains(item);
        }

        // check if item is not in the blacklist
        else if (type.getValue().equals(Type.BLACKLIST)) {
            return !blackList.getValue().contains(item);
        }

        // all items
        return true;
    }

    public enum Type {

        /**
         * Only uses whitelist items
         */
        WHITELIST,


        /**
         * Only uses items not in the blacklist
         */
        BLACKLIST,

        /**
         * Uses all items
         */
        ALL
    }
}
