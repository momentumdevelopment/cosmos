package cope.cosmos.client.manager.managers;

import com.moandjiezana.toml.Toml;
import cope.cosmos.client.alts.AltEntry;
import cope.cosmos.client.clickgui.cosmos.window.Window;
import cope.cosmos.client.clickgui.cosmos.window.WindowManager;
import cope.cosmos.client.clickgui.cosmos.window.windows.CategoryWindow;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.client.manager.managers.SocialManager.Relationship;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import net.minecraft.util.math.Vec2f;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PresetManager extends Manager {

    private String currentPreset;
    private final List<String> presets = new ArrayList<>();

    private final File mainDirectory = new File("cosmos");

    public PresetManager() {
        super("PresetManager", "Handles the client's configs - saving, loading, etc.", 8);
        presets.add("default");
        currentPreset = "default";

        load();
        save();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> Cosmos.INSTANCE.getPresetManager().save()));
    }

    public void setPreset(String name) {
        for (String preset : presets) {
            if (preset.equals(name)) {
                currentPreset = preset;
                break;
            }
        }
    }

    public void createPreset(String name) {
        presets.add(name);
    }

    public void removePreset(String name) {
        for (String preset : presets) {
            if (preset.equals(name)) {
                presets.remove(preset);
                break;
            }
        }
    }

    public void load() {
        writeDirectories();
        loadInfo();
        loadModules();
        loadSocial();
        loadGUI();
        loadAlts();
    }

    public void save() {
        saveInfo();
        saveModules();
        saveSocial();
        saveGUI();
        saveAlts();
    }

    public void writeDirectories() {
        if (!mainDirectory.exists()) {
            mainDirectory.mkdirs();
        }

        for (String preset : presets) {
            File presetDirectory = new File("cosmos/" + preset);

            if (!presetDirectory.exists()) {
                presetDirectory.mkdirs();
            }
        }
    }

    public void saveModules() {
        try {
            OutputStreamWriter moduleOutputStreamWriter = new OutputStreamWriter(new FileOutputStream(mainDirectory.getName() + "/" + currentPreset + "/modules.toml"), StandardCharsets.UTF_8);
            StringBuilder outputTOML = new StringBuilder();

            for (Module module : ModuleManager.getAllModules()) {
                outputTOML.append("[").append(module.getName()).append("]").append("\r\n");
                outputTOML.append("Enabled = ").append(module.isEnabled()).append("\r\n");
                outputTOML.append("Drawn = ").append(module.isDrawn()).append("\r\n");
                outputTOML.append("Bind = ").append((double) module.getKey()).append("\r\n");

                for (Setting<?> setting : module.getSettings()) {
                    if (setting != null && !(setting.getValue() instanceof Category)) {
                        if (setting.getValue() instanceof Boolean)
                            outputTOML.append(setting.hasParent() ? setting.getParentSetting().getName() + "-" + setting.getName() : setting.getName()).append(" = ").append(setting.getValue()).append("\r\n");
                        else if (setting.getValue() instanceof Integer)
                            outputTOML.append(setting.hasParent() ? setting.getParentSetting().getName() + "-" + setting.getName() : setting.getName()).append(" = ").append(setting.getValue()).append("\r\n");
                        else if (setting.getValue() instanceof Double)
                            outputTOML.append(setting.hasParent() ? setting.getParentSetting().getName() + "-" + setting.getName() : setting.getName()).append(" = ").append(setting.getValue()).append("\r\n");
                        else if (setting.getValue() instanceof Float)
                            outputTOML.append(setting.hasParent() ? setting.getParentSetting().getName() + "-" + setting.getName() : setting.getName()).append(" = ").append(setting.getValue()).append("\r\n");
                        else if (setting.getValue() instanceof Enum<?>)
                            outputTOML.append(setting.hasParent() ? setting.getParentSetting().getName() + "-" + setting.getName() : setting.getName()).append(" = ").append('"').append(setting.getValue().toString()).append('"').append("\r\n");
                        else if (setting.getValue() instanceof Color)
                            outputTOML.append(setting.hasParent() ? setting.getParentSetting().getName() + "-" + setting.getName() : setting.getName()).append(" = ").append(((Color) setting.getValue()).getRGB()).append("\r\n");
                    }
                }

                outputTOML.append("\r\n");
            }

            moduleOutputStreamWriter.write(outputTOML.toString());
            moduleOutputStreamWriter.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public void loadModules() {
        try {
            InputStream inputStream = Files.newInputStream(Paths.get(mainDirectory.getName() + "/" + currentPreset + "/modules.toml"));
            Toml inputTOML = new Toml().read(inputStream);

            if (inputTOML != null) {
                for (Module module : ModuleManager.getAllModules()) {
                    if (inputTOML.getBoolean(module.getName() + ".Enabled") != null) {
                        if (inputTOML.getBoolean(module.getName() + ".Enabled")) {
                            module.enable();
                            module.getAnimation().setState(true);
                        }

                        module.setDrawn(inputTOML.getBoolean(module.getName() + ".Drawn"));
                        module.setKey((int) ((double) inputTOML.getDouble(module.getName() + ".Bind")));

                        for (Setting<?> setting : module.getSettings()) {
                            if (setting != null && !(setting.getValue() instanceof Category)) {
                                if (setting.getValue() instanceof Boolean && inputTOML.getBoolean(module.getName() + "." + (setting.hasParent() ? setting.getParentSetting().getName() + "-" + setting.getName() : setting.getName())) != null)
                                    ((Setting<Boolean>) setting).setValue(inputTOML.getBoolean(module.getName() + "." + (setting.hasParent() ? setting.getParentSetting().getName() + "-" + setting.getName() : setting.getName())));
                                else if (setting.getValue() instanceof Integer && inputTOML.getDouble(module.getName() + "." + (setting.hasParent() ? setting.getParentSetting().getName() + "-" + setting.getName() : setting.getName())) != null)
                                    ((Setting<Integer>) setting).setValue((int) ((double) inputTOML.getDouble(module.getName() + "." + (setting.hasParent() ? setting.getParentSetting().getName() + "-" + setting.getName() : setting.getName()))));
                                else if (setting.getValue() instanceof Double && inputTOML.getDouble(module.getName() + "." + (setting.hasParent() ? setting.getParentSetting().getName() + "-" + setting.getName() : setting.getName())) != null)
                                    ((Setting<Double>) setting).setValue(inputTOML.getDouble(module.getName() + "." + (setting.hasParent() ? setting.getParentSetting().getName() + "-" + setting.getName() : setting.getName())));
                                else if (setting.getValue() instanceof Float && inputTOML.getDouble(module.getName() + "." + (setting.hasParent() ? setting.getParentSetting().getName() + "-" + setting.getName() : setting.getName())) != null)
                                    ((Setting<Float>) setting).setValue((float) ((double) inputTOML.getDouble(module.getName() + "." + (setting.hasParent() ? setting.getParentSetting().getName() + "-" + setting.getName() : setting.getName()))));
                                else if (setting.getValue() instanceof Enum<?> && Enum.valueOf(((Enum<?>) setting.getValue()).getClass(), inputTOML.getString(module.getName() + "." + (setting.hasParent() ? setting.getParentSetting().getName() + "-" + setting.getName() : setting.getName()))) != null)
                                    ((Setting<Enum<?>>) setting).setValue(Enum.valueOf(((Enum<?>) setting.getValue()).getClass(), inputTOML.getString(module.getName() + "." + (setting.hasParent() ? setting.getParentSetting().getName() + "-" + setting.getName() : setting.getName()))));
                                else if (setting.getValue() instanceof Color && inputTOML.getLong(module.getName() + "." + (setting.hasParent() ? setting.getParentSetting().getName() + "-" + setting.getName() : setting.getName())) != null) {
                                    int colorValue = (int) ((long) inputTOML.getLong(module.getName() + "." + (setting.hasParent() ? setting.getParentSetting().getName() + "-" + setting.getName() : setting.getName())));
                                    ((Setting<Color>) setting).setValue(new Color(colorValue, true));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void saveSocial() {
        try {
            OutputStreamWriter socialOutputStreamWriter = new OutputStreamWriter(new FileOutputStream(mainDirectory.getName() + "/social.toml"), StandardCharsets.UTF_8);
            StringBuilder outputTOML = new StringBuilder();

            outputTOML.append("[Social]").append("\r\n");
            outputTOML.append("Friends").append(" = ").append("[");
            for (Map.Entry<String, Relationship> social : Cosmos.INSTANCE.getSocialManager().getSocials().entrySet()) {
                outputTOML.append('"').append(social.getKey()).append('"').append(", ");
            }

            outputTOML.append("]").append("\r\n");
            socialOutputStreamWriter.write(outputTOML.toString());
            socialOutputStreamWriter.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void loadSocial() {
        try {
            InputStream inputStream = Files.newInputStream(Paths.get(mainDirectory.getName() + "/social.toml"));
            Toml inputTOML = new Toml().read(inputStream);

            if (inputTOML != null) {
                Iterator<Object> friendsList = inputTOML.getList("Social.Friends").iterator();

                while (friendsList.hasNext()) {
                    String friend = (String) friendsList.next();
                    Cosmos.INSTANCE.getSocialManager().addSocial(friend, Relationship.FRIEND);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void saveInfo() {
        try {
            OutputStreamWriter infoOutputStreamWriter = new OutputStreamWriter(new FileOutputStream(mainDirectory.getName() + "/info.toml"), StandardCharsets.UTF_8);
            StringBuilder outputTOML = new StringBuilder();

            outputTOML.append("[Info]").append("\r\n");
            outputTOML.append("Setup = ").append(Cosmos.SETUP).append("\r\n");
            outputTOML.append("Prefix = ").append('"').append(Cosmos.PREFIX).append('"').append("\r\n");
            outputTOML.append("Preset = ").append('"').append(currentPreset).append('"').append("\r\n");

            infoOutputStreamWriter.write(outputTOML.toString());
            infoOutputStreamWriter.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void loadInfo() {
        try {
            InputStream inputStream = Files.newInputStream(Paths.get(mainDirectory.getName() + "/info.toml"));
            Toml inputTOML = new Toml().read(inputStream);

            Cosmos.SETUP = inputTOML.getBoolean("Info.Setup");
            Cosmos.PREFIX = inputTOML.getString("Info.Prefix");
            currentPreset = inputTOML.getString("Info.Preset");
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

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
            OutputStreamWriter guiOutputStreamWriter = new OutputStreamWriter(new FileOutputStream(mainDirectory.getName() + "/gui.toml"), StandardCharsets.UTF_8);
            StringBuilder outputTOML = new StringBuilder();


            for (Window window : WindowManager.getWindows()) {
                CategoryWindow categoryWindow = ((CategoryWindow) (window));
                outputTOML.append("[").append(Setting.formatEnum(categoryWindow.getCategory())).append("]").append("\r\n");
                outputTOML.append("X = ").append(categoryWindow.getPosition().x).append("\r\n");
                outputTOML.append("Y = ").append(categoryWindow.getPosition().y).append("\r\n");
                outputTOML.append("Height = ").append(categoryWindow.getHeight()).append("\r\n");

                outputTOML.append("\r\n");
            }

            guiOutputStreamWriter.write(outputTOML.toString());
            guiOutputStreamWriter.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void loadGUI() {
        try {
            InputStream inputStream = Files.newInputStream(Paths.get(mainDirectory.getName() + "/gui.toml"));
            Toml inputTOML = new Toml().read(inputStream);

            for (Window window : WindowManager.getWindows()) {
                CategoryWindow categoryWindow = ((CategoryWindow) (window));
                categoryWindow.setPosition(new Vec2f((float) ((double) inputTOML.getDouble(Setting.formatEnum(categoryWindow.getCategory()) + ".X")), (float) ((double) inputTOML.getDouble(Setting.formatEnum(categoryWindow.getCategory()) + ".Y"))));
                categoryWindow.setHeight((float) ((double) inputTOML.getDouble(Setting.formatEnum(categoryWindow.getCategory()) + ".Height")));
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public String getCurrentPreset() {
        return currentPreset;
    }
}
