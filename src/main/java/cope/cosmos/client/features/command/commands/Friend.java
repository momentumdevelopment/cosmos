package cope.cosmos.client.features.command.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.features.command.Command;
import cope.cosmos.client.manager.managers.SocialManager.Relationship;
import cope.cosmos.util.client.ChatUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketChatMessage;

import java.util.concurrent.CompletableFuture;

public class Friend extends Command {
    public Friend() {
        super("Friend", "Updates your friends list", LiteralArgumentBuilder.literal("friend")
                .then(RequiredArgumentBuilder.argument("action", StringArgumentType.string()).suggests((context, builder) -> suggestActions(builder)).then(RequiredArgumentBuilder.argument("name", StringArgumentType.string()).suggests((context, builder) -> suggestNames(builder))
                    .executes(context -> {
                        if (StringArgumentType.getString(context, "action").equals("add")) {
                            Cosmos.INSTANCE.getSocialManager().addSocial(StringArgumentType.getString(context, "name"), Relationship.FRIEND);
                            Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.GREEN + "Command dispatched successfully!", "Added friend with name " + StringArgumentType.getString(context, "name"));
                            Cosmos.INSTANCE.getChatManager().sendChatMessage("/w " + StringArgumentType.getString(context, "name") + " I just added you as a friend on Cosmos!");
                        }

                        else if (StringArgumentType.getString(context, "action").equals("remove")) {
                            Cosmos.INSTANCE.getSocialManager().removeSocial(StringArgumentType.getString(context, "name"));
                            Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.GREEN + "Command dispatched successfully!", "Removed friend with name " + StringArgumentType.getString(context, "name"));
                        }

                        return 1;
                    })
                )

                .executes(context -> {
                    Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.RED + "An error occured!", "Please enter the name of the person to friend!");
                    return 1;
                }))

                .executes(context -> {
                    Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.RED + "An error occured!", "Please enter the correct action, was expecting add or remove!");
                    return 1;
                })
        );
    }

    private static CompletableFuture<Suggestions> suggestNames(SuggestionsBuilder suggestionsBuilder) {
        for (EntityPlayer entityPlayer : mc.world.playerEntities) {
            suggestionsBuilder.suggest(entityPlayer.getName());
        }

        return suggestionsBuilder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestActions(SuggestionsBuilder suggestionsBuilder) {
        suggestionsBuilder.suggest("add");
        suggestionsBuilder.suggest("remove");
        return suggestionsBuilder.buildFuture();
    }
}
