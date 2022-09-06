package cope.cosmos.client.features.command.commands;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.features.command.Command;
import cope.cosmos.client.manager.managers.SocialManager.Relationship;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author linustouchtips
 * @since 08/24/2022
 */
public class FriendCommand extends Command {
    public static FriendCommand INSTANCE;

    public FriendCommand() {
        super("Friend", new String[] {"F"}, "Adds players to the friends list");
        INSTANCE = this;
    }

    @Override
    public void onExecute(String[] args) {

        if (args.length == 2) {

            // new friend
            EntityPlayer friend = null;

            // search name
            for (EntityPlayer player : mc.world.playerEntities) {

                // check name
                if (args[1].equalsIgnoreCase(player.getName())) {
                    friend = player;
                    break;
                }
            }

            // friend found
            if (friend != null && !friend.equals(mc.player)) {

                // add friends
                if (args[0].equalsIgnoreCase("Add")) {
                    getCosmos().getSocialManager().addSocial(friend.getName(), Relationship.FRIEND);

                    // command success
                    getCosmos().getChatManager().sendClientMessage("Added friend with name " + ChatFormatting.GRAY + friend.getName());

                    // send a message to notify the friended player
                    getCosmos().getChatManager().sendChatMessage("/w " + friend.getName() + " I just added you as a friend on Cosmos!");
                }

                // remove friends
                else if (args[0].equalsIgnoreCase("Remove") || args[0].equalsIgnoreCase("Del") || args[0].equalsIgnoreCase("Delete")) {
                    getCosmos().getSocialManager().removeSocial(friend.getName());

                    // command success
                    getCosmos().getChatManager().sendClientMessage("Removed friend with name " + ChatFormatting.GRAY + friend.getName());
                }

                else {

                    // unrecognized arguments exception
                    getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Action!", ChatFormatting.RED + "Please enter the correct action for this command.");
                }
            }

            else {

                // unrecognized player exception
                getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Player!", ChatFormatting.RED + "Player could not be found.");
            }
        }

        else if (args.length == 1) {

            // new friend
            EntityPlayer friend = null;

            // search name
            for (EntityPlayer player : mc.world.playerEntities) {

                // check name
                if (args[0].equalsIgnoreCase(player.getName())) {
                    friend = player;
                    break;
                }
            }

            // friend found
            if (friend != null) {

                // add friends
                getCosmos().getSocialManager().addSocial(friend.getName(), Relationship.FRIEND);

                // command success
                getCosmos().getChatManager().sendClientMessage("Added friend with name " + ChatFormatting.GRAY + friend.getName());
            }

            else {

                // unrecognized player exception
                getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Player!", ChatFormatting.RED + "Player could not be found.");
            }
        }

        else {

            // unrecognized arguments exception
            getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Arguments!", ChatFormatting.RED + "Please enter the correct arguments for this command.");
        }
    }

    @Override
    public String getUseCase() {
        return "<optional:add/remove> <name>";
    }

    @Override
    public int getArgSize() {
        return 2;
    }
}
