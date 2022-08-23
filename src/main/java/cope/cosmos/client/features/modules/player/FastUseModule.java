package cope.cosmos.client.features.modules.player;

import cope.cosmos.asm.mixins.accessor.IMinecraft;
import cope.cosmos.client.events.entity.player.interact.RightClickItemEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.world.ShiftBlocks;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 06/08/2021
 */
public class FastUseModule extends Module {
    public static FastUseModule INSTANCE;

    public FastUseModule() {
        super("FastUse", Category.PLAYER, "Allows you to place items and blocks faster");
        INSTANCE = this;
    }

    // **************************** speeds ****************************

    public static Setting<Double> speed = new Setting<>("Speed", 0.0, 4.0, 4.0, 0)
            .setDescription("Place speed");

    // **************************** anticheat ****************************

    public static Setting<Boolean> ghostFix = new Setting<>("GhostFix", false)
            .setDescription("Fixes the item ghost issue on some servers");

    // **************************** general ****************************

    public static Setting<Boolean> fastDrop = new Setting<>("FastDrop", false)
            .setDescription("Drops items faster");

    // **************************** packet use ****************************

    public static Setting<Boolean> packetUse = new Setting<>("PacketUse", false)
            .setDescription("Uses packets when using items");

    public static Setting<Boolean> packetFood = new Setting<>("PacketFood", false)
            .setDescription("Uses packets when eating food")
            .setVisible(() -> packetUse.getValue());

    public static Setting<Boolean> packetPotion = new Setting<>("PacketPotions", true)
            .setDescription("Uses packets when drinking potions")
            .setVisible(() -> packetUse.getValue());

    // **************************** items ****************************

    public static Setting<Boolean> exp = new Setting<>("EXP", true)
            .setDescription("Applies fast placements to experience");

    public static Setting<Boolean> crystals = new Setting<>("Crystals", false)
            .setDescription("Applies fast placements to crystals");

    public static Setting<Boolean> blocks = new Setting<>("Blocks", false)
            .setDescription("Applies fast placements to blocks");

    public static Setting<Boolean> spawnEggs = new Setting<>("SpawnEggs", false)
            .setDescription("Applies fast placements to spawn eggs");

    public static Setting<Boolean> fireworks = new Setting<>("Fireworks", false)
            .setDescription("Applies fast placements to fireworks");

    @Override
    public void onUpdate() {

        // make sure we're holding a valid item
        if (isHoldingValidItem()) {

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

        if (fastDrop.getValue()) {

            // spam the drop item packet
            if (mc.gameSettings.keyBindDrop.isKeyDown()) {
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {

            // cancel place on block packets
            if (ghostFix.getValue()) {
                if (isHoldingValidItem()) {

                    // interacting block
                    Block interactBlock = mc.world.getBlockState(((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getPos()).getBlock();

                    // make sure we are not interacting with a sneak block
                    if (!ShiftBlocks.contains(interactBlock)) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerRightClick(RightClickItemEvent event) {
        if (packetUse.getValue()) {

            // make sure we are holding eatable/drinkable items
            if (packetFood.getValue() && event.getItemStack().getItem() instanceof ItemFood || packetPotion.getValue() && event.getItemStack().getItem().equals(Items.POTIONITEM)) {

                // cancel eating animation and skip to the item finish state
                event.setCanceled(true);
                event.getItemStack().getItem().onItemUseFinish(event.getItemStack(), mc.world, mc.player);

                // skip ticks lolololol
                for (int i = 0; i < event.getItemStack().getMaxItemUseDuration(); i++) {
                    mc.player.connection.sendPacket(new CPacketPlayer());
                }
            }
        }
    }

    /**
     * Checks if the player is holding an item that needs fast usage
     * @return Whether the player is holding an item that needs fast usage
     */
    public boolean isHoldingValidItem() {
        return InventoryUtil.isHolding(Items.EXPERIENCE_BOTTLE) && exp.getValue() || InventoryUtil.isHolding(Items.END_CRYSTAL) && crystals.getValue() || InventoryUtil.isHolding(Items.SPAWN_EGG) && spawnEggs.getValue() || InventoryUtil.isHolding(Items.FIREWORKS) && fireworks.getValue() || InventoryUtil.isHolding(ItemBlock.class) && blocks.getValue();
    }

    @Override
    public boolean isActive() {
        return isEnabled() && isHoldingValidItem() && mc.player.isHandActive();
    }
}
