package cope.cosmos.client.features.command.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.features.command.Command;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Bind;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import static cope.cosmos.client.features.setting.Bind.Device;

/**
 * @author Wolfsurge
 */
public class BindCommand extends Command {

    public BindCommand() {
        super("Bind", "Binds a module to the given keybind", LiteralArgumentBuilder.literal("bind")
            .then(RequiredArgumentBuilder.argument("module", StringArgumentType.string()).suggests((context, builder) -> suggestNames(builder))
                    .then(RequiredArgumentBuilder.argument("key", StringArgumentType.string())

                    .executes(context -> {
                        // Module to bind
                        Module toBind = Cosmos.INSTANCE.getModuleManager().getModule(module -> module.getName().equalsIgnoreCase(StringArgumentType.getString(context, "module")));

                        // Key arg
                        String key = StringArgumentType.getString(context, "key").toUpperCase();

                        // Mouse button
                        if (key.contains("MOUSE") || key.contains("BUTTON")) {
                            key = key.replace("MOUSE", "BUTTON");

                            int code = Mouse.getButtonIndex(key);

                            toBind.getBind().setValue(new Bind(code, Device.MOUSE));
                        }

                        // Keyboard
                        else {
                            int code = Keyboard.getKeyIndex(key);

                            toBind.getBind().setValue(new Bind(code, Device.KEYBOARD));
                        }

                        // Command success
                        if (toBind.getBind().getValue().getButtonCode() > 1) {
                            Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.GREEN + "Command dispatched successfully!", "Set " + toBind.getName() + "'s bind to " + toBind.getBind().getValue().getButtonName());
                        }

                        // Command failure
                        else {
                            Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.RED + "An error occurred!", "Please enter a correct bind!");
                        }

                        return 1;
                    }))

                    .executes(context -> {
                        Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.RED + "An error occurred!", "Please enter a correct bind!");
                        return 1;
                    }))

            .executes(context -> {
                Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.RED + "An error occurred!", "Please enter the name of the module!");
                return 1;
            })
        );
    }

    private static CompletableFuture<Suggestions> suggestNames(SuggestionsBuilder suggestionsBuilder) {
        Cosmos.INSTANCE.getModuleManager().getAllModules().forEach(module ->
                suggestionsBuilder.suggest(module.getName())
        );

        return suggestionsBuilder.buildFuture();
    }

}
