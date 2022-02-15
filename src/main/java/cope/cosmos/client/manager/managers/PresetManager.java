package cope.cosmos.client.manager.managers;

import com.moandjiezana.toml.Toml;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.client.manager.managers.SocialManager.Relationship;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author linustouchtips
 * @since 06/08/2021
 */
public class PresetManager extends Manager {

    // the current preset to save and load to
    private String currentPreset;

    // list of presets
    private final List<String> presets = new CopyOnWriteArrayList<>();

    // the client directory
    private final File mainDirectory = new File("cosmos");

    public PresetManager() {
        super("PresetManager", "Handles the client's configs - saving, loading, etc.");

        // create a default preset, if the user does not make any custom presets this will be loaded
        presets.add("default");
        currentPreset = "default";

        // load and save the default config
        load();
        save();

        // save the config when the game is closed
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            getCosmos().getPresetManager().save();
        }));
    }

    /**
     * Sets the current preset
     * @param in The new preset
     */
    public void setPreset(String in) {
        if (presets.contains(in)) {
            if (!currentPreset.equals(in)) {
                currentPreset = in;
            }

            // load the new preset
            load();
        }
    }

    /**
     * Creates a new preset
     * @param in The new preset
     */
    public void createPreset(String in) {
        presets.add(in);

        // set the current preset temp
        String previousPreset = currentPreset;
        currentPreset = in;

        // create the directories
        writeDirectories();
        save();

        // reset
        currentPreset = previousPreset;
    }

    /**
     * Removes a preset from the list
     * @param in The preset to remove
     */
    public void removePreset(String in) {
        if (currentPreset.equals(in)) {
            currentPreset = "default";
            load();
        }

        presets.remove(in);
    }

    /**
     * Loads the client configuration
     */
    public void load() {
        writeDirectories();
        loadInfo();
        loadModules();
        loadSocial();
        // loadGUI();
        // loadAlts();
    }

    /**
     * Saves the client configuration
     */
    public void save() {
        saveInfo();
        saveModules();
        saveSocial();
        // saveGUI();
        // saveAlts();
    }

    /**
     * Writes the needed directories for the configuration files
     */
    public void writeDirectories() {
        if (!mainDirectory.exists()) {
            mainDirectory.mkdirs();
        }

        presets.forEach(preset -> {
            File presetDirectory = new File("cosmos/" + preset);

            if (!presetDirectory.exists()) {
                presetDirectory.mkdirs();
            }
        });
    }

    /**
     * Writes the module's configuration to a TOML file
     */
    public void saveModules() {
        try {
            // the file writer
            OutputStreamWriter moduleOutputStreamWriter = new OutputStreamWriter(new FileOutputStream(mainDirectory.getName() + "/" + currentPreset + "/modules.toml"), StandardCharsets.UTF_8);

            // the output string
            StringBuilder outputTOML = new StringBuilder();

            getCosmos().getModuleManager().getAllModules().forEach(module -> {
                if (module != null) {
                    try {
                        // writes the enabled state, drawn state, and bind
                        outputTOML.append("[").append(module.getName()).append("]").append("\r\n");
                        outputTOML.append("Enabled = ").append(module.isEnabled()).append("\r\n");
                        outputTOML.append("Drawn = ").append(module.isDrawn()).append("\r\n");
                        outputTOML.append("Bind = ").append(module.getKey()).append("\r\n");

                        module.getSettings().forEach(setting -> {
                            if (setting != null) {
                                // add the parent identifier if the setting is a subsetting
                                {
                                    if (setting.hasParent()) {
                                        outputTOML.append(setting.getParentSetting().getName()).append("-").append(setting.getName());
                                    }

                                    else {
                                        outputTOML.append(setting.getName());
                                    }
                                }

                                outputTOML.append(" = ");

                                // write the setting value
                                {
                                    if (setting.getValue() instanceof Enum<?>) {
                                        outputTOML.append('"').append(setting.getValue().toString()).append('"');
                                    }

                                    else if (setting.getValue() instanceof Color) {
                                        outputTOML.append(((Color) setting.getValue()).getRGB());
                                    }

                                    else {
                                        outputTOML.append(setting.getValue());
                                    }
                                }

                                // put the next setting on a new line
                                outputTOML.append("\r\n");
                            }
                        });

                        outputTOML.append("\r\n");
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            });

            moduleOutputStreamWriter.write(outputTOML.toString());
            moduleOutputStreamWriter.close();

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Loads the client configuration from the TOML file
     */
    @SuppressWarnings("unchecked")
    public void loadModules() {
        try {
            // the stream from the configuration file
            InputStream inputStream = Files.newInputStream(Paths.get(mainDirectory.getName() + "/" + currentPreset + "/modules.toml"));

            // the toml input from the file
            Toml inputTOML = new Toml().read(inputStream);

            if (inputTOML != null) {
                getCosmos().getModuleManager().getAllModules().forEach(module -> {
                    if (module != null) {
                        try {
                            // set the enabled state
                            if (inputTOML.getBoolean(module.getName() + ".Enabled") != null) {
                                if (inputTOML.getBoolean(module.getName() + ".Enabled", false)) {
                                    module.enable(true);
                                    module.getAnimation().setState(true);
                                }
                            }

                            // set the drawn state
                            if (inputTOML.getBoolean(module.getName() + ".Drawn") != null) {
                                module.setDrawn(inputTOML.getBoolean(module.getName() + ".Drawn", true));
                            }

                            // set the keybind
                            if (inputTOML.getLong(module.getName() + ".Bind") != null) {
                                int key = inputTOML.getLong(module.getName() + ".Bind", 0L).intValue();
                                module.setKey(key);
                            }

                            // set the setting values
                            module.getSettings().forEach(setting -> {
                                if (setting != null) {
                                    try {
                                        // the setting identifier in the TOML file
                                        String identifier;

                                        {
                                            if (setting.hasParent()) {
                                                identifier = module.getName() + "." + setting.getParentSetting().getName() + "-" + setting.getName();
                                            }

                                            else {
                                                identifier = module.getName() + "." + setting.getName();
                                            }
                                        }

                                        // set the value based on the setting data type
                                        if (setting.getValue() instanceof Boolean) {
                                            if (inputTOML.getBoolean(identifier) != null) {
                                                boolean value = inputTOML.getBoolean(identifier, false);
                                                ((Setting<Boolean>) setting).setValue(value);
                                            }
                                        }

                                        else if (setting.getValue() instanceof Double) {
                                           if (inputTOML.getDouble(identifier) != null) {
                                                double value = inputTOML.getDouble(identifier, 0.0);
                                                ((Setting<Double>) setting).setValue(value);
                                           }
                                        }

                                        else if (setting.getValue() instanceof Float) {
                                            if (inputTOML.getDouble(identifier) != null) {
                                                float value = inputTOML.getDouble(identifier, 0.0).floatValue();
                                                ((Setting<Float>) setting).setValue(value);
                                            }
                                        }

                                        else if (setting.getValue() instanceof Enum<?>) {
                                            if (inputTOML.getString(identifier) != null) {
                                                Enum<?> value = Enum.valueOf(((Enum<?>) setting.getValue()).getClass(), inputTOML.getString(identifier, ""));
                                                ((Setting<Enum<?>>) setting).setValue(value);
                                            }
                                        }

                                        else if (setting.getValue() instanceof Color) {
                                            if (inputTOML.getLong(identifier) != null) {
                                                Color value = new Color(inputTOML.getLong(identifier, -1L).intValue(), true);
                                                ((Setting<Color>) setting).setValue(value);
                                           }
                                        }
                                    } catch (Exception exception) {
                                        exception.printStackTrace();
                                    }
                                }
                            });
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }
                });
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Writes the user's socials to a TOML file
     */
    public void saveSocial() {
        try {
            // the file writer
            OutputStreamWriter socialOutputStreamWriter = new OutputStreamWriter(new FileOutputStream(mainDirectory.getName() + "/social.toml"), StandardCharsets.UTF_8);

            // the output string
            StringBuilder outputTOML = new StringBuilder();

            try {
                outputTOML.append("[Social]").append("\r\n");

                // add all socials
                outputTOML.append("Friends").append(" = ").append("[");
                for (Map.Entry<String, Relationship> social : getCosmos().getSocialManager().getSocials().entrySet()) {
                    outputTOML.append('"').append(social.getKey()).append('"').append(", ");
                }

                outputTOML.append("]").append("\r\n");
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            // write the socials to a TOML file
            socialOutputStreamWriter.write(outputTOML.toString());
            socialOutputStreamWriter.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Loads the client socials from the TOML file
     */
    public void loadSocial() {
        try {
            // the stream from the social file
            InputStream inputStream = Files.newInputStream(Paths.get(mainDirectory.getName() + "/social.toml"));

            // the TOML input from the file
            Toml inputTOML = new Toml().read(inputStream);

            // add the socials from our configuration file to our socials list
            if (inputTOML != null) {
                try {
                    if (inputTOML.getList("Social.Friends") != null) {

                        for (Object o : inputTOML.getList("Social.Friends", new CopyOnWriteArrayList <>())) {
                            String friend = (String) o;
                            getCosmos().getSocialManager().addSocial(friend, Relationship.FRIEND);
                        }
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Writes the client info to a TOML file
     */
    public void saveInfo() {
        try {
            // the file writer
            OutputStreamWriter infoOutputStreamWriter = new OutputStreamWriter(new FileOutputStream(mainDirectory.getName() + "/info.toml"), StandardCharsets.UTF_8);

            // the output string
            StringBuilder outputTOML = new StringBuilder();

            try {
                outputTOML.append("[Info]").append("\r\n");

                // write our client info
                outputTOML.append("Setup = ").append(Cosmos.SETUP).append("\r\n");
                outputTOML.append("Prefix = ").append('"').append(Cosmos.PREFIX).append('"').append("\r\n");
                outputTOML.append("Preset = ").append('"').append(currentPreset).append('"').append("\r\n");
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            infoOutputStreamWriter.write(outputTOML.toString());
            infoOutputStreamWriter.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Loads the client info from the TOML file
     */
    public void loadInfo() {
        try {
            // the stream from the info file
            InputStream inputStream = Files.newInputStream(Paths.get(mainDirectory.getName() + "/info.toml"));

            // the TOML input from the file
            Toml inputTOML = new Toml().read(inputStream);

            try {
                // set the client's info
                if (inputTOML.getBoolean("Info.Setup") != null) {
                    Cosmos.SETUP = inputTOML.getBoolean("Info.Setup", false);
                }

                if (inputTOML.getString("Info.Prefix") != null) {
                    Cosmos.PREFIX = inputTOML.getString("Info.Prefix", "*");
                }

                if (inputTOML.getString("Info.Preset") != null) {
                    currentPreset = inputTOML.getString("Info.Preset", "default");
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    // TODO: fix alt/GUI saving & loading

    /*
    public void saveAlts() {
        try {
            OutputStreamWriter altOutputStreamWriter = new OutputStreamWriter(new FileOutputStream(mainDirectory.getName() + "/alts.toml"), StandardCharsets.UTF_8);
            StringBuilder outputTOML = new StringBuilder();

            int altList = 0;
            for (AltEntry altEntry : AltManager.getAlts()) {
                outputTOML.append("[").append(altList).append("]").append("\r\n");
                outputTOML.append("Name = ").append('"').append(altEntry.getName()).append('"').append("\r\n");
                outputTOML.append("Email = ").append('"').append(altEntry.getEmail()).append('"').append("\r\n");
                outputTOML.append("Password = ").append('"').append(altEntry.getPassword()).append('"').append("\r\n");

                outputTOML.append("\r\n");
                altList++;
            }

            altOutputStreamWriter.write(outputTOML.toString());
            altOutputStreamWriter.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void loadAlts() {
        try {
            InputStream inputStream = Files.newInputStream(Paths.get(mainDirectory.getName() + "/alts.toml"));
            Toml inputTOML = new Toml().read(inputStream);

            for (int i = 0; i < 20; i++) {
                if (inputTOML.getString(i + ".Email") != null)
                    AltManager.getAlts().add(new AltEntry(inputTOML.getString(i + ".Email"), inputTOML.getString(i + ".Password")));
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }


    public void saveGUI() {
        try {
            // the file writer
            OutputStreamWriter guiOutputStreamWriter = new OutputStreamWriter(new FileOutputStream(mainDirectory.getName() + "/gui.toml"), StandardCharsets.UTF_8);

            // the output string
            StringBuilder outputTOML = new StringBuilder();

            try {
                // write the window properties
                getCosmos().getWindowGUI().getManager().getPinnedWindows().forEach(window -> {
                    outputTOML.append("[").append(window.getName()).append("]").append("\r\n");
                    outputTOML.append("X = ").append(window.getPosition().x).append("\r\n");
                    outputTOML.append("Y = ").append(window.getPosition().y).append("\r\n");
                    outputTOML.append("Height = ").append(window.getHeight()).append("\r\n");
                    outputTOML.append("Width = ").append(window.getWidth()).append("\r\n");
                });
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            // write to a TOML file
            guiOutputStreamWriter.write(outputTOML.toString());
            guiOutputStreamWriter.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void loadGUI() {
        try {
            // the stream from the GUI file
            InputStream inputStream = Files.newInputStream(Paths.get(mainDirectory.getName() + "/gui.toml"));

            // the TOML input from the file
            Toml inputTOML = new Toml().read(inputStream);


        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    */

    /**
     * Gets the client configuration's current preset
     * @return The current preset
     */
    public String getCurrentPreset() {
        return currentPreset;
    }
}
