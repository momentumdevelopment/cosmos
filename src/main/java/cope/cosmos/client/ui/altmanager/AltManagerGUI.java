package cope.cosmos.client.ui.altmanager;

import cope.cosmos.client.manager.managers.AlttManager;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Wolfsurge
 */
public class AltManagerGUI extends GuiScreen {

    // Last GUI screen
    private final GuiScreen lastScreen;

    // List of alt entries
    // public static final List<AltEntry> altEntries = new ArrayList<>();

    // Offset of the entries
    public static float altEntryOffset = 41;

    public AltManagerGUI(GuiScreen lastScreen) {
        this.lastScreen = lastScreen;
    }

    @Override
    public void initGui() {
        // Add buttons
        ScaledResolution scaledResolution = new ScaledResolution(mc);

        this.buttonList.add(new GuiButton(0, scaledResolution.getScaledWidth() / 2 - 150, scaledResolution.getScaledHeight() - 23, 100, 20, "Add Alt"));
        this.buttonList.add(new GuiButton(1, scaledResolution.getScaledWidth() / 2 - 50, scaledResolution.getScaledHeight() - 23, 100, 20, "Delete Selected"));
        this.buttonList.add(new GuiButton(2, scaledResolution.getScaledWidth() / 2 + 50, scaledResolution.getScaledHeight() - 23, 100, 20, "Back"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution scaledResolution = new ScaledResolution(mc);

        // Draw dirt background
        drawDefaultBackground();

        // Title
        drawCenteredString(mc.fontRenderer, "Cosmos Alt Manager", scaledResolution.getScaledWidth() / 2, 10, 0xFFFFFF);

        // Background
        RenderUtil.drawRect((scaledResolution.getScaledWidth() / 2f) - 155, 36, 310, scaledResolution.getScaledHeight() - 38, 0x90000000);

        // Scissor around alt entries
        RenderUtil.scissor((scaledResolution.getScaledWidth() / 2f) - 151, 40, (scaledResolution.getScaledWidth() / 2f) + 151, scaledResolution.getScaledHeight() - 24);

        // Draw alt entries
        for(AltEntry altEntry : AlttManager.getAltEntries())
            altEntry.drawAltEntry(mouseX, mouseY);

        // Stop scissoring
        RenderUtil.endScissor();

        // Display 'No alts!' if there are no alt entries
        if(AlttManager.getAltEntries().isEmpty())
            FontUtil.drawCenteredStringWithShadow("No alts!", scaledResolution.getScaledWidth() / 2f, 50, 0xFFFFFF);

        // 'Currently logged in as' text
        FontUtil.drawStringWithShadow("Currently logged in as " + TextFormatting.GRAY + mc.getSession().getUsername(), 3, 3, 0xFFFFFF);

        // Scroll the alts
        scroll(mouseX, mouseY);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * Scrolls the alt entries
     */
    private void scroll(int mouseX, int mouseY) {
        ScaledResolution scaledResolution = new ScaledResolution(mc);

        // Cancel if there are no alts
        if(AlttManager.getAltEntries().isEmpty()) return;

        // Refresh offsets before running the rest of the code
        refreshOffsets();

        // Check that the mouse is over the available area
        if(mouseOver((scaledResolution.getScaledWidth() / 2f) - 151, 40, 302, scaledResolution.getScaledHeight() - 25, mouseX, mouseY)) {
            int scroll = Mouse.getDWheel();
            if(scroll < 0) {
                // Cancel if the last alt is fully visible
                if(getLastAlt().getOffset() - 1 < scaledResolution.getScaledHeight() - 49) return;

                for (AltEntry altEntry : AlttManager.getAltEntries()) {
                    altEntry.setOffset(altEntry.getOffset() - 16);
                }
            }

            if(scroll > 0) {
                // Cancel if the first alt is fully visible
                if(getFirstAlt().getOffset() >= 41) return;

                for (AltEntry altEntry : AlttManager.getAltEntries()) {
                    altEntry.setOffset(altEntry.getOffset() + 16);
                }
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        switch (button.id) {
            case 0:
                mc.displayGuiScreen(new AddAltGUI(this));
                break;
            case 1:
                if(getSelectedAltEntry() != null) {
                    AlttManager.getAltEntries().remove(getSelectedAltEntry());
                    altEntryOffset -= 32;
                    if(!AlttManager.getAltEntries().isEmpty())
                        getFirstAlt().setOffset(41);
                }
                break;
            case 2:
                mc.displayGuiScreen(lastScreen);
                break;
        }
    }

    /**
     * Called when the GUI is closed. Save the alts when called.
     */
    @Override
    public void onGuiClosed() {
        AlttManager.saveAlts();
    }

    /**
     * Refreshes the alt entries' offsets
     */
    private void refreshOffsets() {
        float altOffset = getFirstAlt().getOffset();
        for(AltEntry altEntry : AlttManager.getAltEntries()) {
            altEntry.setOffset(altOffset);
            altOffset += 32;
        }
    }

    /**
     * Gets the first alt
     * @return The first alt
     */
    private AltEntry getFirstAlt() {
        return AlttManager.getAltEntries().get(0);
    }

    /**
     * There has got to be a better way of doing this...
     * @return The last alt
     */
    private AltEntry getLastAlt() {
        return AlttManager.getAltEntries().get(AlttManager.getAltEntries().size() - 1);
    }

    /**
     * Gets the selected alt
     * @return The selected alt
     */
    private AltEntry getSelectedAltEntry() {
        for(AltEntry altEntry : AlttManager.getAltEntries()) {
            if(altEntry.isSelected)
                return altEntry;
        }

        return null;
    }

    /**
     * Sets the selected alt
     * @param newSelected The new selected alt
     */
    private void setSelectedAltEntry(AltEntry newSelected) {
        for(AltEntry altEntry : AlttManager.getAltEntries())
            altEntry.isSelected = altEntry == newSelected;
    }

    /**
     * Called when a key is typed
     * @param typedChar The character typed
     * @param keyCode The keycode of the character
     * @throws IOException Input output exception
     */
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if(keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(lastScreen);
            return;
        }

        super.keyTyped(typedChar, keyCode);
    }

    /**
     * Called when the mouse is clicked
     * @param mouseX The mouse's X
     * @param mouseY The mouse's Y
     * @param mouseButton The button which is clicked
     * @throws IOException It's an IO Exception, it does IO Exception things...
     */
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        // Set the selected alt
        for(AltEntry altEntry : AlttManager.getAltEntries()) {
            if(altEntry.isMouseOverButton(mouseX, mouseY)) {
                if(altEntry.isSelected)
                    altEntry.whenClicked(mouseX, mouseY, mouseButton);

                setSelectedAltEntry(altEntry);
            }
        }
    }

    /**
     * Linus, I don't know what you did with your isMouseOver method, but it doesn't work :/ (so I did this instead)
     * Checks if the mouse is over a region
     * @param x The lower x
     * @param y The lower y
     * @param width The upper x
     * @param height The upper y
     * @param mouseX The mouse's X
     * @param mouseY The mouse's Y
     * @return Whether the mouse is over the given region
     */
    public static boolean mouseOver(double x, double y, double width, double height, int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height;
    }
}