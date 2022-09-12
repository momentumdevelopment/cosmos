package cope.cosmos.client.manager.managers;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.font.FontRenderer;
import cope.cosmos.util.file.FileSystemUtil;

import java.awt.*;
import java.io.FileInputStream;
import java.io.InputStream;

public class FontManager extends Manager {

    // client font
    private FontRenderer font;

    public FontManager() {
        super("FontManager", "Manages and renders client fonts");
    }

    /**
     * Loads a given font
     * @param in The given font
     */
    public void loadFont(String in) {
        font = new FontRenderer(loadFont(in, 40));
    }

    /**
     * Attempts to load a given font
     * @param in The given font
     * @param size The size of the font
     * @return The loaded font
     */
    private Font loadFont(String in, int size) {
        try {

            // font stream
            InputStream fontStream = new FileInputStream(FileSystemUtil.FONTS.resolve(in).toFile());

            // if the client font exists
            if (fontStream != null) {

                // creates and derives the font
                Font clientFont = Font.createFont(0, fontStream);
                clientFont = clientFont.deriveFont(Font.PLAIN, size);

                // close stream
                fontStream.close();
                return clientFont;
            }

            // default
            return new Font("default", Font.PLAIN, size);

        } catch (Exception exception) {

            // print exception
            if (Cosmos.CLIENT_TYPE.equals(Cosmos.ClientType.DEVELOPMENT)) {
                exception.printStackTrace();
            }

            // notify
            if (font != null && nullCheck()) {

                // unrecognized gamemode exception
                Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Font!", ChatFormatting.RED + "Please enter a valid font.");
            }

            // load default
            return new Font("default", Font.PLAIN, size);
        }
    }

    /**
     * Gets the current font
     * @return The current font
     */
    public FontRenderer getFontRenderer() {
        return font != null ? font : new FontRenderer(new Font("default", Font.PLAIN, 40));
    }

    /**
     * Gets the current font
     * @return The current font
     */
    public String getFont() {
        return font.getName();
    }
}
