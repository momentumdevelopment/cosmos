package cope.cosmos.client.ui.altmanager;

import cope.cosmos.asm.mixins.accessor.IMinecraft;
import cope.cosmos.client.ui.util.InterfaceUtil;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.string.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

/**
 * @author Wolfsurge
 */
public class AltEntry implements InterfaceUtil {

    // The alt
    private Alt alt;
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
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

        // Background
        RenderUtil.drawRect((scaledResolution.getScaledWidth() / 2f) - 150, getOffset(), 300, 30, 0x95000000);

        // Draw border if the mouse is over the button
        if(isMouseOverButton(mouseX, mouseY))
            RenderUtil.drawBorder((scaledResolution.getScaledWidth() / 2f) - 150, getOffset(), 300, 30, ColorUtil.getPrimaryColor().darker().darker());

        // Selected outline
        if(isSelected)
            RenderUtil.drawBorder((scaledResolution.getScaledWidth() / 2f) - 150, getOffset(), 300, 30, ColorUtil.getPrimaryColor());

        // Rotate and draw arrow. Could do with just having it pre-rotated, but I don't want to mess anything else up.
        if(getAlt().getAltSession() != null) {
            GL11.glPushMatrix();

            // Rotate
            GL11.glTranslatef(((scaledResolution.getScaledWidth() / 2f) - 149) + 10, getOffset() + 18, 1);
            GL11.glRotatef(-90, 0, 0, 1);
            GL11.glTranslatef(-(((scaledResolution.getScaledWidth() / 2f) - 149) + 10), -(getOffset() + 18), 1);

            // Colour it white
            GL11.glColor4f(255, 255, 255, 255);

            // Colour the arrow if we are logged in as that alt
            if(Minecraft.getMinecraft().getSession().getPlayerID().equalsIgnoreCase(getAlt().getAltSession().getPlayerID()))
                GL11.glColor3f(ColorUtil.getPrimaryColor().getRed() / 255f, ColorUtil.getPrimaryColor().getGreen() / 255f, ColorUtil.getPrimaryColor().getBlue() / 255f);

            // Bind texture
            Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("cosmos", "textures/icons/dropdown.png"));

            // Draw arrow
            Gui.drawModalRectWithCustomSizedTexture((scaledResolution.getScaledWidth() / 2) - 149, (int) (getOffset() + 10), 0, 0, 25, 25, 25, 25);

            GL11.glPopMatrix();
        }

        // Replace letters in between the first three letters and the '@' with asterisks
        String loginString = "";
        // Make sure that it contains the correct characters
        if (getAlt().getLogin().contains("@") && getAlt().getLogin().length() > 4 && getAlt().getAltType() != Alt.AltType.Cracked)
            loginString = getAlt().getLogin().substring(0, 3) + getAlt().getLogin().substring(3, getAlt().getLogin().indexOf('@')).replaceAll(".", "*") + getAlt().getLogin().substring(getAlt().getLogin().indexOf('@'));

        else if (getAlt().getAltType() == Alt.AltType.Cracked)
            loginString = getAlt().getLogin();

        // Email / Error Message
        if(getAlt().getAltSession() != null)
            FontUtil.drawStringWithShadow(loginString + (getAlt().getAltType() != Alt.AltType.Cracked ? TextFormatting.GRAY + " | " + getAlt().getAltSession().getUsername() : ""), (scaledResolution.getScaledWidth() / 2f) - 120, getOffset() + 5, 0xFFFFFF);
        else
            FontUtil.drawStringWithShadow(TextFormatting.DARK_RED + "Invalid User. Possible Rate Limit.", (scaledResolution.getScaledWidth() / 2f) - 120, getOffset() + 5, 0xFFFFFF);

        // Password
        if(getAlt().getAltType() != Alt.AltType.Cracked)
            FontUtil.drawStringWithShadow(getAlt().getPassword().replaceAll(".", "*"), (scaledResolution.getScaledWidth() / 2f) - 120, getOffset() + 17, 0xFFFFFF);

        // Alt Type
        FontUtil.drawStringWithShadow(getAlt().getAltSession() != null ? getAlt().getAltType().name() : "[INVALID]", (scaledResolution.getScaledWidth() / 2f) + (145 - FontUtil.getStringWidth(getAlt().getAltSession() != null ? getAlt().getAltType().name() : "[INVALID]")), getOffset() + 11, 0xFFFFFF);
    }

    /**
     * Returns whether the mouse is over the button
     * @return Is the mouse is over the button
     */
    public boolean isMouseOverButton(int mouseX, int mouseY) {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        return AltManagerGUI.mouseOver((scaledResolution.getScaledWidth() / 2f) - 150, getOffset(), 300, 30, mouseX, mouseY);
    }

    /**
     * Called when the button is clicked
     */
    public void whenClicked() {
        // Login if the session isn't null, and if the entry is already selected
        if(getAlt().getAltSession() != null) {
            ((IMinecraft) Minecraft.getMinecraft()).setSession(getAlt().getAltSession());
        }
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
