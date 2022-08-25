package cope.cosmos.client.features.command.commands;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.features.command.Command;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Bind;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.string.StringFormatter;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.Arrays;

/**
 * @author linustouchtips
 * @since 08/24/2022
 */
public class SettingCommand extends Command {

    // module
    private final Module module;

    public SettingCommand(Module module) {
        super(module.getName(), module.getAliases(), "Configures the settings of " + module.getName());
        this.module = module;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onExecute(String[] args) {

        if (args.length == 2) {

            // setting to configure
            Setting<?> setting = module.getSetting(setting1 -> setting1.equals(args[0]));

            // if the given setting is valid
            if (setting != null) {

                // bind setting
                if (setting.getValue() instanceof Bind) {

                    // bind key
                    int key = Keyboard.getKeyIndex(args[1].toUpperCase());

                    // recognized key
                    if (key != Keyboard.KEY_NONE) {

                        // bind module
                        module.getBind().setValue(new Bind(key, Bind.Device.KEYBOARD));
                        getCosmos().getChatManager().sendHoverableMessage(module.getName() + " bound to " + args[1].toUpperCase() + "!", "The setting has been updated.");
                    }

                    else {

                        // unrecognized key exception
                        getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Key!", ChatFormatting.RED + "Please enter the name of key that exists.");
                    }
                }

                // boolean setting
                else if (setting.getValue() instanceof Boolean) {

                    // accepted formats
                    if (args[1].equalsIgnoreCase("True") || args[1].equalsIgnoreCase("False")) {

                        // boolean value
                        boolean value = Boolean.parseBoolean(args[1]);

                        // update setting
                        ((Setting<Boolean>) setting).setValue(value);
                        getCosmos().getChatManager().sendHoverableMessage("[" + module.getName() + "] " + ChatFormatting.GRAY + setting.getName() + ChatFormatting.RESET + " set to " + ChatFormatting.GRAY + StringFormatter.capitalise(args[1]), "The setting has been updated.");
                    }

                    else {

                        // unrecognized value exception
                        getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Value!", ChatFormatting.RED + "Please enter a boolean value.");
                    }
                }

                // color setting
                else if (setting.getValue() instanceof Color) {
                    try {

                        // hex value
                        int value = Integer.parseInt(args[1]);

                        // update setting
                        ((Setting<Color>) setting).setValue(new Color(value));
                        getCosmos().getChatManager().sendHoverableMessage("[" + module.getName() + "] " + ChatFormatting.GRAY + setting.getName() + ChatFormatting.RESET + " set to " + ChatFormatting.GRAY + args[1], "The setting has been updated.");
                    } catch (NumberFormatException exception) {

                        // unrecognized value exception
                        getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Value!", ChatFormatting.RED + "Please enter a valid hex color value.");
                    }
                }

                // mode setting
                else if (setting.getValue() instanceof Enum<?>) {

                    // search all values
                    String[] values = Arrays.stream(((Enum<?>) setting.getValue()).getClass().getEnumConstants()).map(Enum::name).toArray(String[]::new);
                    String[] formatValues = Arrays.stream(((Enum<?>) setting.getValue()).getClass().getEnumConstants()).map(StringFormatter::formatEnum).toArray(String[]::new);

                    // find enum value
                    int index = -1;
                    for (int i = 0; i < formatValues.length; i++) {
                        if (formatValues[i].equalsIgnoreCase(args[1])) {
                            index = i;
                            break;
                        }
                    }

                    if (index != -1) {

                        // use value index
                        Enum<?> value = Enum.valueOf(((Enum<?>) setting.getValue()).getClass(), values[index]);

                        // update setting
                        ((Setting<Enum<?>>) setting).setValue(value);
                        getCosmos().getChatManager().sendHoverableMessage("[" + module.getName() + "] " + ChatFormatting.GRAY + setting.getName() + ChatFormatting.RESET + " set to " + ChatFormatting.GRAY + StringFormatter.formatEnum((Enum<?>) setting.getValue()), "The setting has been updated.");
                    }

                    else {

                        // unrecognized value exception
                        getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Value!", ChatFormatting.RED + "Please enter a mode value that exists in " + setting.getValue() + ".");
                    }
                }

                // number setting
                else if (setting.getValue() instanceof Number) {
                    try {

                        // double setting
                        if (setting.getValue() instanceof Double) {

                            // double value
                            double value = Double.parseDouble(args[1]);

                            // check if the setting is within the bounds
                            if (value <= ((Setting<Double>) setting).getMax() && value >= ((Setting<Double>) setting).getMin()) {

                                // update setting
                                ((Setting<Double>) setting).setValue(value);
                                getCosmos().getChatManager().sendHoverableMessage("[" + module.getName() + "] " + ChatFormatting.GRAY + setting.getName() + ChatFormatting.RESET + " set to " + ChatFormatting.GRAY + setting.getValue(), "The setting has been updated.");
                            }

                            else {

                                // out of bounds exception
                                getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Value is out of bounds!", ChatFormatting.RED + "Please enter a value that is within the setting bounds.");
                            }
                        }

                        // float setting
                        if (setting.getValue() instanceof Float) {

                            // float value
                            float value = Float.parseFloat(args[1]);

                            // check if the setting is within the bounds
                            if (value <= ((Setting<Float>) setting).getMax() && value >= ((Setting<Float>) setting).getMin()) {

                                // update setting
                                ((Setting<Float>) setting).setValue(value);
                                getCosmos().getChatManager().sendHoverableMessage("[" + module.getName() + "] " + ChatFormatting.GRAY + setting.getName() + ChatFormatting.RESET + " set to " + ChatFormatting.GRAY + setting.getValue(), "The setting has been updated.");
                            }

                            else {

                                // out of bounds exception
                                getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Value is out of bounds!", ChatFormatting.RED + "Please enter a value that is within the setting bounds.");
                            }
                        }

                        // integer setting
                        if (setting.getValue() instanceof Integer) {

                            // float value
                            int value = Integer.parseInt(args[1]);

                            // check if the setting is within the bounds
                            if (value <= ((Setting<Integer>) setting).getMax() && value >= ((Setting<Integer>) setting).getMin()) {

                                // update setting
                                ((Setting<Integer>) setting).setValue(value);
                                getCosmos().getChatManager().sendHoverableMessage("[" + module.getName() + "] " + ChatFormatting.GRAY + setting.getName() + ChatFormatting.RESET + " set to " + ChatFormatting.GRAY + setting.getValue(), "The setting has been updated.");
                            }

                            else {

                                // out of bounds exception
                                getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Value is out of bounds!", ChatFormatting.RED + "Please enter a value that is within the setting bounds.");
                            }
                        }
                    } catch (NumberFormatException | NullPointerException exception) {

                        // unrecognized value exception
                        getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Value!", ChatFormatting.RED + "Please enter a number value.");
                    }
                }

                else {

                    // unrecognized value format exception
                    getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Value Format!", ChatFormatting.RED + "Please enter a value format that corresponds to a setting.");
                }
            }

            else {

                // drawn setting
                if (args[0].equalsIgnoreCase("Drawn")) {

                    // accepted formats
                    if (args[1].equalsIgnoreCase("True") || args[1].equalsIgnoreCase("False")) {

                        // boolean value
                        boolean value = Boolean.parseBoolean(args[1]);

                        // update setting
                        module.setDrawn(value);
                        getCosmos().getChatManager().sendHoverableMessage("[" + module.getName() + "] " + ChatFormatting.GRAY + "Drawn" + ChatFormatting.RESET + " set to " + ChatFormatting.GRAY + StringFormatter.capitalise(args[1]), "The setting has been updated.");
                    }

                    else {

                        // unrecognized value exception
                        getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Value!", ChatFormatting.RED + "Please enter a boolean value.");
                    }
                }

                else {

                    // unrecognized setting exception
                    getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Setting!", ChatFormatting.RED + "Please enter a setting that exists in " + module.getName() + ".");
                }
            }
        }

        else {

            // unrecognized arguments exception
            getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Arguments!", ChatFormatting.RED + "Please enter the correct arguments for this command.");
        }
    }

    @Override
    public String getUseCase() {
        return "<setting> <value>";
    }

    @Override
    public int getArgSize() {
        return 2;
    }

    /**
     * Gets the module
     * @return The module
     */
    public Module getModule() {
        return module;
    }
}
