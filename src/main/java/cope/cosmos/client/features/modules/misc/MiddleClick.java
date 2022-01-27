package cope.cosmos.client.features.modules.misc;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.SocialManager.*;
import cope.cosmos.client.manager.managers.InventoryManager.Switch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Mouse;

/**
 * @author aesthetical, linustouchtips
 * @since 12/30/2021
 */
public class MiddleClick extends Module {
    public static MiddleClick INSTANCE;

    public MiddleClick() {
        super("MiddleClick", Category.MISC, "Allows you to preform an action when middle-clicking");
        INSTANCE = this;
    }

    public static Setting<EntityAction> entityAction = new Setting<>("EntityAction", EntityAction.FRIEND).setDescription("Action to perform when middle-clicking an entity");
    public static Setting<MissAction> missAction = new Setting<>("MissAction", MissAction.PEARL).setDescription("Action to perform when middle-clicking misses an entity");

    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
        if (Mouse.isButtonDown(2) && Mouse.getEventButtonState()) {

            if (mc.objectMouseOver.typeOfHit.equals(RayTraceResult.Type.ENTITY) && mc.objectMouseOver.entityHit instanceof EntityPlayer) {
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
            }

            else {
                // cache our current hotbar item slot
                int previousSlot = mc.player.inventory.currentItem;

                switch (missAction.getValue()) {
                    case PEARL:
                        // swap to the ender pearl slot
                        getCosmos().getInventoryManager().switchToItem(Items.ENDER_PEARL, Switch.NORMAL);

                        // use the pearl by sending a right click action
                        mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);

                        // we are all done, we can swap back to our original slot
                        getCosmos().getInventoryManager().switchToSlot(previousSlot, Switch.NORMAL);

                        break;
                    case MEND:
                        // swap to the xp slot
                        getCosmos().getInventoryManager().switchToItem(Items.EXPERIENCE_BOTTLE, Switch.NORMAL);

                        // use the xp by sending a right click action
                        mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);

                        // we are all done, we can swap back to our original slot
                        getCosmos().getInventoryManager().switchToSlot(previousSlot, Switch.NORMAL);

                        break;
                }
            }
        }
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

    public enum MissAction {

        /**
         * Throw EXP
         */
        MEND,

        /**
         * Throw an ender pearl
         */
        PEARL
    }
}
