package cope.cosmos.client.manager.managers;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.Cosmos.ClientType;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.font.FontRenderer;
import cope.cosmos.util.file.FileSystemUtil;

import java.awt.*;
import java.io.FileInputStream;
import java.io.InputStream;

public class FontManager extends Manager {

    // client font
    private FontRenderer font;
    private int fontType;

    public FontManager() {
        super("FontManager", "Manages and renders client fonts");
    }

    /**
     * Loads a given font
     * @param in The given font
     * @param type Font type (bold, italicized, etc.)
     */
    public void loadFont(String in, int type) {
        font = new FontRenderer(loadFont(in, 40, type));
        fontType = type;
    }

    /**
     * Attempts to load a given font
     * @param in The given font
     * @param size The size of the font
     * @return The loaded font
     */
    private Font loadFont(String in, int size, int type) {
        fontType = type;
        try {

            // font stream
            InputStream fontStream = new FileInputStream(FileSystemUtil.FONTS.resolve(in).toFile());

            // if the client font exists
            if (fontStream != null) {

                // creates and derives the font
                Font clientFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                clientFont = clientFont.deriveFont(type, size);

                // close stream
                fontStream.close();
                return clientFont;
            }

            // default
            return new Font(Font.SANS_SERIF, type, size);

        } catch (Exception exception) {

            // print exception
            if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                exception.printStackTrace();
            }

            // notify
            if (font != null && nullCheck()) {

                // unrecognized gamemode exception
                Cosmos.INSTANCE.getChatManager().sendHoverableMessage(ChatFormatting.RED + "Unrecognized Font!", ChatFormatting.RED + "Please enter a valid font.");
            }

            // load default
            return new Font(Font.SANS_SERIF, type, size);
        }
    }

    /**
     * Gets the current font
     * @return The current font
     */
    public FontRenderer getFontRenderer() {
        return font != null ? font : new FontRenderer(new Font(Font.SANS_SERIF, Font.PLAIN, 40));
    }

    /**
     * Gets the current font
     * @return The current font
     */
    public String getFont() {
        return font.getName();
    }

    /**
     * Gets the current font type
     * @return The current font type
     */
    public int getFontType() {
        return fontType;
    }
}
