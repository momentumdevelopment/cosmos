package cope.cosmos.client.ui.altmanager;

import cope.cosmos.client.manager.managers.AltManager;
import cope.cosmos.util.render.FontUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class AddAltGUI extends GuiScreen {

    // The last screen (which will always be AltManagerGUI) for easy switching back
    private final GuiScreen lastScreen;
    // The email text box
    private GuiTextField loginField;
    // The password text box
    private GuiTextField passwordField;
    // The current type that is selected (MS by default)
    private Alt.AltType currentType = Alt.AltType.Microsoft;

    public AddAltGUI(GuiScreen lastScreen) {
        this.lastScreen = lastScreen;
    }

    @Override
    public void initGui() {
        // Email and password
        this.loginField = new GuiTextField(1, mc.fontRenderer, this.width / 2 - 100, this.height / 2 - 42, 200, 15);
        this.passwordField = new GuiTextField(2, mc.fontRenderer, this.width / 2 - 100, this.height / 2 - 20, 200, 15);

        // Add and cancel
        this.buttonList.add(new GuiButton(3, this.width / 2 - 51, this.height / 2 + 8, 50, 20, "Add"));
        this.buttonList.add(new GuiButton(4, this.width / 2 + 1, this.height / 2 + 8, 50, 20, "Cancel"));

        // Alt type
        this.buttonList.add(new GuiButton(5, this.width / 2 - 210, this.height / 2 - 42, 100, 20, "Use Microsoft"));
        this.buttonList.add(new GuiButton(6, this.width / 2 - 210, this.height / 2 - 21, 100, 20, "Use Mojang"));
        this.buttonList.add(new GuiButton(7, this.width / 2 - 210, this.height / 2, 100, 20, "Use Cracked"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        // Draw background
        drawDefaultBackground();

        // Title
        drawCenteredString(mc.fontRenderer, "Add Alt Account", scaledResolution.getScaledWidth() / 2, 10, 0xFFFFFF);

        // Draw email text box
        loginField.drawTextBox();
        if(loginField.getText().equals("") && !loginField.isFocused() && loginField.getVisible())
            FontUtil.drawStringWithShadow(TextFormatting.GRAY + "Login", loginField.x + 3, loginField.y + 3.5f, -1);

        // Draw password text box
        passwordField.drawTextBox();
        if(passwordField.getText().equals("") && !passwordField.isFocused() && passwordField.getVisible())
            FontUtil.drawStringWithShadow(TextFormatting.GRAY + "Password", passwordField.x + 3, passwordField.y + 3.5f, -1);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        switch (button.id) {
            case 3:
                // Add new alt
                if(!(loginField.getText().isEmpty())) {
                    // Add alt
                    AltManager.getAltEntries().add(new AltEntry(new Alt(loginField.getText(), passwordField.getText(), currentType), AltManagerGUI.altEntryOffset));
                    // Increase offset
                    AltManagerGUI.altEntryOffset += 32;
                    // Display alt manager
                    mc.displayGuiScreen(lastScreen);
                }
                break;

            case 4:
                // Display Alt Manager GUI
                mc.displayGuiScreen(lastScreen);
                break;

            case 5:
                // Set type to Microsoft
                currentType = Alt.AltType.Microsoft;
                this.loginField.setVisible(true);
                this.passwordField.setVisible(true);
                break;
            case 6:
                // Set type to Mojang
                currentType = Alt.AltType.Mojang;
                this.loginField.setVisible(true);
                this.passwordField.setVisible(true);
                break;
            case 7:
                // Set type to Cracked
                currentType = Alt.AltType.Cracked;
                this.loginField.setVisible(true);
                this.passwordField.setVisible(false);
                break;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        // Text field clicked
        loginField.mouseClicked(mouseX, mouseY, mouseButton);
        passwordField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * Called when a key is typed
     * @param typedChar The character typed
     * @param keyCode The keycode of the character
     * @throws IOException Input output exception
     */
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        // Display the alt manager if we press escape
        if(keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(lastScreen);
            return;
        }

        // Text fields
        loginField.textboxKeyTyped(typedChar, keyCode);
        passwordField.textboxKeyTyped(typedChar, keyCode);

        // Set unfocused if we press enter / return
        if(keyCode == Keyboard.KEY_RETURN) {
            this.loginField.setFocused(false);
            this.passwordField.setFocused(false);
        }

        super.keyTyped(typedChar, keyCode);
    }
}