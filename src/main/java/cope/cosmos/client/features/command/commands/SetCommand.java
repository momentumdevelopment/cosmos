package cope.cosmos.client.features.command.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.features.command.Command;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;

import java.awt.*;
import java.util.concurrent.CompletableFuture;

public class SetCommand extends Command {

    public SetCommand() {
        super("Set", "Sets a setting in a module", LiteralArgumentBuilder.literal("set")
                .then(RequiredArgumentBuilder.argument("module", StringArgumentType.string()).suggests((context, builder) -> suggestNames(builder))
                        .then(RequiredArgumentBuilder.argument("setting", StringArgumentType.string()).suggests((context, builder) -> suggestSettingNames(builder, context))
                                .then(RequiredArgumentBuilder.argument("value", StringArgumentType.string()).suggests((context, builder) -> suggestValues(builder, context))
                                    .executes(context -> {
                                        Module m = Cosmos.INSTANCE.getModuleManager().getModule(module -> module.getName().equalsIgnoreCase(StringArgumentType.getString(context, "module")));
                                        Setting setting = m.getSettings().stream().filter(s -> s.getName().equalsIgnoreCase(StringArgumentType.getString(context, "setting"))).findFirst().orElse(null);

                                        try {
                                            if (setting.getValue() instanceof Float){
                                                setting.setValue(Float.valueOf(StringArgumentType.getString(context, "value")));
                                            } else if (setting.getValue() instanceof Double){
                                                setting.setValue(Double.valueOf(StringArgumentType.getString(context, "value")));
                                            } else if (setting.getValue() instanceof Boolean){
                                                setting.setValue(Boolean.valueOf(StringArgumentType.getString(context, "value")));
                                            } if (setting.getValue() instanceof Float){
                                                setting.setValue(Float.valueOf(StringArgumentType.getString(context, "value")));
                                            } else if (setting.getValue() instanceof Enum<?>){
                                                setting.setValue(Enum.valueOf(((Enum<?>) setting.getValue()).getClass(), StringArgumentType.getString(context, "value")));
                                            }
                                            //TODO parse for Colours
                                            Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.GREEN + "Command dispatched successfully!",
                                                    "Set " + StringArgumentType.getString(context, "setting") + " to " + StringArgumentType.getString(context, "value"));

                                        } catch (Exception e) {
                                            Cosmos.INSTANCE.getChatManager().sendHoverableMessage("Command Failed!", "expected type " +
                                                    setting.getValue().getClass().getName() +
                                                    " but received " +
                                                    StringArgumentType.getString(context, "value"));
                                        }
                                        return 1;
                                    })
                                    ).executes(context -> {
                                                Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.RED + "An error occurred!", "Please enter a correct value!");
                                                return 1;
                                            })
                        ).executes(context -> {
                            Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.RED + "An error occurred!", "Please enter a setting!");
                            return 1;
                        })
                ));
    }

    private static CompletableFuture<Suggestions> suggestNames(SuggestionsBuilder suggestionsBuilder) {
        Cosmos.INSTANCE.getModuleManager().getAllModules().forEach(module ->
                suggestionsBuilder.suggest(module.getName())
        );

        return suggestionsBuilder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestSettingNames(SuggestionsBuilder suggestionsBuilder, CommandContext context) {
        Module m = Cosmos.INSTANCE.getModuleManager().getModule(module -> module.getName().equalsIgnoreCase(StringArgumentType.getString(context, "module")));
        m.getSettings().forEach(setting -> suggestionsBuilder.suggest(setting.getName()));
        return suggestionsBuilder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestValues(SuggestionsBuilder suggestionsBuilder, CommandContext context) {
        Module m = Cosmos.INSTANCE.getModuleManager().getModule(module -> module.getName().equalsIgnoreCase(StringArgumentType.getString(context, "module")));
        Setting setting = m.getSettings().stream().filter(s -> s.getName().equalsIgnoreCase(StringArgumentType.getString(context, "setting"))).findFirst().orElse(null);
        if (setting != null){
            suggestionsBuilder.suggest(setting.getValue().toString());
        }
        return suggestionsBuilder.buildFuture();
    }


}
