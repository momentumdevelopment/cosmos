package cope.cosmos.font;

import cope.cosmos.util.Wrapper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 * @author LiquidBounce Development, linustouchitps
 * @since 05/25/2021
 */
@SideOnly(value = Side.CLIENT)
public class FontImage implements Wrapper {
    
    // font info
    private final Font font;
    private int fontHeight = -1;
    
    // character locations
    private final CharacterLocation[] characterLocations;
    
    // caches
    private final HashMap<String, FontCache> cachedStrings = new HashMap<>();
    
    // texture info
    private int textureID = 0;
    private int textureWidth = 0;
    private int textureHeight = 0;

    public FontImage(Font font) {
        this.font = font;
        characterLocations = new CharacterLocation[255];
        renderBitmap(0, 255);
    }

    /**
     * Gets the font
     * @return The font
     */
    public Font getFont() {
        return font;
    }

    /**
     * Gets the font name
     * @return The font name
     */
    public String getName() {
        return font.getName();
    }

    /**
     * Gets the string width of a given text
     * @param text The given text
     * @return The string width of the given text
     */
    public int getStringWidth(String text) {
        
        // text width
        int width = 0;
        
        // check each character
        for (int character : text.toCharArray()) {
          
            // index of the character
            int index = character < characterLocations.length ? character : 3;

            // font character
            CharacterLocation fontCharacter = characterLocations[index];

            // add to wdith
            if (characterLocations.length <= index || fontCharacter == null) {
                width += mc.fontRenderer.getStringWidth(String.valueOf(character)) / 4;
                continue;
            }

            // add to width
            width += fontCharacter.width - 8;
        }

        return width / 2;
    }

    /**
     * Gets the font height
     * @return The font height
     */
    public float getHeight() {
        return (fontHeight - 8F) / 2F;
    }

    /**
     * Draws the text to an image
     * @param text The text to draw
     * @param x The x position
     * @param y The y position
     * @param color The color of the text
     */
    public void drawString(String text, double x, double y, int color) {
        
        // start render
        GlStateManager.pushMatrix();
        
        // scale and translate
        GlStateManager.scale(0.25, 0.25, 0.25);
        GL11.glTranslated(x * 2, y * 2 - 2, 0);
        
        // bind font texture
        GlStateManager.bindTexture(textureID);

        // color
        GlStateManager.color((color >> 16 & 0xFF) / 255F, (color >> 8 & 0xFF) / 255F, (color & 0xFF) / 255F, (color >> 24 & 0xFF) / 255F);
        
        // current x offset
        double offset = 0;
        
        // get the font cache
        FontCache cached = cachedStrings.get(text);

        // if the cache exist, attempt to use the image cache
        if (cached != null) {
            GL11.glCallList(cached.getDisplayList());
            
            // update the last usage
            cached.setLastUsage(System.currentTimeMillis());
            
            // end render
            GlStateManager.popMatrix();
            return;
        }

        // get the image for each character
        GL11.glBegin(7);
        for (char character : text.toCharArray()) {

            // ignore if font character is out of bounds
            if (characterLocations.length <= character) {
                continue;
            }
            
            // font character
            CharacterLocation fontCharacter = characterLocations[character];
            if (Character.getNumericValue(character) >= characterLocations.length) {
                GL11.glEnd();

                // scale
                GlStateManager.scale(4, 4, 4);
                mc.fontRenderer.drawString(String.valueOf(character), (float) offset * 0.25F + 1, 2F, color, false);
                offset += (double) mc.fontRenderer.getStringWidth(String.valueOf(character)) * 4;

                // reset scale
                GlStateManager.scale(0.25, 0.25, 0.25);

                // bind font texture
                GlStateManager.bindTexture(textureID);
                
                // color
                GlStateManager.color((color >> 16 & 0xFF) / 255F, (color >> 8 & 0xFF) / 255F, (color & 0xFF) / 255F, (color >> 24 & 0xFF) / 255F);
                GL11.glBegin(7);
                continue;
            }

            // ignore if font character doesn't exist
            if (fontCharacter == null) {
                continue;
            }

            // draw the character and update offsets
            drawCharacter(fontCharacter, (float) offset, 0);
            offset += (double) fontCharacter.width - 8;
        }

        // end render
        GL11.glEnd();
        GlStateManager.popMatrix();
    }

