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
import net.minecraft.entity.player.EntityPlayer;

import java.util.concurrent.CompletableFuture;

/**
 * @author linustouchtips
 * @since 07/21/2021
 */
public class FriendCommand extends Command {
    public FriendCommand() {
        super("Friend", "Updates your friends list", LiteralArgumentBuilder.literal("friend")
                .then(RequiredArgumentBuilder.argument("action", StringArgumentType.string()).suggests((context, builder) -> suggestActions(builder)).then(RequiredArgumentBuilder.argument("name", StringArgumentType.string()).suggests((context, builder) -> suggestNames(builder))
                    .executes(context -> {

                        // friend name
                        String friend = StringArgumentType.getString(context, "name");

                        // add friend
                        if (StringArgumentType.getString(context, "action").equals("add")) {
                            Cosmos.INSTANCE.getSocialManager().addSocial(friend, Relationship.FRIEND);

                            // command success
                            Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.GREEN + "Command dispatched successfully!", "Added friend with name " + friend);

                            // send a message to notify the friended player
                            if (!mc.player.getName().equalsIgnoreCase(friend)) {
                                Cosmos.INSTANCE.getChatManager().sendChatMessage("/w " + friend + " I just added you as a friend on Cosmos!");
                            }
                        }

                        // remove friend
                        else if (StringArgumentType.getString(context, "action").equals("remove")) {
                            Cosmos.INSTANCE.getSocialManager().removeSocial(friend);

                            // command success
                            Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.GREEN + "Command dispatched successfully!", "Removed friend with name " + friend);
                        }

                        return 1;
                    })
                )

                // no name
                .executes(context -> {
                    Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.RED + "An error occurred!", "Please enter the name of the person to friend!");
                    return 1;
                }))

                // no action
                .executes(context -> {
                    Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.RED + "An error occurred!", "Please enter the correct action, was expecting add or remove!");
                    return 1;
                })
        );
    }

    /**
     * Suggests names to be added as friends
     * @param suggestionsBuilder Builder to make suggestions
     * @return Suggestions for names
     */
    private static CompletableFuture<Suggestions> suggestNames(SuggestionsBuilder suggestionsBuilder) {

        // all players in world
        for (EntityPlayer entityPlayer : mc.world.playerEntities) {

            // make sure they are not already a friends
            if (Cosmos.INSTANCE.getSocialManager().getSocial(entityPlayer.getName()).equals(Relationship.FRIEND)) {
                continue;
            }

            suggestionsBuilder.suggest(entityPlayer.getName());
        }

        return suggestionsBuilder.buildFuture();
    }

    /**
     * Suggests actions for the command
     * @param suggestionsBuilder Builder to make suggestions
     * @return Suggestions for actions
     */
    private static CompletableFuture<Suggestions> suggestActions(SuggestionsBuilder suggestionsBuilder) {
        suggestionsBuilder.suggest("add");
        suggestionsBuilder.suggest("remove");
        return suggestionsBuilder.buildFuture();
    }
}
