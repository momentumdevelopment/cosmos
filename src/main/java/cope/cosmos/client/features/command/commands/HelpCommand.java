package cope.cosmos.client.features.command.commands;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.features.command.Command;
import cope.cosmos.util.chat.ChatBuilder;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;

import java.util.List;

/**
 * @author linustouchtips
 * @since 08/24/2022
 */
public class HelpCommand extends Command {
    public static HelpCommand INSTANCE;

    public HelpCommand() {
        super("Help", new String[]  {"Commands", "CommandList"}, "Displays all commands");
        INSTANCE = this;
    }

    @Override
    public void onExecute(String[] args) {

        // command list
        if (args.length == 0) {

            // list of commands
            List<Command> commands = getCosmos().getCommandManager().getAllCommands();

            // list of commands formatted for chat
            ChatBuilder commandList = new ChatBuilder();

            // add all commands to command list
            for (int i = 0; i < commands.size(); i++) {
                commandList.append(commands.get(i).getName(), new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatBuilder()
                                .append(commands.get(i).getName(), new Style().setColor(TextFormatting.DARK_PURPLE))
                                .append("\n" + commands.get(i).getDescription(), new Style().setColor(TextFormatting.BLUE))
                                .component())));

                // more presets
                if (i < commands.size() - 1) {
                    commandList.append(", ", new Style());
                }
            }

            // send info
            getCosmos().getChatManager().sendClientMessage(ChatFormatting.GRAY + "[Commands] ");

            // print message
            commandList.push();
        }

        // command help
        else if (args.length == 1) {

            // command that needs more info
            Command command = getCosmos().getCommandManager().getCommand(command1 -> command1.equals(args[0]));

            // valid command
            if (command != null) {

                // send info
                getCosmos().getChatManager().sendClientMessage("[" + command.getName() + "] " + ChatFormatting.GRAY + command.getDescription());
            }

            else {

                // unrecognized arguments exception
                getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Command!", ChatFormatting.RED + "Please enter a valid command to get more information about.");
            }
        }

        else {

            // unrecognized arguments exception
            getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Arguments!", ChatFormatting.RED + "Please enter the correct arguments for this command.");
        }
    }

    @Override
    public String getUseCase() {
        return "<optional:command>";
    }

    @Override
    public int getArgSize() {
        return 1;
    }
}
