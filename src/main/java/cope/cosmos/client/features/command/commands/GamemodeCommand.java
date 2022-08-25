package cope.cosmos.client.features.command.commands;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.features.command.Command;
import net.minecraft.world.GameType;

/**
 * @author linustouchtips
 * @since 08/25/2022
 */
public class GamemodeCommand extends Command {
    public static GamemodeCommand INSTANCE;

    public GamemodeCommand() {
        super("Gamemode", "Sets the player's gamemode");
        INSTANCE = this;
    }

    @Override
    public void onExecute(String[] args) {

        if (args.length == 1) {

            // survival
            if (args[0].equalsIgnoreCase("Survival") || args[0].equalsIgnoreCase("S")) {

                // set gamemode
                mc.playerController.setGameType(GameType.SURVIVAL);
                getCosmos().getChatManager().sendClientMessage("Gamemode is now " + ChatFormatting.GRAY + "Survival");
            }

            // adventure
            else if (args[0].equalsIgnoreCase("Adventure") || args[0].equalsIgnoreCase("A")) {

                // set gamemode
                mc.playerController.setGameType(GameType.ADVENTURE);
                getCosmos().getChatManager().sendClientMessage("Gamemode is now " + ChatFormatting.GRAY + "Adventure");
            }

            // creative
            else if (args[0].equalsIgnoreCase("Creative") || args[0].equalsIgnoreCase("C") || args[0].equalsIgnoreCase("OP")) {

                // set gamemode
                mc.playerController.setGameType(GameType.CREATIVE);
                getCosmos().getChatManager().sendClientMessage("Gamemode is now " + ChatFormatting.GRAY + "Creative");
            }

            // creative
            else if (args[0].equalsIgnoreCase("Spectate") || args[0].equalsIgnoreCase("Spectator")) {

                // set gamemode
                mc.playerController.setGameType(GameType.SPECTATOR);
                getCosmos().getChatManager().sendClientMessage("Gamemode is now " + ChatFormatting.GRAY + "Spectator");
            }

            else {

                // unrecognized gamemode exception
                getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Gamemode!", ChatFormatting.RED + "Please enter a valid gamemode.");
            }
        }

        else {

            // unrecognized arguments exception
            getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Arguments!", ChatFormatting.RED + "Please enter the correct arguments for this command.");
        }
    }

    @Override
    public String getUseCase() {
        return "<gamemode>";
    }

    @Override
    public int getArgSize() {
        return 1;
    }
}
