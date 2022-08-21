package cope.cosmos.client.manager.managers;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.manager.Manager;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 08/15/2022
 */
public class ItemManager extends Manager {

    // serverside held item
    private int serverHeldItem = -1;

    // serverside slots
    Slot heldSlot;
    Slot swapSlot;

    // serverside swap stacks
    ItemStack heldStack;
    ItemStack swapStack;

    public ItemManager() {
        super("ItemManager", "Manages serverside item syncing");
        Cosmos.EVENT_BUS.register(this);
    }

    @Override
    public void onTick() {

        // resync every tick
        // syncItem();
        // syncSlots();
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {

        // packet for switching held items
        if (event.getPacket() instanceof CPacketHeldItemChange) {

            // update the server held item
            serverHeldItem = ((CPacketHeldItemChange) event.getPacket()).getSlotId();

            // if we are not already holding the item
            if (mc.player.inventory.currentItem != ((CPacketHeldItemChange) event.getPacket()).getSlotId()) {

                // sync item
                mc.player.inventory.currentItem = ((CPacketHeldItemChange) event.getPacket()).getSlotId();
            }
        }

        // packet for clicking windows
        if (event.getPacket() instanceof CPacketClickWindow) {

            // swapped items
            if (((CPacketClickWindow) event.getPacket()).getClickType().equals(ClickType.SWAP)) {

                // player inventory
                if (((CPacketClickWindow) event.getPacket()).getWindowId() == 0) {

                    // hotbar item swap
                    if (((CPacketClickWindow) event.getPacket()).getUsedButton() >= 0 && ((CPacketClickWindow) event.getPacket()).getUsedButton() < 9) {

                        // update the server slot info
                        swapSlot = mc.player.inventoryContainer.getSlot(((CPacketClickWindow) event.getPacket()).getSlotId());
                        heldSlot = mc.player.inventoryContainer.getSlot(((CPacketClickWindow) event.getPacket()).getUsedButton() + 36);

                        // update the server held stack info
                        swapStack = swapSlot.getStack();
                        heldStack = heldSlot.getStack();

                        // sync stacks
                        if (swapSlot.getStack() != heldStack) {
                            // swapSlot.putStack(heldStack);
                        }

                        if (heldSlot.getStack() != swapStack) {
                            // heldSlot.putStack(swapStack);
                        }
                    }
                }
            }
        }
    }

    /**
     * Syncs clientside held item to the serverside held item
     */
    public void syncItem() {

        // no server slot
        if (serverHeldItem != -1) {

            // if we are not already holding the item
            if (mc.player.inventory.currentItem != serverHeldItem) {

                // sync item
                mc.player.inventory.currentItem = serverHeldItem;
            }
        }
    }

    /**
     * Syncs clientside slot stacks to the serverside slot stacks
     */
    public void syncSlots() {

        // sync stacks
        if (swapSlot.getStack() != heldStack) {
            swapSlot.putStack(heldStack);
        }

        if (heldSlot.getStack() != swapStack) {
            heldSlot.putStack(swapStack);
        }
    }
}
