package cope.cosmos.util.player;

import cope.cosmos.util.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Objects;

public class InventoryUtil implements Wrapper {

    public static boolean isHolding(Item item) {
        return mc.player.getHeldItemMainhand().getItem().equals(item) || mc.player.getHeldItemOffhand().getItem().equals(item);
    }

    public static boolean isHolding(Block block) {
        return mc.player.getHeldItemMainhand().getItem().equals(Item.getItemFromBlock(block)) || mc.player.getHeldItemOffhand().getItem().equals(Item.getItemFromBlock(block));
    }

    public static boolean isHolding(Class<? extends Item> clazz) {
        return clazz.isInstance(mc.player.getHeldItemMainhand().getItem()) || clazz.isInstance(mc.player.getHeldItemOffhand().getItem());
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

    public static int getItemCount(Item item) {
        return new ArrayList<>(mc.player.inventory.mainInventory).stream()
                .filter(itemStack -> itemStack.getItem().equals(item))
                .mapToInt(ItemStack::getCount)
                .sum()

                + new ArrayList<>(mc.player.inventory.offHandInventory).stream()
                .filter(itemStack -> itemStack.getItem().equals(item))
                .mapToInt(ItemStack::getCount)
                .sum()

                + new ArrayList<>(mc.player.inventory.armorInventory).stream()
                .filter(itemStack -> itemStack.getItem().equals(item))
                .mapToInt(ItemStack::getCount)
                .sum();
    }

    public static int getBlockCount(Block block) {
        return new ArrayList<>(mc.player.inventory.mainInventory).stream()
                .filter(itemStack -> itemStack.getItem().equals(Item.getItemFromBlock(block)))
                .mapToInt(ItemStack::getCount)
                .sum()

                + new ArrayList<>(mc.player.inventory.offHandInventory).stream()
                .filter(itemStack -> itemStack.getItem().equals(Item.getItemFromBlock(block)))
                .mapToInt(ItemStack::getCount)
                .sum();
    }
}
