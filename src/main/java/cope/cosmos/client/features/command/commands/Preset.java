package cope.cosmos.client.features.command.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.features.command.Command;
import cope.cosmos.util.client.ChatUtil;

public class Preset extends Command {
    public Preset() {
        super("Preset", "Creates or Updates a preset", LiteralArgumentBuilder.literal("preset")
                .then(RequiredArgumentBuilder.argument("action", StringArgumentType.string()).then(RequiredArgumentBuilder.argument("name", StringArgumentType.string())
                        .executes(context -> {
                            switch (StringArgumentType.getString(context, "action")) {
                                case "save":
                                    Cosmos.INSTANCE.getPresetManager().createPreset(StringArgumentType.getString(context, "name"));
                                    Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.GREEN + "Command dispatched successfully!", "Saved current prefix");
                                case "load":
                                    Cosmos.INSTANCE.getPresetManager().setPreset(StringArgumentType.getString(context, "name"));
                                    Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.GREEN + "Command dispatched successfully!", "Loaded current preset");
                                    break;
                                case "remove":
                                    Cosmos.INSTANCE.getPresetManager().removePreset(StringArgumentType.getString(context, "name"));
                                    Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.GREEN + "Command dispatched successfully!", "Removed preset with name " + StringArgumentType.getString(context, "name"));
                                    break;
                            }

                            return 1;
                        })
                )

                .executes(context -> {
                    Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.RED + "An error occured!", "Please enter the name of the preset!");
                    return 1;
                }))

                .executes(context -> {
                    Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.RED + "An error occured!", "Please enter the correct action, was expecting save, remove, or load!");
                    return 1;
                })
        );
    }
}
