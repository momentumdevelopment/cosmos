package cope.cosmos.client.features.modules.misc;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.SocialManager;
import cope.cosmos.util.client.ChatUtil;
import cope.cosmos.util.player.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Mouse;

/**
 * @author aesthetical
 * @since 12/30/2021
 */
public class MiddleClick extends Module {
    public static MiddleClick INSTANCE;

    public MiddleClick() {
        super("MiddleClick", Category.MISC, "Does things upon a middle click");
        INSTANCE = this;
    }

    public static final Setting<Boolean> friend = new Setting<>("Friend", true).setDescription("If to add the player as a friend");
    public static final Setting<Boolean> unfriend = new Setting<>("Unfriend", true).setDescription("If to be able to unfriend when middle clicking a player").setParent(friend);

    public static final Setting<Boolean> pearl = new Setting<>("Pearl", false).setDescription("If to throw a pearl upon a middle click");

    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
        if (Mouse.isButtonDown(2) && Mouse.getEventButtonState()) {
            RayTraceResult result = mc.objectMouseOver;

            if (result.typeOfHit.equals(RayTraceResult.Type.MISS) && pearl.getValue()) {
                // get the hotbar slot with an ender pearl on it
                int slot = InventoryUtil.getItemSlot(Items.ENDER_PEARL, InventoryUtil.Inventory.HOTBAR);

                // if there is none, return
                if (slot == -1) {
                    return;
                }

                // cache our current hotbar item slot
                int oldSlot = mc.player.inventory.currentItem;

                // swap to the ender pearl slot
                InventoryUtil.switchToSlot(slot, InventoryUtil.Switch.NORMAL);

                // use the pearl by sending a right click action
                mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);

                // we are all done, we can swap back to our original slot
                InventoryUtil.switchToSlot(oldSlot, InventoryUtil.Switch.NORMAL);
            }

            if (result.typeOfHit.equals(RayTraceResult.Type.ENTITY) && result.entityHit instanceof EntityPlayer && friend.getValue()) {
                // get the player that our crosshair is over
                EntityPlayer player = (EntityPlayer) result.entityHit;

                // get the current relationship between this player and us
                SocialManager.Relationship relationship = getCosmos().getSocialManager().getSocial(player.getName());
                if (relationship.equals(SocialManager.Relationship.FRIEND)) {
                    // if we're already friends with this player, and we have unfriending off, just return
                    if (!unfriend.getValue()) {
                        return;
                    }

                    // remove them from our social manager
                    getCosmos().getSocialManager().removeSocial(player.getName());

                    // tell the user in chat
                    ChatUtil.sendMessage("Removed friend with name " + player.getName());
                } else {
                    // add them to our social manager as a friend
                    getCosmos().getSocialManager().addSocial(player.getName(), SocialManager.Relationship.FRIEND);

                    // tell the user in chat
                    ChatUtil.sendMessage("Added friend with name " + player.getName());

                    // tell the player we just friended
                    mc.player.connection.sendPacket(new CPacketChatMessage("/w " + player.getName() + " I just added you as a friend on Cosmos!"));
                }
            }
        }
    }
}
