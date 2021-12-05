package cope.cosmos.client.alts;

import cope.cosmos.client.manager.managers.AltManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class AltCreatorGUI extends GuiScreen
{
	private final GuiScreen lastGui;
	private GuiTextField passField;
	private GuiTextField emailField;
	
	public AltCreatorGUI(GuiScreen lastGui) {
		this.lastGui = lastGui;
	}
	
	@Override
	public void initGui() {
		this.emailField = new GuiTextField(1, mc.fontRenderer, this.width / 2 - 100, this.height / 2 - 100, 200, 15);
		this.passField = new GuiTextField(2, mc.fontRenderer, this.width / 2 - 100, this.height / 2 - 80, 200, 15);
		this.buttonList.add(new GuiButton(3, this.width / 2 - 51, this.height / 2 - 60, 50, 20, "Add"));
		this.buttonList.add(new GuiButton(4, this.width / 2 + 1, this.height / 2 - 60, 50, 20, "Cancel"));
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.drawCenteredString(mc.fontRenderer, "Add Alt Account", this.width / 2, 10, -1);
		this.emailField.drawTextBox();
		this.passField.drawTextBox();
		if(this.emailField.getText().equals("") && !this.emailField.isFocused()) {
			this.fontRenderer.drawStringWithShadow("Email", this.width / 2 - 97, this.height / 2 - 96, 0xFF999999);
		}
		if(this.passField.getText().equals("") && !this.passField.isFocused()) {
			this.fontRenderer.drawStringWithShadow("Password", this.width/2 - 97, this.height/2 - 76, 0xFF999999);
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void keyTyped(char typedChar, int keyCode) throws IOException {
		if(keyCode == Keyboard.KEY_ESCAPE) {
			mc.displayGuiScreen(this.lastGui);
			return;
		}
		this.emailField.textboxKeyTyped(typedChar, keyCode);
		this.passField.textboxKeyTyped(typedChar, keyCode);
		if(keyCode == Keyboard.KEY_RETURN) {
			this.emailField.setFocused(false);
			this.passField.setFocused(false);
		}
		super.keyTyped(typedChar, keyCode);
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		this.emailField.mouseClicked(mouseX, mouseY, mouseButton);
		this.passField.mouseClicked(mouseX, mouseY, mouseButton);
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		switch(button.id) {
			case 3 : {
				AltEntry alt = new AltEntry(this.emailField.getText(), this.passField.getText());
				AltManager.getAlts().add(alt);
				mc.displayGuiScreen(this.lastGui);
				/* TODO add alts via configging */
				break;
			}
			case 4 : {
				mc.displayGuiScreen(this.lastGui);
				break;
			}
			default : break;
		}
	}
}
