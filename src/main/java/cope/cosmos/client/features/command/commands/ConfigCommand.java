package cope.cosmos.client.features.command.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.features.command.Command;

public class ConfigCommand extends Command {
    public ConfigCommand() {
        super("Config", "Creates or Updates a preset", LiteralArgumentBuilder.literal("config")
                .then(RequiredArgumentBuilder.argument("action", StringArgumentType.string()).then(RequiredArgumentBuilder.argument("name", StringArgumentType.string())
                        .executes(context -> {
                            switch (StringArgumentType.getString(context, "action")) {
                                case "save":
                                    Cosmos.INSTANCE.getConfigManager().createPreset(StringArgumentType.getString(context, "name"));
                                    Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.GREEN + "Command dispatched successfully!", "Saved current config");
                                case "load":
                                    Cosmos.INSTANCE.getConfigManager().loadPreset(StringArgumentType.getString(context, "name"));
                                    Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.GREEN + "Command dispatched successfully!", "Loaded current config");
                                    break;
                                case "remove":
                                    Cosmos.INSTANCE.getConfigManager().deletePreset(StringArgumentType.getString(context, "name"));
                                    Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.GREEN + "Command dispatched successfully!", "Removed preset with name " + StringArgumentType.getString(context, "name"));
                                    break;
                            }

                            return 1;
                        })
                )

                .executes(context -> {
                    Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.RED + "An error occurred!", "Please enter the name of the preset!");
                    return 1;
                }))

                .executes(context -> {
                    Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.RED + "An error occurred!", "Please enter the correct action, was expecting save, remove, or load!");
                    return 1;
                })
        );
    }
}
