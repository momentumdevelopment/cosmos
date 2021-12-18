package cope.cosmos.util.player;

import cope.cosmos.asm.mixins.accessor.IPlayerControllerMP;
import cope.cosmos.util.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;

import java.util.ArrayList;
import java.util.Objects;

public class InventoryUtil implements Wrapper {

    public static boolean isHolding(Item item) {
        return mc.player.getHeldItemMainhand().getItem().equals(item) || mc.player.getHeldItemOffhand().getItem().equals(item);
    }

    public static boolean isHolding(Class<? extends Item> clazz) {
        return clazz.isInstance(mc.player.getHeldItemMainhand().getItem()) || clazz.isInstance(mc.player.getHeldItemOffhand().getItem());
    }

    public static void switchToSlot(int slot, Switch switchMode) {
        if (slot != -1 && mc.player.inventory.currentItem != slot) {
            switch (switchMode) {
                case NORMAL:
                    mc.player.inventory.currentItem = slot;
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
                    break;
                case PACKET:
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
                    break;
            }
        }

        mc.playerController.updateController();
        ((IPlayerControllerMP) mc.playerController).hookSyncCurrentPlayItem();
    }

    public static void switchToSlot(Item item, Switch switchMode) {
        if (getItemSlot(item, Inventory.HOTBAR) != -1 && mc.player.inventory.currentItem != getItemSlot(item, Inventory.HOTBAR)) {
            switchToSlot(getItemSlot(item, Inventory.HOTBAR), switchMode);
        }

        ((IPlayerControllerMP) mc.playerController).hookSyncCurrentPlayItem();
    }

    public static int getItemSlot(Item item, Inventory inventory) {
        switch (inventory) {
            case INVENTORY:
                for (int i = 9; i <= 44; i++) {
                    if (mc.player.inventory.getStackInSlot(i).getItem().equals(item)) {
                        return i;
                    }
                }
            case HOTBAR:
                for (int i = 0; i < 9; i++) {
                    if (mc.player.inventory.getStackInSlot(i).getItem().equals(item)) {
                        return i;
                    }
                }
        }

        return -1;
    }

    public static int getItemSlot(Class<? extends Item> clazz, Inventory inventory) {
        switch (inventory) {
            case INVENTORY:
                for (int i = 9; i <= 44; i++) {
                    if (clazz.isInstance(mc.player.inventory.getStackInSlot(i).getItem())) {
                        return i;
                    }
                }
            case HOTBAR:
                for (int i = 0; i < 9; i++) {
                    if (clazz.isInstance(mc.player.inventory.getStackInSlot(i).getItem())) {
                        return i;
                    }
                }
        }

        return -1;
    }

    public static int getBlockSlot(Block block, Inventory inventory) {
        switch (inventory) {
            case INVENTORY:
                for (int i = 0; i <= 44; i++) {
                    Item item = mc.player.inventory.getStackInSlot(i).getItem();

                    if (item instanceof ItemBlock && ((ItemBlock) item).getBlock().equals(block)) {
                        return i;
                    }
                }
            case HOTBAR:
                for (int i = 0; i <= 9; i++) {
                    Item item = mc.player.inventory.getStackInSlot(i).getItem();

                    if (item instanceof ItemBlock && ((ItemBlock) item).getBlock().equals(block)) {
                        return i;
                    }
                }
        }

        return -1;
    }

    public static int getItemCount(Item item) {
        return new ArrayList<>(mc.player.inventory.mainInventory).stream().filter(itemStack -> itemStack.getItem().equals(item)).mapToInt(ItemStack::getCount).sum() + new ArrayList<>(mc.player.inventory.offHandInventory).stream().filter(itemStack -> itemStack.getItem().equals(item)).mapToInt(ItemStack::getCount).sum() + new ArrayList<>(mc.player.inventory.armorInventory).stream().filter(itemStack -> itemStack.getItem().equals(item)).mapToInt(ItemStack::getCount).sum();
    }

    public static int getBlockCount(Block block) {
        return new ArrayList<>(mc.player.inventory.mainInventory).stream().filter(itemStack -> itemStack.getItem().equals(Item.getItemFromBlock(block))).mapToInt(ItemStack::getCount).sum() + new ArrayList<>(mc.player.inventory.offHandInventory).stream().filter(itemStack -> itemStack.getItem().equals(Item.getItemFromBlock(block))).mapToInt(ItemStack::getCount).sum();
    }

    public static boolean isHolding32k() {
        for (int i = 0; i < mc.player.getHeldItemMainhand().getEnchantmentTagList().tagCount(); i++) {
            mc.player.getHeldItemMainhand().getEnchantmentTagList().getCompoundTagAt(i);
            if (Enchantment.getEnchantmentByID(mc.player.getHeldItemMainhand().getEnchantmentTagList().getCompoundTagAt(i).getByte("id")) != null) {
                if (Enchantment.getEnchantmentByID(mc.player.getHeldItemMainhand().getEnchantmentTagList().getCompoundTagAt(i).getShort("id")) != null) {
                    if (Objects.requireNonNull(Enchantment.getEnchantmentByID(mc.player.getHeldItemMainhand().getEnchantmentTagList().getCompoundTagAt(i).getShort("id"))).isCurse()) {
                        continue;
                    }

                    if (mc.player.getHeldItemMainhand().getEnchantmentTagList().getCompoundTagAt(i).getShort("lvl") >= 1000) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public enum Switch {
        NORMAL, PACKET, NONE
    }

    @SuppressWarnings("unused")
    public enum Inventory {
        INVENTORY, HOTBAR, CRAFTING
    }
}
