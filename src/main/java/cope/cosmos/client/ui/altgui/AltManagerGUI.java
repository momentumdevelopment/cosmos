package cope.cosmos.client.ui.altgui;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.ui.util.ScissorStack;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;

/**
 * @author Surge
 * @since 02/05/2022
 */
public class AltManagerGUI extends GuiScreen {

    // Last GUI screen
    private final GuiScreen lastScreen;

    // Offset of the entries
    public static float altEntryOffset = 41;

    private final ScissorStack scissorStack = new ScissorStack();

    public AltManagerGUI(GuiScreen lastScreen) {
        this.lastScreen = lastScreen;
    }

    @Override
    public void initGui() {
        // Add buttons
        ScaledResolution scaledResolution = new ScaledResolution(mc);

        buttonList.add(new GuiButton(0, scaledResolution.getScaledWidth() / 2 - 150, scaledResolution.getScaledHeight() - 23, 100, 20, "Add Alt"));
        buttonList.add(new GuiButton(1, scaledResolution.getScaledWidth() / 2 - 50, scaledResolution.getScaledHeight() - 23, 100, 20, "Delete Selected"));
        buttonList.add(new GuiButton(2, scaledResolution.getScaledWidth() / 2 + 50, scaledResolution.getScaledHeight() - 23, 100, 20, "Back"));

        altEntryOffset = 41;

        if (!Cosmos.INSTANCE.getAltManager().getAltEntries().isEmpty()) {
            getFirstAlt().setOffset(altEntryOffset);
        }
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
        scissorStack.pushScissor((scaledResolution.getScaledWidth() / 2) - 151, 40, (scaledResolution.getScaledWidth() / 2) + 151, scaledResolution.getScaledHeight() - 24);

        // Draw alt entries
        Cosmos.INSTANCE.getAltManager().getAltEntries().forEach(entry -> {
            entry.drawAltEntry(mouseX, mouseY);
        });
        
        // Stop scissoring
        scissorStack.popScissor();

        // Display 'No alts!' if there are no alt entries
        if (Cosmos.INSTANCE.getAltManager().getAltEntries().isEmpty()) {
            FontUtil.drawStringWithShadow("No alts!", (scaledResolution.getScaledWidth() / 2f) - FontUtil.getStringWidth("No alts!") / 2F + 0.75F, 50 - FontUtil.getFontHeight() / 2F + 2F, 0xFFFFFF);
        }

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
        if (Cosmos.INSTANCE.getAltManager().getAltEntries().isEmpty()) {
            return;
        }

        // Refresh offsets before running the rest of the code
        refreshOffsets();

        // Check that the mouse is over the available area
        if (mouseOver((scaledResolution.getScaledWidth() / 2f) - 151, 40, 302, scaledResolution.getScaledHeight() - 25, mouseX, mouseY)) {
            int scroll = Mouse.getDWheel();
            if (scroll < 0) {
                // Cancel if the last alt is fully visible
                if (getLastAlt().getOffset() - 1 < scaledResolution.getScaledHeight() - 49) {
                    return;
                }

                Cosmos.INSTANCE.getAltManager().getAltEntries().forEach(entry -> {
                    entry.setOffset(entry.getOffset() - 16);
                });
            }

            else if (scroll > 0) {
                // Cancel if the first alt is fully visible
                if (getFirstAlt().getOffset() >= 41) {
                    return;
                }

                Cosmos.INSTANCE.getAltManager().getAltEntries().forEach(entry -> {
                    entry.setOffset(entry.getOffset() + 16);
                });
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        switch (button.id) {
            case 0:
                // Display add alt gui
                mc.displayGuiScreen(new AddAltGUI(this));
                break;
            case 1:
                // If there is a selected entry, remove it
                if (getSelectedAltEntry() != null) {
                    // Remove the entry
                    Cosmos.INSTANCE.getAltManager().getAltEntries().remove(getSelectedAltEntry());

                    // Decrease the offset
                    altEntryOffset -= 32;

                    // Refresh positions of all other alts
                    if (!Cosmos.INSTANCE.getAltManager().getAltEntries().isEmpty()) {
                        getFirstAlt().setOffset(41);
                    }
                }
                break;
            case 2:
                // Display multiplayer gui
                mc.displayGuiScreen(lastScreen);
                break;
        }
    }

    /**
     * Called when the GUI is closed.
     */
    @Override
    public void onGuiClosed() {
        // Save the alts to alts.toml
        Cosmos.INSTANCE.getConfigManager().saveAlts();
    }

    /**
     * Refreshes the alt entries' offsets
     */
    private void refreshOffsets() {
        // Get the first alt's offset
        float altOffset = getFirstAlt().getOffset();

        for (AltEntry altEntry : Cosmos.INSTANCE.getAltManager().getAltEntries()) {
            // Set the offset to a new offset
            altEntry.setOffset(altOffset);
            // Increase offset for next entry
            altOffset += 32;
        }
    }

    /**
     * Gets the first alt
     * @return The first alt
     */
    private AltEntry getFirstAlt() {
        return Cosmos.INSTANCE.getAltManager().indexOf(0);
    }

    /**
     * There has got to be a better way of doing this...
     * Gets the last alt in the list
     * @return The last alt
     */
    private AltEntry getLastAlt() {
        return Cosmos.INSTANCE.getAltManager().indexOf(Cosmos.INSTANCE.getAltManager().getAltEntries().size() - 1);
    }

    /**
     * Gets the selected alt
     * @return The selected alt
     */
    private AltEntry getSelectedAltEntry() {
        for (AltEntry altEntry : Cosmos.INSTANCE.getAltManager().getAltEntries()) {
            // If the alt is selected, return the entry
            if (altEntry.isSelected) {
                return altEntry;
            }
        }

        return null;
    }

    /**
     * Sets the selected alt
     * @param newSelected The new selected alt
     */
    private void setSelectedAltEntry(AltEntry newSelected) {
        // Sets whether the alt is selected to whether it is the newSelected parameter
        for (AltEntry altEntry : Cosmos.INSTANCE.getAltManager().getAltEntries()) {
            altEntry.isSelected = altEntry == newSelected;
        }
    }

    /**
     * Called when a key is typed
     * @param typedChar The character typed
     * @param keyCode The keycode of the character
     * @throws IOException Input output exception
     */
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        // Display multiplayer GUI if we press escape
        if (keyCode == Keyboard.KEY_ESCAPE) {
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
        for (AltEntry altEntry : Cosmos.INSTANCE.getAltManager().getAltEntries()) {
            if (altEntry.isMouseOverButton(mouseX, mouseY)) {
                // Run only if it is already selected
                if (altEntry.isSelected) {
                    altEntry.whenClicked();
                }

                // Set it to be selected
                setSelectedAltEntry(altEntry);
            }
        }
    }

    /**
     * Linus, I don't know what you did with your isMouseOver method, but it didn't work :/ (so I did this instead)
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
