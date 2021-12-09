package cope.cosmos.client.features.modules.player;

import cope.cosmos.asm.mixins.accessor.IMinecraft;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.event.annotation.Subscription;
import cope.cosmos.util.player.InventoryUtil;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 06/08/2021
 */
@SuppressWarnings("unused")
public class FastUse extends Module {
    public static FastUse INSTANCE;

    public FastUse() {
        super("FastUse", Category.PLAYER, "Allows you to place items and blocks faster");
        INSTANCE = this;
    }

    // delay for using items
    public static Setting<Double> speed = new Setting<>("Speed", 0.0, 4.0, 4.0, 0).setDescription("Place speed");

    // anti-cheat
    public static Setting<Boolean> ghostFix = new Setting<>("GhostFix", false).setDescription("Fixes the item ghost issue on some servers");
    public static Setting<Boolean> fastDrop = new Setting<>("FastDrop", false).setDescription("Drops items faster");

    // packet use
    public static Setting<Boolean> packetUse = new Setting<>("PacketUse", false).setDescription("Uses packets when using items");
    public static Setting<Boolean> packetFood = new Setting<>("Food", false).setParent(packetUse).setDescription("Uses packets when eating food");
    public static Setting<Boolean> packetPotion = new Setting<>("Potions", true).setParent(packetUse).setDescription("Uses packets when drinking potions");

    // valid items
    public static Setting<Boolean> exp = new Setting<>("EXP", true).setDescription("Applies fast placements to experience");
    public static Setting<Boolean> bow = new Setting<>("Bow", false).setDescription("Applies fast placements to bows");
    public static Setting<Boolean> crystals = new Setting<>("Crystals", false).setDescription("Applies fast placements to crystals");
    public static Setting<Boolean> blocks = new Setting<>("Blocks", false).setDescription("Applies fast placements to blocks");
    public static Setting<Boolean> spawnEggs = new Setting<>("SpawnEggs", false).setDescription("Applies fast placements to spawn eggs");
    public static Setting<Boolean> fireworks = new Setting<>("Fireworks", false).setDescription("Applies fast placements to fireworks");

    @Override
    public void onUpdate() {
        // make sure we're holding a valid item
        if (InventoryUtil.isHolding(Items.EXPERIENCE_BOTTLE) && exp.getValue() || InventoryUtil.isHolding(Items.END_CRYSTAL) && crystals.getValue() || InventoryUtil.isHolding(Items.SPAWN_EGG) && spawnEggs.getValue() || InventoryUtil.isHolding(Items.FIREWORKS) && fireworks.getValue() || InventoryUtil.isHolding(Item.getItemFromBlock(Blocks.OBSIDIAN)) && blocks.getValue()) {
            if (ghostFix.getValue() && mc.gameSettings.keyBindUseItem.isKeyDown()) {
                // spam the use packet
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
            }

            else {
                // set our vanilla right click delay timer to 0
                ((IMinecraft) mc).setRightClickDelayTimer(4 - speed.getValue().intValue());
            }
        }

        // spam the drop item packet
        if (fastDrop.getValue() && mc.gameSettings.keyBindDrop.isKeyDown()) {
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
        }

        // fast bow, make sure we are holding a bow and shooting it
        if (InventoryUtil.isHolding(Items.BOW) && bow.getValue() && mc.player.isHandActive()) {

            // make sure we've held it for at least a minimum of 1 tick
            if (mc.player.getItemInUseMaxCount() >= speed.getValue() - 1) {

                // spam release bow packets
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(mc.player.getActiveHand()));
                mc.player.stopActiveHand();
            }
        }
    }

    @Subscription
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {

            // cancel place on block packets
            if (ghostFix.getValue() && InventoryUtil.isHolding(Items.EXPERIENCE_BOTTLE)) {
                event.setCanceled(true);
            }
        }
    }

    @Subscription
    public void onPlayerRightClick(PlayerInteractEvent.RightClickItem event) {
        if (packetUse.getValue() && event.getEntityPlayer().equals(mc.player)) {

            // make sure we are holding eatable/drinkable items
            if (packetFood.getValue() && event.getItemStack().getItem() instanceof ItemFood || packetPotion.getValue() && event.getItemStack().getItem().equals(Items.POTIONITEM)) {

                // cancel eating animation and skip to the item finish state
                event.setCanceled(true);
                event.getItemStack().getItem().onItemUseFinish(event.getItemStack(), event.getWorld(), event.getEntityPlayer());

                // skip ticks lolololol
                for (int i = 0; i < ((4 - speed.getValue()) * 8); i++) {
                    mc.player.connection.sendPacket(new CPacketPlayer());
                }
            }
        }
    }

    @Override
    public boolean isActive() {
        return isEnabled() && mc.player.isHandActive();
    }
}
