package cope.cosmos.client.features.command.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.features.command.Command;
import cope.cosmos.client.features.modules.Module;

public class DrawnCommand extends Command {
    public DrawnCommand() {
        super("Drawn", "Hides or shows a module on the arraylist", LiteralArgumentBuilder.literal("drawn")
                .then(RequiredArgumentBuilder.argument("module", StringArgumentType.string())
                        .executes(context -> {
                            Module drawnModule = Cosmos.INSTANCE.getModuleManager().getModule(module -> module.getName().equals(StringArgumentType.getString(context, "module")));
                            Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.GREEN + "Command dispatched successfully!", StringArgumentType.getString(context, "name") + " has been " + (drawnModule.isDrawn() ? "hidden!" : "drawn!"));
                            drawnModule.setDrawn(!drawnModule.isDrawn());

                            return 1;
                        })
                )

                .executes(context -> {
                    Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.RED + "An error occured!", "Please enter the module to draw/hide!");
                    return 1;
                })
        );
    }
}
