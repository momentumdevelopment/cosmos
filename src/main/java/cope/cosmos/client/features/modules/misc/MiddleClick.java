package cope.cosmos.client.features.modules.misc;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.SocialManager.*;
import cope.cosmos.util.client.ChatUtil;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.player.InventoryUtil.*;
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
        super("MiddleClick", Category.MISC, "Allows you to preform an action when middle-clicking");
        INSTANCE = this;
    }

    public static Setting<Boolean> friend = new Setting<>("Friend", true).setDescription("Add a player as a friend when middle-clicking");
    public static Setting<Boolean> pearl = new Setting<>("Pearl", false).setDescription("Throw a pearl when middle-clicking");

    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
        if (Mouse.isButtonDown(2) && Mouse.getEventButtonState()) {

            if (mc.objectMouseOver.typeOfHit.equals(RayTraceResult.Type.MISS)) {
                if (pearl.getValue()) {
                    // cache our current hotbar item slot
                    int previousSlot = mc.player.inventory.currentItem;

                    // swap to the ender pearl slot
                    InventoryUtil.switchToSlot(Items.ENDER_PEARL, Switch.NORMAL);

                    // use the pearl by sending a right click action
                    mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);

                    // we are all done, we can swap back to our original slot
                    InventoryUtil.switchToSlot(previousSlot, Switch.NORMAL);
                }
            }

            if (mc.objectMouseOver.typeOfHit.equals(RayTraceResult.Type.ENTITY) && mc.objectMouseOver.entityHit instanceof EntityPlayer) {

                if (friend.getValue()) {
                    // get the current relationship between this player and us
                    if (getCosmos().getSocialManager().getSocial(mc.objectMouseOver.entityHit.getName()).equals(Relationship.FRIEND)) {
                        // remove them from our social manager
                        getCosmos().getSocialManager().removeSocial(mc.objectMouseOver.entityHit.getName());

                        // tell the user in chat
                        ChatUtil.sendMessage("Removed friend with name " + mc.objectMouseOver.entityHit.getName());
                    }

                    else {
                        // add them to our social manager as a friend
                        getCosmos().getSocialManager().addSocial(mc.objectMouseOver.entityHit.getName(), Relationship.FRIEND);

                        // tell the user in chat
                        ChatUtil.sendMessage("Added friend with name " + mc.objectMouseOver.entityHit.getName());

                        // tell the player we just friended
                        mc.player.connection.sendPacket(new CPacketChatMessage("/w " + mc.objectMouseOver.entityHit.getName() + " I just added you as a friend on Cosmos!"));
                    }
                }
            }
        }
    }
}
