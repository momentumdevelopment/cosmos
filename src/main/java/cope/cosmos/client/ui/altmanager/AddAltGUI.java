package cope.cosmos.client.ui.altmanager;

import cope.cosmos.util.render.FontUtil;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
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
    private GuiTextField emailField;
    // The password text box
    private GuiTextField passwordField;

    private Alt.AltType currentType = Alt.AltType.Microsoft;

    public AddAltGUI(GuiScreen lastScreen) {
        this.lastScreen = lastScreen;
    }

    @Override
    public void initGui() {
        // Email and password
        this.emailField = new GuiTextField(1, mc.fontRenderer, this.width / 2 - 100, this.height / 2 - 100, 200, 15);
        this.passwordField = new GuiTextField(2, mc.fontRenderer, this.width / 2 - 100, this.height / 2 - 80, 200, 15);

        // Add and cancel
        this.buttonList.add(new GuiButton(3, this.width / 2 - 51, this.height / 2 - 60, 50, 20, "Add"));
        this.buttonList.add(new GuiButton(4, this.width / 2 + 1, this.height / 2 - 60, 50, 20, "Cancel"));

        ScaledResolution scaledResolution = new ScaledResolution(mc);

        // Alt type
        this.buttonList.add(new GuiButton(5, 3, scaledResolution.getScaledHeight() / 2 - 30, 100, 20, "Use Microsoft"));
        this.buttonList.add(new GuiButton(6, 3, scaledResolution.getScaledHeight() / 2 - 10, 100, 20, "Use Mojang"));
        this.buttonList.add(new GuiButton(7, 3, scaledResolution.getScaledHeight() / 2 + 10, 100, 20, "Use Cracked"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        // Draw dirt background
        drawDefaultBackground();

        // Title
        drawCenteredString(mc.fontRenderer, "Add Alt Account", scaledResolution.getScaledWidth() / 2, 10, 0xFFFFFF);

        // Draw email text box
        emailField.drawTextBox();
        if(emailField.getText().equals(""))
            FontUtil.drawStringWithShadow(TextFormatting.GRAY + "Email", emailField.x + 3, emailField.y + 3, -1);

        // Draw password text box
        passwordField.drawTextBox();
        if(passwordField.getText().equals(""))
            FontUtil.drawStringWithShadow(TextFormatting.GRAY + "Password", passwordField.x + 3, passwordField.y + 3, -1);

        FontUtil.drawStringWithShadow("Current: " + TextFormatting.GRAY + currentType.name(), 3, scaledResolution.getScaledHeight() / 2f + 35, 0xFFFFFF);

        // Draw buttons
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        switch (button.id) {
            case 3:
                // Add new alt
                if(!(emailField.getText().isEmpty())) {
                    AltManagerGUI.altEntries.add(new AltEntry(new Alt(emailField.getText(), passwordField.getText(), currentType), AltManagerGUI.altEntryOffset));
                    AltManagerGUI.altEntryOffset += 32;
                    mc.displayGuiScreen(lastScreen);
                }
                break;

            case 4: mc.displayGuiScreen(lastScreen); break;

            case 5: currentType = Alt.AltType.Microsoft; break;
            case 6: currentType = Alt.AltType.Mojang; break;
            case 7: currentType = Alt.AltType.Cracked; break;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        emailField.mouseClicked(mouseX, mouseY, mouseButton);
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
        if(keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(lastScreen);
            return;
        }

        emailField.textboxKeyTyped(typedChar, keyCode);
        passwordField.textboxKeyTyped(typedChar, keyCode);

        if(keyCode == Keyboard.KEY_RETURN) {
            this.emailField.setFocused(false);
            this.passwordField.setFocused(false);
        }

        super.keyTyped(typedChar, keyCode);
    }
}
