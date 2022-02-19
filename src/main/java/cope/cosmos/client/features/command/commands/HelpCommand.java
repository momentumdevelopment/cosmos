package cope.cosmos.client.features.command.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.features.command.Command;

import java.util.List;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("Help", "Displays all commands", LiteralArgumentBuilder.literal("help")
                .then(RequiredArgumentBuilder.argument("page", IntegerArgumentType.integer())
                        .executes((context) -> {
                            sendHelpList(IntegerArgumentType.getInteger(context, "page"));
                            return 1;
                        })
                )
                .executes((context) -> {
                    sendHelpList(1);
                    return 1;
                })
        );
    }

    /**
     * Sends all commands that are truncated onto the provided page
     * @param page The page you'd like to view
     */
    private static void sendHelpList(int page) {
        List<Command> commands = Cosmos.INSTANCE.getCommandManager().getAllCommands();

        // we'll have a max of 5 commands per "page"
        int maxLength = 5;

        // get the max amount of pages by figuring out how many times maxLength can go into the commmand length
        int total = (int) Math.ceil(commands.size() / (double) maxLength);

        // make sure our page count isn't out of bounds
        if (page > total || page < 1) {
            page = 1;
        }

        // send our base message. hovering over will tell them what page they're on and the total amount of pages
        Cosmos.INSTANCE.getChatManager().sendHoverableMessage("Commands list", "Page " + page + "/" + total);

        // we truncate the list by the page - 1 (for indexing in java) * our max length. we then
        // can make sure our to parameter doesn't go out of bounds, otherwise java becomes big mad
        commands.subList((page - 1) * maxLength, Math.min(page * maxLength, commands.size()))
                .forEach((command) -> Cosmos.INSTANCE.getChatManager().sendHoverableMessage(
                        ChatFormatting.GRAY +
                                command.getName().toLowerCase() +
                                ChatFormatting.RESET, command.getDescription())
                );
    }
}
