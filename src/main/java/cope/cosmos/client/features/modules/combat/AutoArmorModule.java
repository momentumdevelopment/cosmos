package cope.cosmos.client.features.modules.combat;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.math.Timer;
import cope.cosmos.util.math.Timer.Format;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.network.play.client.CPacketEntityAction;

/**
 * @author aesthetical, linustouchtips
 * @since 1/05/2022
 */
public class AutoArmorModule extends Module {
    public static AutoArmorModule INSTANCE;

    public AutoArmorModule() {
        super("AutoArmor", Category.COMBAT, "Automatically equips the best piece of armor");
        INSTANCE = this;
    }

    // **************************** anticheat ****************************

    public static Setting<Boolean> fast = new Setting<>("Fast", false)
            .setDescription("Uses quick switching to switch the armor pieces");

    public static Setting<Boolean> inventoryStrict = new Setting<>("InventoryStrict", false)
            .setDescription("Opens inventory serverside before switching");

    // **************************** general ****************************

    // Is this necessary??
    public static Setting<Double> delay = new Setting<>("Delay", 0.0, 200.0, 1000.0, 1)
            .setDescription("The delay before equipping another piece of armor");

    public static Setting<Priority> priority = new Setting<>("Priority", Priority.BLAST)
            .setDescription("Armor type priority");

    public static Setting<Boolean> noBinding = new Setting<>("NoBinding", true)
            .setAlias("AntiCurse", "NoCurse")
            .setDescription("If to avoid equipping armor with the enchantment Curse of Binding");

    public static Setting<Boolean> inventory = new Setting<>("Inventory", false)
            .setAlias("InventoryOnly")
            .setDescription("Allow module to equip armor if the inventory GUI is open");

    // have an array of the best slots
    // remember: -1 = no slot found
    private ArmorSlot[] bestSlots = {
            new ArmorSlot(-1, false),
            new ArmorSlot(-1, false),
            new ArmorSlot(-1, false),
            new ArmorSlot(-1, false)
    };

    // a timer for waiting to do the next action
    private final Timer armorTimer = new Timer();

    @Override
    public void onDisable() {
        super.onDisable();

        // reset our best slots to all -1 (the default)
        bestSlots = new ArmorSlot[] {
                new ArmorSlot(-1, false),
                new ArmorSlot(-1, false),
                new ArmorSlot(-1, false),
                new ArmorSlot(-1, false)
        };
    }

