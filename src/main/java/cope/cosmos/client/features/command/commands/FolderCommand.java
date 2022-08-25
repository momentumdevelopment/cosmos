package cope.cosmos.client.features.command.commands;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.features.command.Command;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * @author linustouchtips
 * @since 08/24/2022
 */
public class FolderCommand extends Command {
    public static FolderCommand INSTANCE;

    public FolderCommand() {
        super("Folder", new String[]{"OpenFolder", "File", "OpenFile"}, "Opens the client folder");
        INSTANCE = this;
    }

    @Override
    public void onExecute(String[] args) {

        if (args.length == 0) {

            // open file
            try {
                Desktop.getDesktop().open(new File("cosmos"));
            } catch (IOException exception) {

                // unrecognized file exception
                getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized File!", ChatFormatting.RED + "Client folder was not found!");
            }
        }

        else {

            // unrecognized arguments exception
            getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Arguments!", ChatFormatting.RED + "Please enter the correct arguments for this command.");
        }
    }

    @Override
    public String getUseCase() {
        return "";
    }

    @Override
    public int getArgSize() {
        return 0;
    }
}
