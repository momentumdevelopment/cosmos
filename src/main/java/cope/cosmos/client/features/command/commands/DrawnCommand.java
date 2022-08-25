package cope.cosmos.client.features.command.commands;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.features.command.Command;
import cope.cosmos.client.features.modules.Module;

/**
 * @author linustouchtips
 * @since 08/24/2022
 */
public class DrawnCommand extends Command {
    public static DrawnCommand INSTANCE;

    public DrawnCommand() {
        super("Drawn", new String[] {"Drawn", "Draw", "Hide"}, "Draws or hides modules from the arraylist");
    }

    @Override
    public void onExecute(String[] args) {

        if (args.length == 2) {

            // module to bind
            Module module = getCosmos().getModuleManager().getModule(module1 -> module1.equals(args[0]));

            // if the given module is valid
            if (module != null) {

                // accepted formats
                if (args[1].equalsIgnoreCase("True") || args[1].equalsIgnoreCase("False")) {

                    // boolean value
                    boolean value = Boolean.parseBoolean(args[1]);

                    // update setting
                    module.setDrawn(value);
                    getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.GRAY + module.getName() + ChatFormatting.RESET + " is now " + ChatFormatting.GRAY + (value ? "drawn" : "hidden"), "The module has been " + (value ? "drawn" : "hidden"));
                }
            }

            else {

                // unrecognized module exception
                getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Module!", ChatFormatting.RED + "Please enter a module that exists.");
            }
        }

        else {

            // unrecognized arguments exception
            getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Arguments!", ChatFormatting.RED + "Please enter the correct arguments for this command.");
        }
    }

    @Override
    public String getUseCase() {
        return "<module> <value>";
    }

    @Override
    public int getArgSize() {
        return 2;
    }
}
