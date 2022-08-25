package cope.cosmos.client.features.command.commands;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.features.command.Command;

import java.util.List;

/**
 * @author linustouchtips
 * @since 08/24/2022
 */
public class ConfigCommand extends Command {
    public static ConfigCommand INSTANCE;

    public ConfigCommand() {
        super("Config", new String[] {"preset", "configuration"}, "Saves and loads configurations");
        INSTANCE = this;
    }

    @Override
    public void onExecute(String[] args) {

        if (args.length == 2) {

            // config saving
            if (args[0].equalsIgnoreCase("Save")) {
                getCosmos().getConfigManager().createPreset(args[1]);
                getCosmos().getChatManager().sendHoverableMessage("Saved current config", "Saved config as " + args[1] + ".");
            }

            // config loading
            else if (args[0].equalsIgnoreCase("Load")) {

                // check if the config exists
                boolean exists = getCosmos().getConfigManager().getPresets().contains(args[1]);

                // load if exists
                if (exists) {
                    getCosmos().getConfigManager().loadPreset(args[1]);
                    getCosmos().getChatManager().sendHoverableMessage("Loaded config " + ChatFormatting.GRAY + args[1], "Loaded current config");
                }

                else {

                    // unrecognized action
                    getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Config Name!", ChatFormatting.RED + "Please enter a valid config name.");
                }
            }

            else {

                // unrecognized action
                getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Action!", ChatFormatting.RED + "Please use a valid action.");
            }
        }

        else if (args.length == 1) {

            // config listing
            if (args[0].equalsIgnoreCase("List")) {

                // list of presets
                List<String> presetList = getCosmos().getConfigManager().getPresets();

                // config list
                StringBuilder configList = new StringBuilder();

                // all presets
                for (int i = 0; i < presetList.size(); i++) {

                    // current preset
                    boolean current = presetList.get(i).equalsIgnoreCase(getCosmos().getConfigManager().getPreset());

                    // highlight
                    if (current) {
                        configList.append(ChatFormatting.GREEN);
                    }

                    // add to config list
                    configList.append(presetList.get(i));

                    // reset
                    if (current) {
                        configList.append(ChatFormatting.RESET);
                    }

                    // more presets
                    if (i < presetList.size() - 1) {
                        configList.append(", ");
                    }
                }

                getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.GRAY + "[Configs]", "List of configs");
                getCosmos().getChatManager().sendClientMessage(configList.toString());
            }

            else {

                // unrecognized action
                getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Action!", ChatFormatting.RED + "Please use a valid action.");
            }
        }

        else {

            // unrecognized arguments exception
            getCosmos().getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Arguments!", ChatFormatting.RED + "Please enter the correct arguments for this command.");
        }
    }

    @Override
    public String getUseCase() {
        return "<save/load/list> <optional:name>";
    }

    @Override
    public int getArgSize() {
        return 2;
    }
}
