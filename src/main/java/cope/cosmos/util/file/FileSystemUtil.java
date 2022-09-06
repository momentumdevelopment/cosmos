package cope.cosmos.util.file;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.Cosmos.ClientType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Handles file system actions
 *
 * @author aesthetical
 * @since 07/06/2022
 */
public class FileSystemUtil {

    // paths that are being written to
    private static final List<Path> WRITING_PATHS = new CopyOnWriteArrayList<>();

    // the user directory
    public static final Path RUNNING_DIRECTORY = Paths.get("");

    public static final Path COSMOS;
    public static final Path INFO;
    public static final Path SOCIAL;
    public static final Path ALTS;
    public static final Path GUI;
    public static final Path KEYBINDS;

    public static final Path PRESETS;
    public static final Path FONTS;

    /**
     * Reads the contents from a file
     * @param path the file path
     * @param createIfNotExist if to create this file if it does not exist
     * @return the content or null if none
     */
    public static String read(Path path, boolean createIfNotExist) {

        // we cannot read the directory content
        if (Files.isDirectory(path)) {
            return null;
        }

        // create our file
        if (createIfNotExist) {
            create(path);
        }

        InputStream stream = null;
        String content = null;

        try {
            stream = new FileInputStream(path.toFile());

            StringBuilder builder = new StringBuilder();

            // write characters to builder from the stream
            int i;
            while ((i = stream.read()) != -1) {
                builder.append((char) i);
            }

            // the file content
            content = builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            // close stream if exists
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return content;
    }

    /**
     * Writes to a file
     * @param path the file path
     * @param content the content to write to the file
     */
    public static void write(Path path, String content) {

        // do not allow two streams to write to the same file at once
        if (WRITING_PATHS.contains(path)) {

            System.out.println("[Cosmos] Tried to write to path " + path + " while already writing to it! Aborting...");
            return;
        }

        // create this file if it does not exist already
        create(path);

        // our file output stream
        OutputStream stream = null;

        try {

            // add to our writing paths
            WRITING_PATHS.add(path);

            // create our file output stream
            stream = new FileOutputStream(path.toFile());

            // write our bytes to the output stream
            stream.write(content.getBytes(StandardCharsets.UTF_8), 0, content.length());
        } catch (IOException e) {

            // L
            e.printStackTrace();
        } finally {

            // if the stream was created, we should close it
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // remove from our paths we are writing to
        WRITING_PATHS.remove(path);
    }

    /**
     * Creates a file if it does not exist
     * @param path the file path
     */
    public static void create(Path path) {
        if (!Files.exists(path)) {

            // create the new file
            try {
                Files.createFile(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Checks if the file system will allow us to write to this file/directory
     *
     * @param path the path
     * @return if we can write to here
     */
    public static boolean isWriteable(Path path) {
        return Files.exists(path) && Files.isWritable(path);
    }

    /**
     * Creates important directories/files to the client
     * @param path the file path
     * @param directory if this file should be created as a directory
     */
    private static void createSystemFile(Path path, boolean directory) {
        if (Files.exists(path)) {
            return;
        }

        if (directory) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        else {
            create(path);
        }

        // log in the console for debugging purposes if this is a development environment
        if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
            System.out.println("[Cosmos] Created the " + (directory ? "directory" : "file") + " " + path + " successfully.");
        }
    }

    static {

        // our path
        Path dir;

        try {
            // the user directory
            dir = new File(System.getProperty("user.dir")).toPath();
        } catch (Exception ignored) {
            dir = RUNNING_DIRECTORY;
        }

        // if we cannot write, minecraft always has access to the running directory, so we'll allow that
        if (dir == null || !isWriteable(dir)) {
            dir = RUNNING_DIRECTORY;
        }

        // set the cosmos directory
        COSMOS = dir.resolve("cosmos");

        INFO = COSMOS.resolve("info.toml");
        SOCIAL = COSMOS.resolve("social.toml");
        ALTS = COSMOS.resolve("alts.toml");
        GUI = COSMOS.resolve("gui.toml");
        KEYBINDS = COSMOS.resolve("keybinds.toml");

        // set the rest of our static constants
        PRESETS = COSMOS.resolve("presets");
        FONTS = COSMOS.resolve("fonts");

        // create proper directories/files
        createSystemFile(COSMOS, true);
        createSystemFile(PRESETS, true);
        createSystemFile(FONTS, true);

        createSystemFile(INFO, false);
        createSystemFile(SOCIAL, false);
        createSystemFile(ALTS, false);
        createSystemFile(GUI, false);
    }
}