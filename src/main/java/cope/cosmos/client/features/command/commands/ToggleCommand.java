package cope.cosmos.client.features.command.commands;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.features.command.Command;
import cope.cosmos.client.features.modules.Module;

/**
 * @author linustouchtips
 * @since 08/25/2022
 */
public class ToggleCommand extends Command {
    public static ToggleCommand INSTANCE;

    public ToggleCommand() {
        super("Toggle", new String[]{"t", "Enable"}, "Toggles a module");
        INSTANCE = this;
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
                    if (value) {
                        module.enable(true);
                    }

                    else {
                        module.disable(true);
                    }

                    getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.GRAY + module.getName() + ChatFormatting.RESET + " is now " + (module.isEnabled() ? ChatFormatting.GREEN + "enabled" : ChatFormatting.RED + "disabled") + ChatFormatting.RESET + "!", "The module has been " + (module.isEnabled() ? ChatFormatting.GREEN + "enabled" : ChatFormatting.RED + "disabled") + ChatFormatting.RESET + ".");
                }
            }

            else {

                // unrecognized module exception
                getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Module!", ChatFormatting.RED + "Please enter a module that exists.");
            }
        }

        else if (args.length == 1) {

            // module to bind
            Module module = getCosmos().getModuleManager().getModule(module1 -> module1.equals(args[0]));

            // if the given module is valid
            if (module != null) {

                // update setting
                module.toggle();
                getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.GRAY + module.getName() + ChatFormatting.RESET + " is now " + (module.isEnabled() ? ChatFormatting.GREEN + "enabled" : ChatFormatting.RED + "disabled") + ChatFormatting.RESET + "!", "The module has been " + (module.isEnabled() ? ChatFormatting.GREEN + "enabled" : ChatFormatting.RED + "disabled") + ChatFormatting.RESET + ".");
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
        return "<module> <optional:value>";
    }

    @Override
    public int getArgSize() {
        return 2;
    }
}
