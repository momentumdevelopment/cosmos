package cope.cosmos.util.player;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.manager.managers.InventoryManager;
import cope.cosmos.util.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Arrays;

/**
 * @author linustouchtips
 * @since 05/06/2021
 */
public class InventoryUtil implements Wrapper {

    /**
     * Checks if the player is holding a specified item
     * @param item The specified item
     * @return Whether the player is holding the specified item
     */
    public static boolean isHolding(Item item) {
        return mc.player.getHeldItemMainhand().getItem().equals(item) || mc.player.getHeldItemOffhand().getItem().equals(item);
    }

    /**
     * Checks if the player is holding a specified block
     * @param block The specified block
     * @return Whether the player is holding the specified block
     */
    public static boolean isHolding(Block block) {
        return mc.player.getHeldItemMainhand().getItem().equals(Item.getItemFromBlock(block)) || mc.player.getHeldItemOffhand().getItem().equals(Item.getItemFromBlock(block));
    }

    /**
     * Checks if the player is holding any item in a specified list of items
     * @param items The specified item list
     * @return Whether the player is holding any item in a specified list of items
     */
    public static boolean isHolding(Item[] items) {
        return Arrays.stream(items).anyMatch(inventoryItem -> inventoryItem.equals(mc.player.getHeldItemMainhand().getItem())) || Arrays.stream(items).anyMatch(inventoryItem -> inventoryItem.equals(mc.player.getHeldItemOffhand().getItem()));
    }

    /**
     * Checks if the player is holding any block in a specified list of blocks
     * @param blocks The specified block list
     * @return Whether the player is holding any block in a specified list of blocks
     */
    public static boolean isHolding(Block[] blocks) {
        return Arrays.stream(blocks).anyMatch(inventoryItem -> Item.getItemFromBlock(inventoryItem).equals(mc.player.getHeldItemMainhand().getItem())) || Arrays.stream(blocks).anyMatch(inventoryItem -> Item.getItemFromBlock(inventoryItem).equals(mc.player.getHeldItemOffhand().getItem()));
    }

    /**
     * Checks if the player is holding a specified item
     * @param clazz The specified item
     * @return Whether the player is holding the specified item
     */
    public static boolean isHolding(Class<? extends Item> clazz) {
        return clazz.isInstance(mc.player.getHeldItemMainhand().getItem()) || clazz.isInstance(mc.player.getHeldItemOffhand().getItem());
    }

    /**
     * Checks if a given item is in the player's hotbar
     * @param in The item to search
     * @return Whether the given item is in the player's hotbar
     */
    public static boolean isInHotbar(Item in) {
        return Cosmos.INSTANCE.getInventoryManager().searchSlot(in, InventoryManager.InventoryRegion.HOTBAR) != -1;
    }

    /**
     * Gets the highest enchantment level of the current held item
     * @return The highest enchantment level of the current held item
     */
    public static int getHighestEnchantLevel() {
        // highest enchantment lvl
        int highestLvl = 0;

        // check each enchantment lvl
        for (int i = 0; i < mc.player.getHeldItemMainhand().getEnchantmentTagList().tagCount(); i++) {
            // the enchantment
            NBTTagCompound enchantment = mc.player.getHeldItemMainhand().getEnchantmentTagList().getCompoundTagAt(i);

            if (enchantment.getShort("lvl") > highestLvl) {
                highestLvl = enchantment.getShort("lvl");
            }
        }

        // 32k -> lvl greater than 1000
        return highestLvl;
    }

    /**
     * Gets the amount of the item the player has in their inventory
     * @param item The item to count
     * @return The amount of the item the player has in their inventory
     */
    public static int getItemCount(Item item) {
        // armor inventory
        if (item instanceof ItemArmor) {
            return mc.player.inventory.armorInventory.stream()
                    .filter(itemStack -> itemStack.getItem().equals(item))
                    .mapToInt(ItemStack::getCount)
                    .sum();
        }

        // main inventory
        else {
            return mc.player.inventory.mainInventory.stream()
                    .filter(itemStack -> itemStack.getItem().equals(item))
                    .mapToInt(ItemStack::getCount)
                    .sum()

                    + mc.player.inventory.offHandInventory.stream()
                    .filter(itemStack -> itemStack.getItem().equals(item))
                    .mapToInt(ItemStack::getCount)
                    .sum();
        }
    }
}