    /**
     * Draws a character to an image
     * @param character The character to draw
     * @param x The x position
     * @param y The y position
     */
    private void drawCharacter(CharacterLocation character, float x, float y) {

        // draw character
        GL11.glTexCoord2f(character.x / (float) textureWidth, character.y / (float) textureHeight);
        GL11.glVertex2f(x, y);
        GL11.glTexCoord2f(character.x / (float) textureWidth, (character.y / (float) textureHeight) + (character.height / (float) textureHeight));
        GL11.glVertex2f(x, y + character.height);
        GL11.glTexCoord2f((character.x / (float) textureWidth) + (character.width / (float) textureWidth), (character.y / (float) textureHeight) + (character.height / (float) textureHeight));
        GL11.glVertex2f(x + character.width, y + character.height);
        GL11.glTexCoord2f((character.x / (float) textureWidth) + (character.width / (float) textureWidth), character.y / (float) textureHeight);
        GL11.glVertex2f(x + character.width, y);
    }

    /**
     * Renders all the characters in the bitmap
     * @param startCharacter The character to start at
     * @param stopCharacter The character to stop at
     */
    private void renderBitmap(int startCharacter, int stopCharacter) {
        
        // images of the font
        BufferedImage[] fontImages = new BufferedImage[stopCharacter];
        
        // position
        int height = 0;
        int characterX = 0;
        int characterY = 0;

        // update from start to stop
        for (int i = startCharacter; i < stopCharacter; i++) {
            
            // font images
            BufferedImage fontImage = drawCharacterToImage((char) i);
            CharacterLocation fontCharacter = new CharacterLocation(characterX, characterY, fontImage.getWidth(), fontImage.getHeight());

            // clamp heights
            if (fontCharacter.height > fontHeight) {
                fontHeight = fontCharacter.height;
            }

            if (fontCharacter.height > height) {
                height = fontCharacter.height;
            }

            // ignore if out of bounds ???
            if (characterLocations.length <= i) {
                continue;
            }

            // update bitmap
            characterLocations[i] = fontCharacter;
            fontImages[i] = fontImage;

            // ???
            if ((characterX += fontCharacter.width) <= 2048) {
                continue;
            }

            // clamp to width
            if (characterX > textureWidth) {
                textureWidth = characterX;
            }

            // update positions
            characterX = 0;
            characterY += height;
            height = 0;
        }

        // height of the texture
        textureHeight = characterY + height;

        // generate graphics from image
        BufferedImage bufferedImage = new BufferedImage(textureWidth, textureHeight, 2);
        Graphics2D graphics2D = (Graphics2D) bufferedImage.getGraphics();
        graphics2D.setFont(font);
        graphics2D.setColor(new Color(255, 255, 255, 0));
        graphics2D.fillRect(0, 0, textureWidth, textureHeight);
        graphics2D.setColor(Color.WHITE);

        // draw from start to stop
        for (int i = startCharacter; i < stopCharacter; i++) {

            // check if the image exists
            if (fontImages[i] == null || characterLocations[i] == null) {
                continue;
            }

            // draw the character image
            graphics2D.drawImage(fontImages[i], characterLocations[i].x, characterLocations[i].y, null);
        }

        // update texture id
        textureID = TextureUtil.uploadTextureImageAllocate(TextureUtil.glGenTextures(), bufferedImage, true, true);
    }

    /**
     * Draws a character to an image
     * @param character The character to draw
     * @return The image of the character
     */
    private BufferedImage drawCharacterToImage(char character) {

        // graphics with antialiasing
        Graphics2D graphics2D = (Graphics2D) new BufferedImage(1, 1, 2).getGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics2D.setFont(font);

        // get the font metrics from the graphics
        FontMetrics fontMetrics = graphics2D.getFontMetrics();

        // height and width of the character
        int characterHeight = fontMetrics.getHeight() + 3;
        int characterWidth = fontMetrics.charWidth(character) + 8;

        // clamp width
        if (characterWidth <= 8) {
            characterWidth = 7;
        }

        // clamp height
        if (characterHeight <= 0) {
            characterHeight = font.getSize();
        }

        // image of the font
        BufferedImage fontImage = new BufferedImage(characterWidth, characterHeight, 2);

        // graphics with antialiasing
        Graphics2D graphics = (Graphics2D) fontImage.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);

        // draw the character
        graphics.drawString(String.valueOf(character), 3, 1 + fontMetrics.getAscent());
        return fontImage;
    }

    private static class CharacterLocation {

        // location info
        protected final int x;
        protected final int y;
        protected final int width;
        protected final int height;

        CharacterLocation(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}