    @Override
    public void onTick() {
        if (mc.currentScreen == null || mc.currentScreen instanceof GuiInventory && inventory.getValue()) {

            // loop through our entire inventory
            for (int i = 0; i < 36; i++) {

                // get the slot from our inventory
                ItemStack stack = mc.player.inventory.getStackInSlot(i);

                // if the stack is not an armor stack, we can move to the next slot
                if (stack.isEmpty() || !(stack.getItem() instanceof ItemArmor)) {
                    continue;
                }

                ItemArmor itemArmor = (ItemArmor) stack.getItem();

                // if the stack has a binding curse and we don't want to equip binding armor, move to the next
                if (noBinding.getValue() && EnchantmentHelper.hasBindingCurse(stack)) {
                    continue;
                }

                // get our armor type index (0 - 3)
                int index = itemArmor.armorType.getIndex();

                // check our inventory armor slot
                Item armorSlotItem = mc.player.inventory.armorInventory.get(index).getItem();

                // pause if elytra is in chest armor
                if (armorSlotItem.equals(Items.ELYTRA)) {
                    continue;
                }

                // get the damageReduceAmount, higher = better armor.
                // the above isn't always the deciding factor, if i ever decide i want to add priority, we'll have to do more checks
                int damageReduceAmount = armorSlotItem instanceof ItemArmor ? ((ItemArmor) armorSlotItem).damageReduceAmount : -1;

                // priority of current armor piece
                boolean armorPriority = EnchantmentHelper.getEnchantments(stack).containsKey(priority.getValue().getEnchantment());

                // if we have a prioritized armor available and this current armor piece is not prioritized, then we can ignore it
                if (bestSlots[index].getPriority() && !armorPriority) {
                    continue;
                }

                // if the armor piece is a "better" piece of armor, we'll set that as a good slot
                if (damageReduceAmount < itemArmor.damageReduceAmount) {
                    bestSlots[index] = new ArmorSlot(i, armorPriority);
                }
            }

            for (int i = 0; i < 4; i++) {

                // if the delay specified has passed
                if (armorTimer.passedTime(delay.getValue().longValue(), Format.MILLISECONDS)) {

                    // open inventory via packets
                    if (inventoryStrict.getValue()) {
                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.OPEN_INVENTORY));
                    }

                    // get the best slots array
                    int id = bestSlots[i].getSlot();

                    // if the damageReduceAmount is equal to -1 (meaning nothing was set there), continue
                    if (id != -1) {

                        // if the armor slot we are replacing was empty
                        // the reason why i do this, is because we do not delay our click packets, so it'll happen instantly
                        // and this will always be true if we did it by the window clicks. so this is the state before we replace the armor
                        boolean hadItem = !mc.player.inventory.armorInventory.get(i).isEmpty();

                        // get the packet slot
                        // when sending a CPacketWindowClick packet, we have to use the proper slot ids
                        // if i had the picture, this is where i'd link it, but i don't.
                        int packetSlot = id < 9 ? id + 36 : id;

                        // quick swap
                        if (fast.getValue()) {

                            // if we had an item in the slot before, that means we have to move it out of the way before switching armor pieces
                            if (hadItem) {
                                mc.playerController.windowClick(0, 8 - i, 0, ClickType.QUICK_MOVE, mc.player);
                            }

                            // click the armor slot we'd like to place the item in
                            // "i" represents the index in the armorInventory list, which the packet id is 8 - slotId
                            mc.playerController.windowClick(0, packetSlot, 0, ClickType.QUICK_MOVE, mc.player);
                        }

                        else {

                            // click the slot we have specified
                            mc.playerController.windowClick(0, packetSlot, 0, ClickType.PICKUP, mc.player);

                            // click the armor slot we'd like to place the item in
                            // "i" represents the index in the armorInventory list, which the packet id is 8 - slotId
                            mc.playerController.windowClick(0, 8 - i, 0, ClickType.PICKUP, mc.player);

                            // if we had an item in the slot before, that means we have an item picked up that is just there
                            // we'll put it back, so theres no floating items in our inventory.
                            if (hadItem) {
                                mc.playerController.windowClick(0, packetSlot, 0, ClickType.PICKUP, mc.player);
                            }
                        }

                        // reset our best slot index.
                        bestSlots[i] = new ArmorSlot(-1, false);

                        // close window
                        if (inventoryStrict.getValue() && mc.getConnection() != null) {
                            mc.getConnection().getNetworkManager().sendPacket(new CPacketCloseWindow(mc.player.inventoryContainer.windowId));
                        }

                        // if we found a valid better slot, we can rest our time.
                        armorTimer.resetTime();
                    }
                }
            }
        }
    }

    public enum Priority {

        /**
         * Prioritizes blast protection
         */
        BLAST(Enchantments.BLAST_PROTECTION),

        /**
         * Prioritizes blast protection
         */
        PROTECTION(Enchantments.PROTECTION);

        // enchant
        private final Enchantment enchantment;

        Priority(Enchantment enchantment) {
            this.enchantment = enchantment;
        }

        /**
         * Gets the enchantment associated with the priority
         * @return The enchantment associated with the priority
         */
        public Enchantment getEnchantment() {
            return enchantment;
        }
    }

    // generic class to hold armor slots and their respective priorities
    public static class ArmorSlot {

        // slot & priority
        private final int slot;
        private final boolean priority;

        public ArmorSlot(int slot, boolean priority) {
            this.slot = slot;
            this.priority = priority;
        }

        /**
         * Gets the armor slot
         * @return The armor slot
         */
        public int getSlot() {
            return slot;
        }

        /**
         * Gets the armor priority
         * @return The armor priority
         */
        public boolean getPriority() {
            return priority;
        }
    }
}
