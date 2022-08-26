package cope.cosmos.font;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatAllowedCharacters;
import org.lwjgl.opengl.GL11;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;
import java.util.Random;

/**
 * @author LiquidBounce Development, linustouchtips
 * @since 05/25/2021
 */
public class FontRenderer {

    // font image
    private final FontImage fontImage;

    // colors
    private static final int[] hexColors = new int[16];

    public FontRenderer(Font font) {
        fontImage = new FontImage(font);
    }

    /**
     * Draws a given text with a shadow
     * @param text The given text
     * @param x The x position
     * @param y The y position
     * @param color The color of the text
     * @param antiAlias Whether to apply anti-aliasing
     * @return The integer value of the render
     */
    @ParametersAreNonnullByDefault
    public int drawStringWithShadow(String text, float x, float y, int color, boolean antiAlias) {
        return drawString(text, x, y, color, true, antiAlias);
    }

    /**
     * Draws a given text
     * @param text The given text
     * @param x The x position
     * @param y The y position
     * @param color The color of the text
     * @param shadow Whether to draw a shadow behind the text
     * @param antiAlias Whether to apply anti-aliasing
     * @return The integer value of the render
     */
    @ParametersAreNonnullByDefault
    public int drawString(String text, float x, float y, int color, boolean shadow, boolean antiAlias) {

        // scaled y ???
        float scaledY = y - 3;

        // new line handling
        if (text.contains("\n")) {

            // split text into word
            String[] words = text.split("\n");

            // next line y
            float nextLineY = 0.0f;

            // render strings on new lines
            for (String word : words) {
                drawText(word, x, scaledY + nextLineY, color, shadow, antiAlias);
                nextLineY += getHeight();
            }

            return 0;
        }

        // draw a "shadow" text behind the text to give the font more visibility
        if (shadow) {
            drawText(text, x + 0.4F, scaledY + 0.3F, new Color(0, 0, 0, 150).getRGB(), true, antiAlias);
        }

        // draw given text
        return drawText(text, x, scaledY, color, false, antiAlias);
    }

    /**
     * Draws a given text
     * @param in The given text
     * @param x The x position
     * @param y The y position
     * @param color The color of the text
     * @param ignoreColor Whether to ignore color identifiers
     * @param antiAlias Whether to apply anti-aliasing
     * @return The integer value of the render
     */
    private int drawText(String in, float x, float y, int color, boolean ignoreColor, boolean antiAlias) {

        // check if the text is valid
        if (in != null && !in.isEmpty()) {
            GlStateManager.pushMatrix();

            // translate to given position
            GlStateManager.translate(x - 1.5, y + 0.5, 0);

            // start render
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.enableTexture2D();

            // anti-aliasing
            if (antiAlias) {
                GL11.glEnable(GL11.GL_LINE_SMOOTH);
            }
            
            // format color
            int currentColor = color;
            if ((currentColor & 0xFC000000) == 0) {
                currentColor |= 0xFF000000;
            }

            // alpha value
            int alpha = currentColor >> 24 & 0xFF;
            
            // color identifier
            if (in.contains("§")) {
                
                // split text into words
                String[] text = in.split("§");

                // width and random case variables
                double width = 0;
                boolean randomCase = false;

                for (int i = 0; i < text.length; i++) {
                    
                    // current word
                    String word = text[i];

                    // ignore empty words
                    if (word.isEmpty()) {
                        continue;
                    }

                    // draw string and add to width
                    if (i == 0) {
                        fontImage.drawString(word, width, 0, currentColor);
                        width += fontImage.getStringWidth(word);
                        continue;
                    }

                    // word without the color identifier
                    String words = word.substring(1);
                    
                    // character
                    char type = word.charAt(0);
                    
                    // identify colors from identifier
                    int colorIndex = "0123456789abcdefklmnor".indexOf(type);
                    switch (colorIndex) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                        case 7:
                        case 8:
                        case 9:
                        case 10:
                        case 11:
                        case 12:
                        case 13:
                        case 14:
                        case 15:

                            // get the hex color attached to the identifier
                            if (!ignoreColor) {
                                currentColor = hexColors[colorIndex] | alpha << 24;
                            }

                            randomCase = false;
                            break;
                        case 16:

                            // random case identifier
                            randomCase = true;
                            break;
                        case 18:
                            break;
                        case 21:

                            // format color
                            currentColor = color;
                            if ((currentColor & 0xFC000000) == 0) {
                                currentColor |= 0xFF000000;
                            }

                            randomCase = false;
                    }
                    
                    // render string
                    if (randomCase) {
                        fontImage.drawString(getUnicodeText(words), width, 0, currentColor);
                    }
                    
                    else {
                        fontImage.drawString(words, width, 0, currentColor);
                    }

                    // add to width
                    width += fontImage.getStringWidth(words);
                }
            } 
            
            // normal rendering
            else {
                fontImage.drawString(in, 0, 0, currentColor);
            }

            // reset anti-aliasing
            if (antiAlias) {
                GL11.glDisable(GL11.GL_LINE_SMOOTH);
            }

            // end render
            GlStateManager.disableBlend();
            GlStateManager.translate(-(x - 1.5), -(y + 0.5), 0);
            
            GlStateManager.popMatrix();

            return (int) (x + getStringWidth(in));
        }

