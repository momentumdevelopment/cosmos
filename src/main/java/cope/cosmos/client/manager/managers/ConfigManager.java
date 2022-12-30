package cope.cosmos.client.manager.managers;

import com.moandjiezana.toml.Toml;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.Cosmos.ClientType;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Bind;
import cope.cosmos.client.features.setting.Bind.Device;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.client.manager.managers.SocialManager.Relationship;
import cope.cosmos.client.ui.altgui.Alt;
import cope.cosmos.client.ui.altgui.AltEntry;
import cope.cosmos.client.ui.altgui.AltManagerGUI;
import cope.cosmos.util.file.FileSystemUtil;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec2f;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * TODO: CLEANUP this is a mess
 * @author linustouchtips, Surge, aesthetical
 * @since 06/08/2021
 */
public class ConfigManager extends Manager {

    // the client presets
    private final List<String> presets = new CopyOnWriteArrayList<>();

    // our current preset
    private String preset = "default";
    private Path presetPath;

    public ConfigManager() {
        super("ConfigManager", "Handles the client's configurations (presets)");

        // default preset
        presets.add("default");

        // load our info file, this contains all important client information including the preset previously loaded by the user
        loadInfo();

        // load the current preset
        loadPreset(preset);

        loadSocial();
        loadAlts();
        loadKeybinds();

        // add our shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

            // start saving
            System.out.println("[Cosmos] Saving presets...");

            // save presets
            saveInfo();
            saveModules();
            saveSocial();
            saveAlts();
            saveGUI();
            saveKeybinds();

            // tell the user farewell :)
            System.out.println("[Cosmos] Saved presets successfully! See you next time :)");
        }, "PresetManager-Shutdown-Save-Thread"));
    }

    public void loadPreset(String name) {

        // the file path to this preset with the specified name
        preset = name;
        presetPath = FileSystemUtil.PRESETS.resolve(name + ".toml");

        if (!presets.contains(name)) {
            presets.add(name);
        }

        if (Files.exists(presetPath)) {
            long time = System.currentTimeMillis();

            loadModules();
//            loadSocial();
//            loadAlts();
//            loadWallhack();

            System.out.println("[Cosmos] Loaded preset " + name + " in " + (System.currentTimeMillis() - time) + "ms");
        }

        else {

            // create this preset
            createPreset(name);
        }
    }

    /**
     * Creates a preset with the given name
     * @param name The name of the preset
     */
    public void createPreset(String name) {

        // the file path to this preset with the specified name
        presetPath = FileSystemUtil.PRESETS.resolve(name + ".toml");
        presets.add(name);

        // create this preset path if we need too
        if (!Files.exists(presetPath)) {
            FileSystemUtil.create(presetPath);
        }

        preset = name;

        // save our client configurations to the preset file

        long time = System.currentTimeMillis();

        // save info - never a bad idea to do this
//        saveInfo();

        saveModules();
//        saveSocial();
//        saveAlts();
//        saveWallhack();
//        saveGUI();

        System.out.println("[Cosmos] Saved preset " + name + " in " + (System.currentTimeMillis() - time) + "ms");
    }

    /**
     * Deletes a preset
     * @param name The name of the preset
     */
    public void deletePreset(String name) {

        // path to the preset
        Path path = FileSystemUtil.PRESETS.resolve(name + ".toml");

        // check if the preset exists
        if (Files.exists(path)) {
            try {
                presets.remove(name);
                Files.deleteIfExists(path);
                System.out.println("[Cosmos] Deleted preset successfully");
            } catch (IOException e) {
                e.printStackTrace();
            }

            // re-load the default preset
            if (preset.equals(name)) {
                loadPreset(preset = "default");
            }
        }
    }

    /**
     * Loads the client info
     */
    private void loadInfo() {
        String content = FileSystemUtil.read(FileSystemUtil.INFO, true);
        if (content == null || content.isEmpty()) {
            saveInfo();
        }

        else {

            try {
                // read using TOML
                Toml toml = new Toml().read(content);

                // make sure the info tag exists in this toml file
                if (toml.contains("Info")) {

                    // load all variables under the Info tag into the important variables

                    if (toml.contains("Info.Setup")) {
                        Cosmos.SETUP = toml.getBoolean("Info.Setup");
                    }

                    if (toml.contains("Info.Prefix")) {
                        Cosmos.PREFIX = toml.getString("Info.Prefix");
                    }

                    if (toml.contains("Info.Preset")) {
                        preset = toml.getString("Info.Preset");
                    }

                    // load font
                    getCosmos().getFontManager().loadFont(toml.getString("Info.Font", Font.SANS_SERIF) + ".ttf", Math.toIntExact(toml.getLong("Info.FontStyle", (long) Font.PLAIN)));
                }

                // success c:
                System.out.println("[Cosmos] Read info.toml successfully!");
            } catch (IllegalStateException e) {

                // rip
                e.printStackTrace();
                System.out.println("[Cosmos] Could not load info file. Will revert to default configuration.");
            }

        }

        // add the presets in the presets/ directory
        File[] files = FileSystemUtil.PRESETS.toFile().listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                String[] path = file.getName().split("\\.");
                if (path.length > 0) {
                    presets.add(path[0]);
                }
            }

            System.out.println("[Cosmos] " + presets.size() + " presets were found in the presets directory");
        }
    }

    /**
     * Loads the client configuration from the TOML file
     */
    @SuppressWarnings("unchecked")
    public void loadModules() {
        String content = FileSystemUtil.read(presetPath, true);
        if (content == null || content.isEmpty()) {
            saveModules();
        }

        // the toml input from the file
        Toml inputTOML = new Toml().read(content);

        if (inputTOML != null) {
            getCosmos().getModuleManager().getAllModules().forEach(module -> {
                if (module != null) {

                    try {
                        // set the enabled state
                        if (inputTOML.getBoolean(module.getName() + ".Enabled") != null) {
                            boolean state = inputTOML.getBoolean(module.getName() + ".Enabled", false);

                            if (state) {
                                module.enable(false);
                                module.getAnimation().setState(true);
                            }

                            else {
                                module.disable(false);
                                module.getAnimation().setState(false);
                            }
                        }

                        // set the drawn state
                        if (inputTOML.getBoolean(module.getName() + ".Drawn") != null) {
                            boolean drawn = inputTOML.getBoolean(module.getName() + ".Drawn", true);
                            module.setDrawn(drawn);
                        }

                        // set the setting values
                        module.getAllSettings().forEach(setting -> {
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

                                    else if (setting.getValue() instanceof Bind) {

                                        // save non-module binds
                                        if (!setting.getName().equalsIgnoreCase("Bind")) {
                                            if (inputTOML.getString(identifier) != null) {
                                                String[] parts = inputTOML.getString(identifier).split(":");
                                                ((Setting<Bind>) setting).setValue(new Bind(Integer.parseInt(parts[0]), Enum.valueOf(Device.class, parts[1])));
                                            }
                                        }
                                    }

                                    else if (setting.getValue() instanceof List<?>) {

                                        // list value
                                        List<?> value = inputTOML.getList(identifier);

                                        // check if the list exists
                                        if (value != null) {

                                            // list type
                                            boolean itemList = false;
                                            boolean blockList = false;

                                            // lists
                                            List<Item> items = new ArrayList<>();
                                            List<Block> blocks = new ArrayList<>();

                                            // iterate through the list
                                            for (Object object : value) {

                                                // check if the object is a string
                                                if (object instanceof String) {

                                                    // item list
                                                    if (((String) object).contains("item-")) {

                                                        // update type
                                                        if (!itemList) {
                                                            itemList = true;
                                                        }

                                                        // value of the object
                                                        String objectValue = ((String) object).substring(5);

                                                        // item value
                                                        Item itemValue = Item.getByNameOrId(objectValue);

                                                        // add to list
                                                        if (itemValue != null) {
                                                            items.add(itemValue);
                                                        }
                                                    }

                                                    // block list
                                                    else if (((String) object).contains("block-")) {

                                                        // update type
                                                        if (!blockList) {
                                                            blockList = true;
                                                        }

                                                        // value of the object
                                                        String objectValue = ((String) object).substring(6);

                                                        // block value
                                                        Block blockValue = Block.getBlockFromName(objectValue);

                                                        // add to list
                                                        if (blockValue != null) {
                                                            blocks.add(blockValue);
                                                        }
                                                    }
                                                }
                                            }

                                            if (itemList) {
                                                ((Setting<List<?>>) setting).setValue(items);
                                            }

                                            else if (blockList) {
                                                ((Setting<List<?>>) setting).setValue(blocks);
                                            }
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
    }

    /**
     * Loads the client socials from the TOML file
     */
    public void loadSocial() {
        String content = FileSystemUtil.read(FileSystemUtil.SOCIAL, true);
        if (content == null || content.isEmpty()) {
            saveModules();
        }

        // clear out old social items before loading the current one
        getCosmos().getSocialManager().getSocials().clear();

        // the TOML input from the file
        Toml inputTOML = new Toml().read(content);

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

    }

    /**
     * Loads the alts from alts.toml
     */
    public void loadAlts() {
        String content = FileSystemUtil.read(FileSystemUtil.ALTS, true);
        if (content == null || content.isEmpty()) {
            saveModules();
        }

        // clear out old alt entries
        getCosmos().getAltManager().getAltEntries().clear();

        // Input TOML
        Toml input = new Toml().read(content);

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
    }

    /**
     * Load the client gui info from gui.toml
     */
    public void loadGUI() {
        String content = FileSystemUtil.read(FileSystemUtil.GUI, true);
        if (content == null || content.isEmpty()) {
            saveModules();
        }

        else {

            // Input TOML
            Toml toml = new Toml().read(content);

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
        }
    }

    /**
     * Load the client keybind info from keybinds.toml
     */
    public void loadKeybinds() {
        String content = FileSystemUtil.read(FileSystemUtil.KEYBINDS, true);
        if (content == null || content.isEmpty()) {
            saveModules();
        }

        else {

            // Input TOML
            Toml inputTOML = new Toml().read(content);

            // check if the TOML file exists
            if (inputTOML != null) {

                // check all modules
                for (Module module : getCosmos().getModuleManager().getAllModules()) {

                    // bind info
                    String bind = inputTOML.getString("Keybinds." + module.getName());

                    // update keybind
                    if (bind != null) {
                        String[] parts = bind.split(":");
                        module.getBind().setValue(new Bind(Integer.parseInt(parts[0]), Enum.valueOf(Device.class, parts[1])));
                    }
                }
            }

            // notify user
            if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                System.out.println("[Cosmos] GUI was loaded successfully!");
            }
        }
    }

    private void saveInfo() {
        if (!Files.exists(FileSystemUtil.INFO)) {
            FileSystemUtil.create(FileSystemUtil.INFO);
        }

        String builder = "[Info]" + "\n" +

                // the setup tag
                "Setup" +
                " " +
                "=" +
                " " +
                "false" + // set to false by default, as we only setup once
                "\n" +
                "Font" +
                " " +
                "=" +
                " \"" +
                getCosmos().getFontManager().getFont() + // set to false by default, as we only setup once
                "\"\n" +
                "FontType" +
                " " +
                "= " +
                getCosmos().getFontManager().getFontType() +
                "\n" +
                "Prefix" +
                " " +
                "=" +
                " \"" +
                Cosmos.PREFIX +
                "\"\n" +
                "Preset" +
                " " +
                "=" +
                " \"" +
                preset +
                "\"";

        // we have our builder above, let's save this to the info.toml file
        FileSystemUtil.write(FileSystemUtil.INFO, builder);
    }

    /**
     * Writes the module's configuration to a TOML file
     */
    public void saveModules() {

        // the output string
        StringBuilder outputTOML = new StringBuilder();

        getCosmos().getModuleManager().getAllModules().forEach(module -> {
            if (module != null) {
                try {
                    // writes the enabled state, drawn state, and bind
                    outputTOML.append("[").append(module.getName()).append("]").append("\r\n");
                    outputTOML.append("Enabled = ").append(module.isEnabled()).append("\r\n");
                    outputTOML.append("Drawn = ").append(module.isDrawn()).append("\r\n");

                    module.getAllSettings().forEach(setting -> {
                        if (setting != null) {

                            // add the parent identifier if the setting is a subsetting
                            if (!setting.getName().equalsIgnoreCase("Bind")) {
                                if (setting.hasParent()) {
                                    outputTOML.append(setting.getParentSetting().getName()).append("-").append(setting.getName());
                                }

                                else {
                                    outputTOML.append(setting.getName());
                                }

                                outputTOML.append(" = ");
                            }

                            // write the setting value
                            {
                                if (setting.getValue() instanceof Enum<?>) {
                                    outputTOML.append('"').append(setting.getValue().toString()).append('"');
                                }

                                else if (setting.getValue() instanceof Color) {
                                    outputTOML.append(((Color) setting.getValue()).getRGB());
                                }

                                else if (setting.getValue() instanceof Bind) {

                                    // save non-module binds
                                    if (!setting.getName().equalsIgnoreCase("Bind")) {
                                        outputTOML.append('"').append(((Bind) setting.getValue()).getButtonCode()).append(":").append(((Bind) setting.getValue()).getDevice().name()).append('"');
                                    }
                                }

                                else if (setting.getValue() instanceof List<?>) {
                                    outputTOML.append("[ ");

                                    for (Object object : ((List<?>) setting.getValue())) {
                                        outputTOML.append("\"");

                                        if (object instanceof Item) {
                                            outputTOML.append("item-")
                                                    .append(((Item) object).getRegistryName());
                                        }

                                        if (object instanceof Block) {
                                            outputTOML.append("block-")
                                                    .append(((Block) object).getRegistryName());
                                        }

                                        outputTOML.append("\"")
                                                .append(", ");
                                    }

                                    outputTOML.append("]");
                                }

                                else {
                                    outputTOML.append(setting.getValue());
                                }
                            }

                            // put the next setting on a new line
                            if (!setting.getName().equalsIgnoreCase("Bind")) {
                                outputTOML.append("\r\n");
                            }
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

        FileSystemUtil.write(presetPath, outputTOML.toString());
    }

    /**
     * Writes the user's socials to a TOML file
     */
    public void saveSocial() {

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

        FileSystemUtil.write(FileSystemUtil.SOCIAL, outputTOML.toString());

        // notify user
        if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
            System.out.println("[Cosmos] Socials were saved successfully!");
        }
    }

    /**
     * Writes the user's alts to a TOML file
     */
    public void saveAlts() {

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

        FileSystemUtil.write(FileSystemUtil.ALTS, output.toString());

        // notify user
        if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
            System.out.println("[Cosmos] Alts were saved successfully!");
        }
    }

    /**
     * Saves the client gui info to gui.toml
     */
    public void saveGUI() {

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

        FileSystemUtil.write(FileSystemUtil.GUI, output.toString());

        // notify user
        if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
            System.out.println("[Cosmos] GUI was saved successfully!");
        }
    }

    /**
     * Saves the client keybind info to keybinds.toml
     */
    public void saveKeybinds() {

        // Output string
        StringBuilder outputTOML = new StringBuilder();

        try {

            // class
            outputTOML.append("[Keybinds]")
                    .append("\r\n");

            // get all modules
            for (Module module : getCosmos().getModuleManager().getAllModules()) {

                // the bind setting
                Setting<?> bind = module.getBind();

                // checks if the bind is valid
                if (bind != null && bind.getValue() instanceof Bind) {
                    outputTOML.append(module.getName())
                            .append(" = ")
                            .append('"')
                            .append(((Bind) bind.getValue()).getButtonCode())
                            .append(":")
                            .append(((Bind) bind.getValue()).getDevice().name())
                            .append('"')
                            .append("\r\n");
                }
            }

        } catch (Exception exception) {

            // print exception if development environment
            if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                exception.printStackTrace();
            }
        }

        FileSystemUtil.write(FileSystemUtil.KEYBINDS, outputTOML.toString());

        // notify user
        if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
            System.out.println("[Cosmos] GUI was saved successfully!");
        }
    }

    /**
     * Gets the list of presets
     * @return The list of presets
     */
    public List<String> getPresets() {
        return presets;
    }

    /**
     * Gets the current preset
     * @return The current preset
     */
    public String getPreset() {
        return preset;
    }
}