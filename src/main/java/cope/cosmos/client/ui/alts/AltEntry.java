package cope.cosmos.client.ui.alts;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import cope.cosmos.client.managment.managers.AltManager;
import cope.cosmos.utility.IUtility;
import cope.cosmos.utility.render.RenderUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.Map;

public class AltEntry implements IGuiListEntry, IUtility
{
	private final String email;
	private final String password;
	private final YggdrasilUserAuthentication auth;
	private final ResourceLocation unknown = new ResourceLocation("textures/misc/unknown_server.png");
	private final ResourceLocation selected = new ResourceLocation("textures/gui/world_selection.png");
	
	public AltEntry(String email, String password) {
		this.email = email;
		this.password = password;
		this.auth = AltManager.logIn(email, password, false);
	}
	
	@Override
	public void updatePosition(int slotIndex, int x, int y, float partialTicks) {
		
	}
	
	@Override
	public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
		try {
			mc.fontRenderer.drawStringWithShadow(this.auth.getSelectedProfile().getName(), x + 36, y + 2, -1);
			mc.fontRenderer.drawStringWithShadow(this.email, x + 36, y + 12, 0xFF888888);
			mc.fontRenderer.drawStringWithShadow("Premium", x + 36, y + 22, 0xFF55FF55);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			/* TODO fix skin resourcelocation */
			ResourceLocation resourcelocation = DefaultPlayerSkin.getDefaultSkinLegacy();
			Map<Type, MinecraftProfileTexture> map = mc.getSkinManager().loadSkinFromCache(this.auth.getSelectedProfile());
			if (map.containsKey(Type.SKIN)) {
				resourcelocation = mc.getSkinManager().loadSkin(map.get(Type.SKIN), Type.SKIN);
			}
			//mc.getSkinManager().loadSkin(mc.getSkinManager().loadSkinFromCache(this.auth.getSelectedProfile()).get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN, null)
			mc.getTextureManager().bindTexture(resourcelocation);
			GL11.glEnable(GL11.GL_BLEND);
			Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);
			GL11.glDisable(GL11.GL_BLEND);
			if(isSelected) {
				mc.getTextureManager().bindTexture(this.selected);
				RenderUtil.drawRect(x, y, 32, 32, -1601138544);
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				Gui.drawModalRectWithCustomSizedTexture(x - 6, y + 3, 32.0F, 3, 32, 32, 256.0F, 256.0F);
			}
		} catch (NullPointerException npe) {
			mc.fontRenderer.drawStringWithShadow("Unknown Account", x + 36, y + 2, 0xFFFF5555);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			mc.getTextureManager().bindTexture(this.unknown);
			GL11.glEnable(GL11.GL_BLEND);
			Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);
			GL11.glDisable(GL11.GL_BLEND);
			if(isSelected) {
				mc.getTextureManager().bindTexture(this.selected);
				RenderUtil.drawRect(x, y, 32, 32, -1601138544);
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				Gui.drawModalRectWithCustomSizedTexture(x - 6, y + 3, 32.0F, 3, 32, 32, 256.0F, 256.0F);
			}
		}
	}
	
	@Override
	public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
		if(relativeX <= 32 && relativeX < 32) {
			AltManager.logIn(this.email, this.password, true);
			return true;
		}
		return false;
	}
	
	@Override
	public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
		
	}

	public String getName() {
		return auth.getSelectedProfile().getName();
	}
	
	public String getEmail() {
		return this.email;
	}
	
	public String getPassword() {
		return this.password;
	}
}
