package cope.cosmos.client.manager.managers;

import com.moandjiezana.toml.Toml;
import cope.cosmos.client.ui.altmanager.Alt;
import cope.cosmos.client.ui.altmanager.AltEntry;
import cope.cosmos.client.ui.altmanager.AltManagerGUI;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Wolfsurge, linustouchtips
 */
public class AltManager {

    // List of alt entries
    private static final List<AltEntry> altEntries = new ArrayList<>();

    // The client directory
    private static final File mainDirectory = new File("cosmos");

    /**
     * Saves the alt accounts to alts.toml
     */
    public static void saveAlts() {
        try {
            // File writer
            OutputStreamWriter altOutputStreamWriter = new OutputStreamWriter(new FileOutputStream(mainDirectory.getName() + "/alts.toml"), StandardCharsets.UTF_8);

            // Output
            StringBuilder output = new StringBuilder();

            try {
                output.append("[Alts]").append("\r\n");

                output.append("Alts").append(" = ").append("[");
                // Add the alt's info, in the order: email:password:type
                for(AltEntry altEntry : getAltEntries()) {
                    output.append('"').append(altEntry.getAlt().getLogin()).append(":").append(altEntry.getAlt().getPassword()).append(":").append(altEntry.getAlt().getAltType().name()).append('"').append(",");
                }
                output.append("]").append("\r\n");
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            // Write the info
            altOutputStreamWriter.write(output.toString());
            altOutputStreamWriter.close();
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Loads the alts from alts.toml
     */
    public static void loadAlts() {
        try {
            // The stream from alts file
            InputStream inputStream = Files.newInputStream(Paths.get(mainDirectory.getName() + "/alts.toml"));

            // Input TOML
            Toml input = new Toml().read(inputStream);

            if(input != null) {
                try {
                    if(input.getList("Alts.Alts") != null) {
                        input.getList("Alts.Alts").forEach(object -> {
                            // 0 = Email, 1 = Password, 2 = Type
                            String[] altInfo = ((String) object).split(":");

                            Alt.AltType type = null;
                            if(altInfo[2].equalsIgnoreCase("Microsoft")) type = Alt.AltType.Microsoft;
                            else if(altInfo[2].equalsIgnoreCase("Mojang")) type = Alt.AltType.Mojang;
                            else if(altInfo[2].equalsIgnoreCase("Cracked")) type = Alt.AltType.Cracked;

                            // Loading alt
                            Alt alt = new Alt(altInfo[0], altInfo[1], type);

                            // Add alt to list
                            getAltEntries().add(new AltEntry(alt, AltManagerGUI.altEntryOffset));
                            // Add to offset
                            AltManagerGUI.altEntryOffset += 32;
                        });
                    }
                }
                catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Gets the alt entries
     * @return The alt entries
     */
    public static List<AltEntry> getAltEntries() {
        return altEntries;
    }

}
