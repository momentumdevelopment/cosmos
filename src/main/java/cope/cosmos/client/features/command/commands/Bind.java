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
import cope.cosmos.client.manager.managers.SocialManager;
import org.lwjgl.input.Keyboard;

import java.util.concurrent.CompletableFuture;

/**
 * @author Wolfsurge, whoever wrote the Friend command
 */
public class Bind extends Command {

    public Bind() {
        super("Bind", "Binds a module to the given keybind", LiteralArgumentBuilder.literal("bind")
            .then(RequiredArgumentBuilder.argument("module", StringArgumentType.string()).suggests((context, builder) -> suggestNames(builder)).then(RequiredArgumentBuilder.argument("key", StringArgumentType.string())
                            .executes(context -> {
                                Module toBind = Cosmos.INSTANCE.getModuleManager().getModule(module -> module.getName().equalsIgnoreCase(StringArgumentType.getString(context, "module")));

                                Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.GREEN + "Command dispatched successfully!", "Set module's bind to " + StringArgumentType.getString(context, "key").toUpperCase());

                                // Set key
                                toBind.setKey(Keyboard.getKeyIndex(StringArgumentType.getString(context, "key").toUpperCase()));

                                return 1;
                            })
                    )

                    .executes(context -> {
                        Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.RED + "An error occured!", "Please enter a correct bind!");
                        return 1;
                    }))

            .executes(context -> {
                Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.RED + "An error occured!", "Please enter the name of the module!");
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
