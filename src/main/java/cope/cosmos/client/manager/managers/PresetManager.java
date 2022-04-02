package cope.cosmos.client.manager.managers;

import com.moandjiezana.toml.Toml;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.Cosmos.ClientType;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.client.manager.managers.SocialManager.Relationship;
import cope.cosmos.client.ui.altgui.Alt;
import cope.cosmos.client.ui.altgui.AltEntry;
import cope.cosmos.client.ui.altgui.AltManagerGUI;
import net.minecraft.util.math.Vec2f;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author linustouchtips, Wolfsurge
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
        loadAlts();
        loadGUI();
    }

    /**
     * Saves the client configuration
     */
    public void save() {
        saveInfo();
        saveModules();
        saveSocial();
        saveAlts();
        saveGUI();
    }

    /**
     * Writes the needed directories for the configuration files
     */
    public void writeDirectories() {
        if (!mainDirectory.exists()) {
            boolean success = mainDirectory.mkdirs();

            // notify user
            if (success) {
                System.out.println("[Cosmos] Main Directory was created successfully!");
            }
        }

        presets.forEach(preset -> {
            File presetDirectory = new File("cosmos/" + preset);

            if (!presetDirectory.exists()) {
                boolean success = presetDirectory.mkdirs();

                // notify user
                if (success) {
                    System.out.println("[Cosmos] " + preset + " Directory was created successfully!");
                }
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

                        // notify user
                        if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                            System.out.println("[Cosmos] " + module.getName() + " was saved successfully!");
                        }

                    } catch (Exception exception) {

                        // print exception if development environment
                        if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                            exception.printStackTrace();
                        }
                    }
                }
            });

            moduleOutputStreamWriter.write(outputTOML.toString());
            moduleOutputStreamWriter.close();

        } catch (IOException exception) {

            // print exception if development environment
            if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                exception.printStackTrace();
            }
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
                                boolean drawn = inputTOML.getBoolean(module.getName() + ".Drawn", true);
                                module.setDrawn(drawn);
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

                            // notify user
                            if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                                System.out.println("[Cosmos] " + module.getName() + " was loaded successfully!");
                            }

                        } catch (Exception exception) {

                            // print exception if development environment
                            if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                                exception.printStackTrace();
                            }
                        }
                    }
                });
            }
        } catch (IOException exception) {

            // print exception if development environment
            if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                exception.printStackTrace();
            }
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

                // print exception if development environment
                if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                    exception.printStackTrace();
                }
            }

            // write the socials to a TOML file
            socialOutputStreamWriter.write(outputTOML.toString());
            socialOutputStreamWriter.close();

            // notify user
            if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                System.out.println("[Cosmos] Socials were saved successfully!");
            }

        } catch (IOException exception) {

            // print exception if development environment
            if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                exception.printStackTrace();
            }
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

                    // print exception if development environment
                    if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                        exception.printStackTrace();
                    }
                }
            }

            // notify user
            if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                System.out.println("[Cosmos] Socials were loaded successfully!");
            }

        } catch (IOException exception) {

            // print exception if development environment
            if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                exception.printStackTrace();
            }
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

            // notify user
            if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                System.out.println("[Cosmos] Info was saved successfully!");
            }

        } catch (IOException exception) {

            // print exception if development environment
            if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                exception.printStackTrace();
            }
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

                // print exception if development environment
                if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                    exception.printStackTrace();
                }
            }

            // notify user
            if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                System.out.println("[Cosmos] Info was loaded successfully!");
            }

        } catch (IOException exception) {

            // print exception if development environment
            if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                exception.printStackTrace();
            }
        }
    }

    /**
     * Saves the alt accounts to alts.toml
     */
    public void saveAlts() {
        try {
            // File writer
            OutputStreamWriter altOutputStreamWriter = new OutputStreamWriter(new FileOutputStream(mainDirectory.getName() + "/alts.toml"), StandardCharsets.UTF_8);

            // Output
            StringBuilder output = new StringBuilder();

            try {
                output.append("[Alts]").append("\r\n");

                output.append("Alts").append(" = ").append("[");

                // Add the alt's info, in the order: email:password:type
                for (AltEntry altEntry : getCosmos().getAltManager().getAltEntries()) {
                    output.append('"').append(altEntry.getAlt().getLogin()).append(":").append(altEntry.getAlt().getPassword()).append(":").append(altEntry.getAlt().getAltType().name()).append('"').append(",");
                }

                output.append("]").append("\r\n");

            } catch (Exception exception) {

                // print exception if development environment
                if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                    exception.printStackTrace();
                }
            }

            // Write the info
            altOutputStreamWriter.write(output.toString());
            altOutputStreamWriter.close();

            // notify user
            if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                System.out.println("[Cosmos] Alts were saved successfully!");
            }

        } catch (IOException exception) {

            // print exception if development environment
            if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                exception.printStackTrace();
            }
        }
    }

    /**
     * Loads the alts from alts.toml
     */
    public void loadAlts() {
        try {
            // The stream from alts file
            InputStream inputStream = Files.newInputStream(Paths.get(mainDirectory.getName() + "/alts.toml"));

            // Input TOML
            Toml input = new Toml().read(inputStream);

            if (input != null) {
                try {
                    if (input.getList("Alts.Alts") != null) {
                        input.getList("Alts.Alts").forEach(object -> {
                            // 0 = Email, 1 = Password, 2 = Type
                            String[] altInfo = ((String) object).split(":");

                            // Get type based on text
                            Alt.AltType type = Alt.AltType.valueOf(altInfo[2]);

                            // Loading alt
                            Alt alt = new Alt(altInfo[0], altInfo[1], type);

                            // Add alt to list
                            getCosmos().getAltManager().getAltEntries().add(new AltEntry(alt, AltManagerGUI.altEntryOffset));

                            // Add to offset
                            AltManagerGUI.altEntryOffset += 32;
                        });
                    }

                } catch (Exception exception) {

                    // print exception if development environment
                    if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                        exception.printStackTrace();
                    }
                }
            }

            // notify user
            if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                System.out.println("[Cosmos] Alts were loaded successfully!");
            }

        } catch (IOException exception) {

            // print exception if development environment
            if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                exception.printStackTrace();
            }
        }
    }

    public void saveGUI() {
        try {
            // File writer
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(mainDirectory.getName() + "/gui.toml"), StandardCharsets.UTF_8);

            // Output string
            StringBuilder output = new StringBuilder();

            try {
                // Add frame info to output
                getCosmos().getClickGUI().getCategoryFrameComponents().forEach(component -> {
                    output.append("[").append(component.getValue().name()).append("]").append("\r\n");
                    output.append("X = ").append(component.getPosition().x).append("\r\n");
                    output.append("Y = ").append(component.getPosition().y).append("\r\n");
                    output.append("Height = ").append(component.getHeight()).append("\r\n");
                    output.append("\r\n");
                });
            } catch (Exception exception) {

                // print exception if development environment
                if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                    exception.printStackTrace();
                }
            }

            // Write and close
            writer.write(output.toString());
            writer.close();

            // notify user
            if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                System.out.println("[Cosmos] GUI was saved successfully!");
            }

        } catch (Exception exception) {

            // print exception if development environment
            if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                exception.printStackTrace();
            }
        }
    }

    public void loadGUI() {
        try {
            // The stream from gui file
            InputStream inputStream = Files.newInputStream(Paths.get(mainDirectory.getName() + "/gui.toml"));

            // Input TOML
            Toml toml = new Toml().read(inputStream);

            if (toml != null) {
                // Set frame values
                getCosmos().getClickGUI().getCategoryFrameComponents().forEach(component -> {
                    // Set X and Y
                    if (toml.getDouble(component.getValue().name() + ".X") != null && toml.getDouble(component.getValue().name() + ".Y") != null) {
                        component.setPosition(new Vec2f(toml.getDouble(component.getValue().name() + ".X").floatValue(), toml.getDouble(component.getValue().name() + ".Y").floatValue()));
                    }

                    // Set height
                    if (toml.getDouble(component.getValue().name() + ".Height") != null) {
                        component.setHeight(toml.getDouble(component.getValue().name() + ".Height").floatValue());
                    }
                });
            }

            // notify user
            if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                System.out.println("[Cosmos] GUI was loaded successfully!");
            }

        } catch (Exception exception) {

            // print exception if development environment
            if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                exception.printStackTrace();
            }
        }
    }

    /**
     * Gets the client configuration's current preset
     * @return The current preset
     */
    public String getCurrentPreset() {
        return currentPreset;
    }
}