        return (int) x;
    }

    /**
     * Gets the font name
     * @return The font name
     */
    public String getName() {
        return fontImage.getName();
    }

    /**
     * Gets the hex color code of a given character
     * @param in The given character
     * @return The hex color code of the given character
     */
    public int getColorCode(char in) {
        return hexColors[FontRenderer.getColorIndex(in)];
    }

    /**
     * Gets the width of a given string
     * @param in The given string
     * @return The width of a given string
     */
    public int getStringWidth(String in) {
        
        // color identifier, we have to custom handle this 
        if (in.contains("§")) {
            
            // split up text
            String[] text = in.split("§");
            
            // current width
            int width = 0;
            
            for (int i = 0; i < text.length; i++) {
                
                // word in the text
                String word = text[i];

                // ignore empty words
                if (word.isEmpty()) {
                    continue;
                }

                // increase width
                if (i == 0) {
                    width += fontImage.getStringWidth(word);
                    continue;
                }

                // word with identifiers removed
                String words = word.substring(1);
                
                // increase width
                width += fontImage.getStringWidth(words);
            }

            // total width
            return width / 2;
        }

        // normal width
        return fontImage.getStringWidth(in) / 2;
    }

    /**
     * Gets the font height
     * @return The font height
     */
    public float getHeight() {
        return fontImage.getHeight() / 2F;
    }

    /**
     * Gets the font size
     * @return The font size
     */
    public int getSize() {
        return fontImage.getFont().getSize();
    }

    /**
     * Gets the color index of a given character
     * @param in The character
     * @return the color index of a given character
     */
    public static int getColorIndex(char in) {
        switch (in) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return in - 48;
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
                return in - 87;
            case 'k':
            case 'l':
            case 'm':
            case 'n':
            case 'o':
                return in - 91;
            case 'r':
                return 21;
        }

        // invalid
        return -1;
    }

    /**
     * Gets a random unicode text (used for obfuscated text and enchantment tables)
     * @param in The text to convert to unicode
     * @return The converted text
     */
    private String getUnicodeText(String in) {

        // final unicode text
        StringBuilder unicode = new StringBuilder();

        // check characters in message
        for (char character : in.toCharArray()) {

            // if the character isn't allowed to be sent in chat, ignore
            if (!ChatAllowedCharacters.isAllowedCharacter(character)) {
                continue;
            }

            // allowed unicode character
            String allowedUnicode = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■";

            // add random unicode
            Random random = new Random();
            int index = random.nextInt(allowedUnicode.length());

            // add the unicode to the final string
            unicode.append(allowedUnicode.charAt(index));
        }

        return unicode.toString();
    }

    // assign hex color values to the array
    static {
        hexColors[0] = 0;
        hexColors[1] = 170;
        hexColors[2] = 43520;
        hexColors[3] = 43690;
        hexColors[4] = 0xAA0000;
        hexColors[5] = 0xAA00AA;
        hexColors[6] = 0xFFAA00;
        hexColors[7] = 0xAAAAAA;
        hexColors[8] = 0x555555;
        hexColors[9] = 0x5555FF;
        hexColors[10] = 0x55FF55;
        hexColors[11] = 0x55FFFF;
        hexColors[12] = 0xFF5555;
        hexColors[13] = 0xFF55FF;
        hexColors[14] = 0xFFFF55;
        hexColors[15] = 0xFFFFFF;
    }
}
