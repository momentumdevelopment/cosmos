package cope.cosmos.client.features.modules.combat;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.math.Timer;
import cope.cosmos.util.math.Timer.Format;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author aesthetical
 * @since 01/05/2022
 */
public class ReplenishModule extends Module {
    public static ReplenishModule INSTANCE;

    public ReplenishModule() {
        super("Replenish", new String[] {"HotbarRefill", "Refill"}, Category.COMBAT, "Replaces items in your hotbar with items from your inventory");
        INSTANCE = this;
    }

    // **************************** general ****************************

    public static Setting<Double> percent = new Setting<>("Percent", 1.0, 70.0, 99.0, 1)
            .setAlias("Scale")
            .setDescription("The percentage of the item stack size from 100% before replacing");

    public static Setting<Double> delay = new Setting<>("Delay", 0.0, 100.0, 1000.0, 1)
            .setDescription("The delay in ms before replenishing");

    public static Setting<Boolean> multitask = new Setting<>("Multitask", true)
            .setAlias("Wait")
            .setDescription("Allow replenish while combat modules are active");

    // our cached hotbar
    private final Map<Integer, ItemStack> hotbar = new ConcurrentHashMap<>();

    // waiting for the next inventory interactions
    private final Timer timer = new Timer();

    // our refill priority before the timer has passed
    private int refillSlot = -1;

    @Override
    public void onDisable() {
        super.onDisable();

        // reset values
        hotbar.clear();
        refillSlot = -1;
    }

    @Override
    public void onTick() {

        // if we have not queued a slot to refill next timer reset
        if (refillSlot == -1) {

            // go through the entire hotbar
            for (int i = 0; i < 9; i++) {

                // get the stack from our hotbar cache
                ItemStack stack = mc.player.inventory.getStackInSlot(i);

                // if it was not found in the cache, we have to set it in the cache
                if (hotbar.getOrDefault(i, null) == null) {

                    // ignore air items
                    if (stack.getItem().equals(Items.AIR)) {
                        continue;
                    }

                    hotbar.put(i, stack);
                    continue;
                }

                // so, getCount() and getMaxStackSize() return ints, so we need to convert them to a double
                // because 45 / 64 = 0.703125, but converting to an int will be a whole number, so that's why the casts are needed
                // to then make it out of 100, we need to multiply by 100. the above example would give us 70.0
                double percentage = ((double) stack.getCount() / (double) stack.getMaxStackSize()) * 100.0;

                // if the percentage of the item counts is less than the minimum required percent, we can refill the slot
                if (percentage <= percent.getValue()) {

                    // if we are crystaling and the wait setting is on, wait for AC to be turned off
                    if (stack.getItem().equals(Items.END_CRYSTAL) && (!multitask.getValue() && AutoCrystalModule.INSTANCE.isActive())) {
                        continue;
                    }

                    // if we still have time to wait, queue the slot and stop
                    if (!timer.passedTime(delay.getValue().longValue(), Format.MILLISECONDS)) {

                        // cache the slot we'd like to do after this timer has passed the time
                        refillSlot = i;
                    }

                    else {

                        // replenish the slot if the timer has passed, and reset
                        fillStack(i, stack);

                        // reset timer
                        timer.resetTime();
                    }

                    break;
                }
            }
        }

        else {

            // if the time has passed and we have a queued slot, we can handle that here.
            if (timer.passedTime(delay.getValue().longValue(), Format.MILLISECONDS)) {

                // replenish
                fillStack(refillSlot, hotbar.get(refillSlot));

                // reset values
                timer.resetTime();
                refillSlot = -1;
            }
        }
    }

    /**
     * Replenishes the hotbar slot specified
     * @param slot The hotbar slot
     * @param stack The cached item stack to compare to the ones in the inventory
     */
    private void fillStack(int slot, ItemStack stack) {

        // if the slot is null (-1) or the stack is null or the stack cannot be stacked, don't bother handling
        if (slot != -1 && stack != null && stack.isStackable()) {

            // find the inventory slot we're gonna use to merge with our hotbar slot
            int replenishSlot = -1;

            // loop through our inventory, after the hotbar
            for (int i = 9; i < 36; i++) {

                // item
                ItemStack itemStack = mc.player.inventory.getStackInSlot(i);

                // if the slot found in the inventory isn't empty
                if (!itemStack.isEmpty()) {

                    // we cannot merge stacks if they do not have the same name
                    // for example, the two stacks can be Wooden Planks, but if one is named "Fuck" and the other
                    // one is not, they cannot merge together regardless of the block type
                    if (!stack.getDisplayName().equals(itemStack.getDisplayName())) {
                        continue;
                    }

                    // if the stack from the hotbar is a block
                    if (stack.getItem() instanceof ItemBlock) {

                        // if the stack in the inventory is not a block
                        if (!(itemStack.getItem() instanceof ItemBlock)) {
                            continue;
                        }

                        // get both block item instances
                        ItemBlock hotbarBlock = (ItemBlock) stack.getItem();
                        ItemBlock inventoryBlock = (ItemBlock) itemStack.getItem();

                        // if they are not the same block, move on
                        if (!hotbarBlock.getBlock().equals(inventoryBlock.getBlock())) {
                            continue;
                        }
                    }

                    else {

                        // if the hotbar item is not a ItemBlock and the items don't match, move on
                        if (!stack.getItem().equals(itemStack.getItem())) {
                            continue;
                        }
                    }

                    // we have found the slot we'll use to replenish our items with
                    replenishSlot = i;
                }
            }

            // if none was found (as our default is -1), don't handle
            if (replenishSlot != -1) {

                // cache the total amount of items combined in each stack
                // this needs to be cached because the changes happen upon the windowClick calls
                int total = stack.getCount() + mc.player.inventory.getStackInSlot(replenishSlot).getCount();

                // click the slot we found, and merge into our hotbar slot
                mc.playerController.windowClick(0, replenishSlot, 0, ClickType.PICKUP, mc.player);

                // since 9 will usually be greater than our hotbar slot id, we'll add 36 for the packet id
                mc.playerController.windowClick(0, slot < 9 ? slot + 36 : slot, 0, ClickType.PICKUP, mc.player);

                // lets say that one stack has a total of 50 items in the stack, and the one we're replenishing has 10 items
                // 50 + 10 = 60, and lets say the max size of the stack is 64. the 50 stack will be completely gone, meaning we have nothing floating around
                // however, if that 50 was 64, we have a remainder of items left (10 + 64 = 74, leaving 10 left in the stack), which will leave it floating in the inventory
                // so if the above is true, we'll replace it back into its original slot.
                if (total >= stack.getMaxStackSize()) {
                    mc.playerController.windowClick(0, replenishSlot, 0, ClickType.PICKUP, mc.player);
                }

                // reset refill slot
                refillSlot = -1;
            }
        }
    }
}
