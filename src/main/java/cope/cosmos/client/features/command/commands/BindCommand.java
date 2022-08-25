package cope.cosmos.client.features.command.commands;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.features.command.Command;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Bind;
import cope.cosmos.client.features.setting.Bind.Device;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * @author linustouchtips
 * @since 08/23/2020
 */
public class BindCommand extends Command {
    public static BindCommand INSTANCE;

    public BindCommand() {
        super("Bind", new String[] {"b", "Key", "KeyBind"}, "Binds a feature to a given key");
        INSTANCE = this;
    }

    @Override
    public void onExecute(String[] args) {

        // specified keyboard/mouse
        if (args.length == 3) {

            // module to bind
            Module module = getCosmos().getModuleManager().getModule(module1 -> module1.equals(args[0]));

            // if the given module is valid
            if (module != null) {

                // keyboard bind
                if (args[2].equalsIgnoreCase("Keyboard")) {

                    // bind key
                    int key = Keyboard.getKeyIndex(args[1].toUpperCase());

                    // recognized key
                    if (key != Keyboard.KEY_NONE) {

                        // bind module
                        module.getBind().setValue(new Bind(key, Device.KEYBOARD));
                        getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.GRAY + module.getName() + ChatFormatting.RESET + " bound to " + ChatFormatting.GRAY + args[1].toUpperCase(), "The module has been bound.");
                    }

                    else {

                        // unrecognized key exception
                        getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Key!", "Please enter the name of key that exists.");
                    }
                }

                // mouse bind
                else if (args[2].equalsIgnoreCase("Mouse") || args[2].equalsIgnoreCase("Button")) {

                    // bind button
                    int button = Mouse.getButtonIndex(args[1]);

                    // recognized button
                    if (button != -1) {

                        // bind module
                        module.getBind().setValue(new Bind(button, Device.MOUSE));
                        getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.GRAY + module.getName() + ChatFormatting.RESET + " bound to " + ChatFormatting.GRAY + args[1].toUpperCase(), "The module has been bound.");
                    }

                    else {

                        // unrecognized button exception
                        getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Button!", "Please enter the name of button that exists.");
                    }
                }

                else {

                    // unrecognized key format exception
                    getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Key Format!", "Please enter the name of key format that is supported.");
                }
            }

            else {

                // unrecognized module exception
                getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Module!", "Please enter a module that exists.");
            }
        }

        // generic
        else if (args.length == 2) {

            // module to bind
            Module module = getCosmos().getModuleManager().getModule(module1 -> module1.equals(args[0]));

            // if the given module is valid
            if (module != null) {

                // bind key
                int key = Keyboard.getKeyIndex(args[1].toUpperCase());

                // recognized key
                if (key != Keyboard.KEY_NONE) {

                    // bind module
                    module.getBind().setValue(new Bind(key, Device.KEYBOARD));
                    getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.GRAY + module.getName() + ChatFormatting.RESET + " bound to " + ChatFormatting.GRAY + args[1].toUpperCase(), "The module has been bound.");
                }

                else {

                    // unrecognized key exception
                    getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Key!", ChatFormatting.RED + "Please enter the name of key that exists.");
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
        return "<module> <key/button> <optional:format>";
    }

    @Override
    public int getArgSize() {
        return 3;
    }
}
