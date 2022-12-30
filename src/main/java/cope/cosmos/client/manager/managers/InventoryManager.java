package cope.cosmos.client.manager.managers;

import cope.cosmos.asm.mixins.accessor.IPlayerControllerMP;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.network.PacketEvent.PacketSendEvent;
import cope.cosmos.client.manager.Manager;
import net.minecraft.block.Block;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips, aesthetical
 * @since 12/30/2021
 */
public class InventoryManager extends Manager {

    // held item slot (serverside)
    private int serverSlot = -1;

    public InventoryManager() {
        super("InventoryManager", "Manages player hotbar and inventory actions");
        Cosmos.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent event) {

        // packet for switching items
        if (event.getPacket() instanceof CPacketHeldItemChange) {

            // bounds check - prevents getting kicked by NCP updated in case we accidentally send an invalid swap packet
            if (!InventoryPlayer.isHotbar(((CPacketHeldItemChange) event.getPacket()).getSlotId())) {
                event.setCanceled(true);
                return;
            }

            // update server slot
            serverSlot = ((CPacketHeldItemChange) event.getPacket()).getSlotId();
        }
    }

    /**
     * Switches to a specific slot number in the hotbar
     * @param in The slot to switch to
     * @param swap The switch mode to use
     */
    public void switchToSlot(int in, Switch swap) {

        // check if the slot is actually in the hotbar
        if (InventoryPlayer.isHotbar(in)) {

            // not already at the correct slot
            if (mc.player.inventory.currentItem != in) {
                switch (swap) {
                    case NORMAL:
                        // update our current item and send a packet to the server
                        mc.player.inventory.currentItem = in;
                        mc.player.connection.sendPacket(new CPacketHeldItemChange(in));
                        break;
                    case PACKET:
                        // send a switch packet to the server, should be silent client-side
                        mc.player.connection.sendPacket(new CPacketHeldItemChange(in));
                        ((IPlayerControllerMP) mc.playerController).setCurrentPlayerItem(in);
                        // ((IPlayerControllerMP) mc.playerController).hookSyncCurrentPlayItem();
                        break;
                }
            }
        }
    }

    /**
     * Switches to an item in the hotbar
     * @param in The item to switch to
     * @param swap The switch mode to use
     */
    public void switchToItem(Item in, Switch swap) {

        // slot of the item
        int slot = searchSlot(in, InventoryRegion.HOTBAR);

        // switch to the slot
        switchToSlot(slot, swap);
    }

    /**
     * Switches to an item in the hotbar
     * @param in The item to switch to
     * @param swap The switch mode to use
     */
    public void switchToItem(Class<? extends Item> in, Switch swap) {

        // slot of the item
        int slot = searchSlot(in, InventoryRegion.HOTBAR);

        // switch to the slot
        switchToSlot(slot, swap);
    }

    /**
     * Switches to the first of a list of specified items in the hotbar
     * @param in The items to switch to
     * @param swap The switch mode to use
     */
    public void switchToItem(Item[] in, Switch swap) {
        // the first valid slot
        int slot = -1;

        // search our hotbar
        for (int i = InventoryRegion.HOTBAR.getStart(); i < InventoryRegion.HOTBAR.getBound(); i++) {

            // check each of the items
            for (Item item : in) {
                if (mc.player.inventory.getStackInSlot(i).getItem().equals(item)) {
                    slot = i;
                    break;
                }
            }

            // if we found a slot, exit the process
            if (slot != -1) {
                break;
            }
        }

        // switch to the found slot
        switchToSlot(slot, swap);
    }

    /**
     * Switches to a block in the hotbar
     * @param in The block to switch to
     * @param swap The switch mode to use
     */
    public void switchToBlock(Block in, Switch swap) {
        switchToItem(Item.getItemFromBlock(in), swap);
    }

    /**
     * Switches to the first of a list of specified blocks in the hotbar
     * @param in The blocks to switch to
     * @param swap The switch mode to use
     */
    public void switchToBlock(Block[] in, Switch swap) {

        // array of blocks converted to items
        Item[] blockItems = new Item[in.length];

        // copy the converted values to our items array
        for (int i = 0; i < in.length; i++) {
            blockItems[i] = Item.getItemFromBlock(in[i]);
        }

        // switch to our blocks
        switchToItem(blockItems, swap);
    }

    /**
     * Searches the slot id of a given item
     * @param in The given item
     * @param inventoryRegion The inventory region to search
     * @return The slot id of the given item
     */
    public int searchSlot(Item in, InventoryRegion inventoryRegion) {

        // slot of the item
        int slot = -1;

        // search the region for the item
        for (int i = inventoryRegion.getStart(); i < inventoryRegion.getBound(); i++) {

            // if we found the slot, save it and return
            if (mc.player.inventory.getStackInSlot(i).getItem().equals(in)) {
                slot = i;
                break;
            }
        }

        return slot;
    }

    /**
     * Searches the slot id of a given item
     * @param in The given item
     * @param inventoryRegion The inventory region to search
     * @return The slot id of the given item
     */
    public int searchSlot(Block[] in, InventoryRegion inventoryRegion) {

        // slot of the item
        int slot = -1;

        // search the region for the item
        for (int i = inventoryRegion.getStart(); i < inventoryRegion.getBound(); i++) {

            // check block list
            for (Block block : in) {

                // if we found the slot, save it and return
                if (slot == -1 && mc.player.inventory.getStackInSlot(i).getItem().equals(Item.getItemFromBlock(block))) {
                    slot = i;
                    break;
                }
            }
        }

        return slot;
    }

    /**
     * Searches the slot id of a given item
     * @param in The given item
     * @param inventoryRegion The inventory region to search
     * @return The slot id of the given item
     */
    public int searchSlot(Class<? extends Item> in, InventoryRegion inventoryRegion) {

        // slot of the item
        int slot = -1;

        // search the region for the item
        for (int i = inventoryRegion.getStart(); i <= inventoryRegion.getBound(); i++) {

            // if we found the slot, save it and return
            if (in.isInstance(mc.player.inventory.getStackInSlot(i).getItem())) {
                slot = i;
                break;
            }
        }

        return slot;
    }

    /**
     * Gets the slot we are on server side
     * @return the server side slot
     */
    public int getServerSlot() {
        return serverSlot;
    }

    /**
     * Resyncs the client with the server slot
     */
    public void syncSlot() {
        if (serverSlot != mc.player.inventory.currentItem) {
            mc.player.inventory.currentItem = serverSlot;
        }
    }

    public enum Switch {

        /**
         * Client-side switches to an item
         */
        NORMAL,

        /**
         * Server-side switches to an item, not visible client-side
         */
        PACKET,

        /**
         * Does not switch
         */
        NONE
    }

    public enum InventoryRegion {

        /**
         * Search the full inventory
         */
        INVENTORY(0, 45),

        /**
         * Search the hotbar
         */
        HOTBAR(0, 8),

        /**
         * Search the crafting inventory
         */
        CRAFTING(80, 83),

        /**
         * Search the armor inventory
         */
        ARMOR(100, 103);

        // inventory slot id's
        private final int start, bound;

        InventoryRegion(int start, int bound) {
            this.start = start;
            this.bound = bound;
        }

        /**
         * Gets the starting slot id
         * @return The starting slot id
         */
        public int getStart() {
            return start;
        }

        /**
         * Gets the last slot id
         * @return The last slot id
         */
        public int getBound() {
            return bound;
        }
    }
}