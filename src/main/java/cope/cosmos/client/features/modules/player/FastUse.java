package cope.cosmos.client.features.modules.player;

import cope.cosmos.asm.mixins.accessor.IMinecraft;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.utility.player.InventoryUtil;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@SuppressWarnings("unused")
public class FastUse extends Module {
    public static FastUse INSTANCE;

    public FastUse() {
        super("FastUse", Category.PLAYER, "Allows you to place items and blocks faster");
        INSTANCE = this;
    }

    public static Setting<Double> speed = new Setting<>("Speed", "Place speed", 0.0, 4.0, 4.0, 0);

    public static Setting<Boolean> ghostFix = new Setting<>("GhostFix", "Fixes the item ghost issue on some servers", false);
    public static Setting<Boolean> fastDrop = new Setting<>("FastDrop", "Drops items faster", false);

    public static Setting<Boolean> packetUse = new Setting<>("PacketUse", "Uses packets when using items", false);
    public static Setting<Boolean> packetGapple = new Setting<>("Gapple", "Uses packets when eating gapples", false).setParent(packetUse);
    public static Setting<Boolean> packetPotion = new Setting<>("Potions", "Uses packets when drinking potions", true).setParent(packetUse);

    public static Setting<Boolean> exp = new Setting<>("EXP", "Applies fast placements to experience", true);
    public static Setting<Boolean> bow = new Setting<>("Bow", "Applies fast placements to bows", false);
    public static Setting<Boolean> crystals = new Setting<>("Crystals", "Applies fast placements to crystals", false);
    public static Setting<Boolean> blocks = new Setting<>("Blocks", "Applies fast placements to blocks", false);
    public static Setting<Boolean> spawnEggs = new Setting<>("SpawnEggs", "Applies fast placements to spawn eggs", false);
    public static Setting<Boolean> fireworks = new Setting<>("Fireworks", "Applies fast placements to fireworks", false);

    @Override
    public void onUpdate() {
        if (InventoryUtil.isHolding(Items.EXPERIENCE_BOTTLE) && exp.getValue() || InventoryUtil.isHolding(Items.END_CRYSTAL) && crystals.getValue() || InventoryUtil.isHolding(Items.SPAWN_EGG) && spawnEggs.getValue() || InventoryUtil.isHolding(Items.FIREWORKS) && fireworks.getValue() || InventoryUtil.isHolding(Item.getItemFromBlock(Blocks.OBSIDIAN)) && blocks.getValue()) {
            if (ghostFix.getValue() && mc.gameSettings.keyBindUseItem.isKeyDown()) {
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
            }

            else {
                ((IMinecraft) mc).setRightClickDelayTimer(4 - speed.getValue().intValue());
            }
        }

        if (fastDrop.getValue() && mc.gameSettings.keyBindDrop.isKeyDown()) {
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
        }

        if (InventoryUtil.isHolding(Items.BOW) && bow.getValue() && mc.player.isHandActive() && mc.player.getItemInUseMaxCount() >= speed.getValue() - 1) {
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(mc.player.getActiveHand()));
            mc.player.stopActiveHand();
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
            if (ghostFix.getValue() && InventoryUtil.isHolding(Items.EXPERIENCE_BOTTLE)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerRightClick(PlayerInteractEvent.RightClickItem event) {
        if (packetUse.getValue() && event.getEntityPlayer().equals(mc.player)) {
            if ((packetGapple.getValue() && event.getItemStack().getItem().equals(Items.GOLDEN_APPLE)) || (packetPotion.getValue() && event.getItemStack().getItem().equals(Items.POTIONITEM))) {
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
