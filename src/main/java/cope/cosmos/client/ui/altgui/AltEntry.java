package cope.cosmos.client.ui.altgui;

import cope.cosmos.client.ui.util.InterfaceWrapper;
import cope.cosmos.util.Wrapper;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.string.ColorUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Surge
 * @since 02/05/2022
 */
public class AltEntry implements Wrapper, InterfaceWrapper {

    // The alt
    private final Alt alt;

    // The offset of the button
    private float offset;

    // Is this the selected alt
    public boolean isSelected;

    public AltEntry(Alt alt, float offset) {
        this.alt = alt;
        this.offset = offset;
    }

    /**
     * Draws the alt entry
     * @param mouseX The mouse's X
     * @param mouseY The mouse's Y
     */
    public void drawAltEntry(int mouseX, int mouseY) {
        // Gets the scaled resolution
        ScaledResolution scaledResolution = new ScaledResolution(mc);

        // Background
        RenderUtil.drawRect((scaledResolution.getScaledWidth() / 2F) - 150, getOffset(), 300, 30, 0x95000000);

        // Draw border if the mouse is over the button
        if (isMouseOverButton(mouseX, mouseY)) {
            RenderUtil.drawBorder((scaledResolution.getScaledWidth() / 2F) - 150, getOffset(), 300, 30, ColorUtil.getPrimaryColor().darker().darker());
        }

        // Selected outline
        if (isSelected) {
            RenderUtil.drawBorder((scaledResolution.getScaledWidth() / 2F) - 150, getOffset(), 300, 30, ColorUtil.getPrimaryColor());
        }

        // Rotate and draw arrow. Could do with just having it pre-rotated, but I don't want to mess anything else up.
        if (getAlt().getAltSession() != null) {
            glPushMatrix();

            // Rotate
            glTranslatef(((scaledResolution.getScaledWidth() / 2F) - 148) + 10, getOffset() + 17, 1);
            glRotatef(-90, 0, 0, 1);
            glTranslatef(-(((scaledResolution.getScaledWidth() / 2F) - 148) + 10), -(getOffset() + 17), 1);

            // Colour it white
            glColor4f(255, 255, 255, 255);

            // Colour the arrow if we are logged in as that alt
            if (mc.getSession().getPlayerID().equalsIgnoreCase(getAlt().getAltSession().getPlayerID())) {
                glColor3f(ColorUtil.getPrimaryColor().getRed() / 255F, ColorUtil.getPrimaryColor().getGreen() / 255F, ColorUtil.getPrimaryColor().getBlue() / 255F);
            }

            // Bind texture
            mc.getTextureManager().bindTexture(new ResourceLocation("cosmos", "textures/icons/dropdown.png"));

            // Draw arrow
            Gui.drawModalRectWithCustomSizedTexture((scaledResolution.getScaledWidth() / 2) - 148, (int) (getOffset() + 9), 0, 0, 25, 25, 25, 25);

            glPopMatrix();
        }

        // Replace letters in between the first three letters and the '@' with asterisks
        String loginString = "";

        // Make sure that it contains the correct characters
        if (getAlt().getLogin().contains("@") && getAlt().getLogin().length() > 4 && getAlt().getAltType() != Alt.AltType.CRACKED) {
            loginString = getAlt().getLogin().substring(0, 3) + getAlt().getLogin().substring(3, getAlt().getLogin().indexOf('@')).replaceAll(".", "*") + getAlt().getLogin().substring(getAlt().getLogin().indexOf('@'));
        }

        else if (getAlt().getAltType() == Alt.AltType.CRACKED) {
            loginString = getAlt().getLogin();
        }

        // Email
        FontUtil.drawStringWithShadow(loginString + (getAlt().getAltType() != Alt.AltType.CRACKED ? TextFormatting.GRAY : ""), (scaledResolution.getScaledWidth() / 2F) - 120, getOffset() + 5, 0xFFFFFF);

        // Password
        if (getAlt().getAltType() != Alt.AltType.CRACKED) {
            FontUtil.drawStringWithShadow(getAlt().getPassword().replaceAll(".", "*"), (scaledResolution.getScaledWidth() / 2F) - 120, getOffset() + 17, 0xFFFFFF);
        }

        // Alt Type
        FontUtil.drawStringWithShadow(getAlt().getAltSession() != null ? getAlt().getAltType().name() : "[NO SESSION]", (scaledResolution.getScaledWidth() / 2F) + (145 - FontUtil.getStringWidth(getAlt().getAltSession() != null ? getAlt().getAltType().name() : "[NO SESSION]")), getOffset() + 11, 0xFFFFFF);
    }

    /**
     * Returns whether the mouse is over the button
     * @return Is the mouse is over the button
     */
    public boolean isMouseOverButton(int mouseX, int mouseY) {
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        return AltManagerGUI.mouseOver((scaledResolution.getScaledWidth() / 2F) - 150, getOffset(), 300, 30, mouseX, mouseY);
    }

    /**
     * Called when the button is clicked
     */
    public void whenClicked() {
        // Login
        getAlt().login();
    }

    /**
     * Gets the alt
     * @return The alt
     */
    public Alt getAlt() {
        return alt;
    }

    /**
     * Gets the offset of the entry
     * @return The offset of the entry
     */
    public float getOffset() {
        return offset;
    }

    /**
     * Sets the offset of the entry
     * @param offset The new offset
     */
    public void setOffset(float offset) {
        this.offset = offset;
    }
}
