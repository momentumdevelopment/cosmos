package cope.cosmos.client.features.command.commands;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.features.command.Command;

/**
 * @author linustouchtips
 * @since 08/24/2022
 */
public class PrefixCommand extends Command {
    public static PrefixCommand INSTANCE;

    public PrefixCommand() {
        super("Prefix", "Sets the client prefix");
        INSTANCE = this;
    }

    @Override
    public void onExecute(String[] args) {

        if (args.length == 1) {

            // new prefix
            String prefix = args[0];

            // must be of length 1
            if (prefix.length() == 1) {

                // update prefix
                Cosmos.PREFIX = prefix;

                // notify
                getCosmos().getChatManager().sendHoverableMessage("Prefix set to " + ChatFormatting.GRAY + args[0], "Set the client prefix.");
            }

            else {

                // unsupported prefix length exception
                getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unsupported Prefix Length!", ChatFormatting.RED + "Please enter a prefix of length 1.");
            }
        }

        else {

            // unrecognized arguments exception
            getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Arguments!", ChatFormatting.RED + "Please enter the correct arguments for this command.");
        }
    }

    @Override
    public String getUseCase() {
        return "<prefix>";
    }

    @Override
    public int getArgSize() {
        return 0;
    }
}
