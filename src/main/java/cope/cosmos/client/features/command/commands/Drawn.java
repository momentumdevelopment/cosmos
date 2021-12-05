package cope.cosmos.client.features.command.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.features.command.Command;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.managment.managers.ModuleManager;
import cope.cosmos.utility.client.ChatUtil;

public class Drawn extends Command {
    public Drawn() {
        super("Drawn", "Hides or shows a module on the arraylist", LiteralArgumentBuilder.literal("drawn")
                .then(RequiredArgumentBuilder.argument("module", StringArgumentType.string())
                        .executes(context -> {
                            Module drawnModule = ModuleManager.getModule(module -> module.getName().equals(StringArgumentType.getString(context, "module")));
                            ChatUtil.sendHoverableMessage(ChatFormatting.GREEN + "Command dispatched successfully!", StringArgumentType.getString(context, "name") + " has been " + (drawnModule.isDrawn() ? "hidden!" : "drawn!"));
                            drawnModule.setDrawn(!drawnModule.isDrawn());

                            return 1;
                        })
                )

                .executes(context -> {
                    ChatUtil.sendHoverableMessage(ChatFormatting.RED + "An error occured!", "Please enter the module to draw/hide!");
                    return 1;
                })
        );
    }
}
