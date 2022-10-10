package cope.cosmos.client.features.modules.miscellaneous;

import cope.cosmos.client.events.input.MiddleClickEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.InventoryManager.InventoryRegion;
import cope.cosmos.client.manager.managers.InventoryManager.Switch;
import cope.cosmos.client.manager.managers.SocialManager.Relationship;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

/**
 * @author aesthetical, linustouchtips
 * @since 12/30/2021
 */
public class MiddleClickModule extends Module {
    public static MiddleClickModule INSTANCE;

    public MiddleClickModule() {
        super("MiddleClick", new String[] {"MCP", "MCF", "MiddleClickPearl", "MiddleClickFriend"}, Category.MISCELLANEOUS, "Allows you to preform an action when middle-clicking");
        INSTANCE = this;
    }

    // **************************** general settings ****************************

    public static Setting<EntityAction> entityAction = new Setting<>("EntityAction", EntityAction.FRIEND)
            .setDescription("Action to perform when middle-clicking an entity");

    public static Setting<Boolean> pearl = new Setting<>("Pearl", true)
            .setAlias("MCP", "MiddleClickPearl")
            .setDescription("If to pearl if raytrace type is MISS");

    public static Setting<Boolean> mend = new Setting<>("Mend", true)
            .setDescription("If to mend if raytrace type is BLOCK");

    public static Setting<Boolean> guis = new Setting<>("Guis", false)
            .setVisible(mend::getValue)
            .setDescription("If to mend if holding middle click in a GUI");

    public static Setting<Boolean> cancelBlock = new Setting<>("CancelBlock", true)
            .setDescription("Cancels block picking if you middle click a block");

    private int serverSlot = -1;

    @Override
    public void onDisable() {
        super.onDisable();

        resetProcess();
        serverSlot = -1;
    }

    @Override
    public void onTick() {

        // if we're facing a block and we want to mend and we have middle click down
        // also check for if we do not allow guis and if our currentScreen is null (no gui open)
        if (Mouse.isButtonDown(2)) {
            if (!guis.getValue() && mc.currentScreen != null) {
                resetProcess();
                return;
            }

            if (mc.objectMouseOver.typeOfHit.equals(Type.BLOCK) && mend.getValue()) {
                int currentSlot = mc.player.inventory.currentItem;

                // get the first xp bottle slot
                int slot = getCosmos().getInventoryManager().searchSlot(Items.EXPERIENCE_BOTTLE, InventoryRegion.HOTBAR);

                // if slot is -1 (invalid), we'll swap back
                if (slot == -1) {

                    // resync with our current slot
                    if (serverSlot != currentSlot) {
                        mc.player.connection.sendPacket(new CPacketHeldItemChange(currentSlot));
                    }

                    // reset xp slot for when we get more xp
                    serverSlot = -1;

                    // return, we're done
                    return;
                }

                // if the xp slot != our current slot, and it is not our current held item
                if (slot != serverSlot) {
                    serverSlot = slot;
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
                }

                // use the xp bottle
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
            }
        }

        else {
            resetProcess();
        }
    }

    @SubscribeEvent
    public void onMouseInput(MiddleClickEvent event) {

        // if we are on block and we want to cancel block picking, we'll stop us from picking the block
        if (mc.objectMouseOver.typeOfHit.equals(Type.BLOCK) && cancelBlock.getValue()) {
            event.setCanceled(true);
        }

        int currentSlot = mc.player.inventory.currentItem;

        switch (mc.objectMouseOver.typeOfHit) {
            case ENTITY: {
                if (!(mc.objectMouseOver.entityHit instanceof EntityPlayer) || !Mouse.getEventButtonState()) {
                    break;
                }

                switch (entityAction.getValue()) {
                    case FRIEND:
                        // get the current relationship between this player and us
                        if (getCosmos().getSocialManager().getSocial(mc.objectMouseOver.entityHit.getName()).equals(Relationship.FRIEND)) {
                            // remove them from our social manager
                            getCosmos().getSocialManager().removeSocial(mc.objectMouseOver.entityHit.getName());

                            // tell the user in chat
                            getCosmos().getChatManager().sendClientMessage("Removed friend with name " + mc.objectMouseOver.entityHit.getName());
                        }

                        else {
                            // add them to our social manager as a friend
                            getCosmos().getSocialManager().addSocial(mc.objectMouseOver.entityHit.getName(), Relationship.FRIEND);

                            // tell the user in chat
                            getCosmos().getChatManager().sendClientMessage("Added friend with name " + mc.objectMouseOver.entityHit.getName());

                            // tell the player we just friended
                            mc.player.connection.sendPacket(new CPacketChatMessage("/w " + mc.objectMouseOver.entityHit.getName() + " I just added you as a friend on Cosmos!"));
                        }

                        break;
                    case DUEL:
                        // send a duel request
                        mc.player.connection.sendPacket(new CPacketChatMessage("/duel " + mc.objectMouseOver.entityHit.getName()));
                        break;
                }

                break;
            }

            case MISS: {
                if (pearl.getValue()) {

                    // swap to the ender pearl slot
                    getCosmos().getInventoryManager().switchToItem(Items.ENDER_PEARL, Switch.NORMAL);

                    // use the pearl by sending a right click action
                    mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);

                    // we are all done, we can swap back to our original slot
                    getCosmos().getInventoryManager().switchToSlot(currentSlot, Switch.NORMAL);
                }
            }
        }
    }

    /**
     * Resets the held item
     */
    private void resetProcess() {
        if (serverSlot != -1 && serverSlot != mc.player.inventory.currentItem) {
            mc.player.connection.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
        }

        serverSlot = -1;
    }

    public enum EntityAction {

        /**
         * Add the entity to the friends list
         */
        FRIEND,

        /**
         * Send a duel request to the entity
         */
        DUEL
    }
}