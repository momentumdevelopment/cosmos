package cope.cosmos.client.clickgui.cosmos.navigation.navs;

import cope.cosmos.client.clickgui.cosmos.navigation.Navigation;
import cope.cosmos.client.clickgui.util.GUIUtil;
import cope.cosmos.client.features.modules.client.ClickGUI;
import cope.cosmos.util.Wrapper;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class PlayerNavigation extends Navigation implements Wrapper, GUIUtil {

    @Override
    public void drawNavigation() {
        SCREEN_WIDTH = new ScaledResolution(mc).getScaledWidth();
        SCREEN_HEIGHT = new ScaledResolution(mc).getScaledHeight();

        glPushMatrix();

        // background
        RenderUtil.drawRect(0, 0, SCREEN_WIDTH, 30, new Color(23, 23, 29));

        float scaledWidth = ((FontUtil.getStringWidth(mc.player.getName()) + 8) * 2.75F);

        // info background
        RenderUtil.drawRect(0, 0, scaledWidth, 40, new Color(23, 23, 29));
        RenderUtil.drawRect(0, 40, scaledWidth, 3, new Color(ClickGUI.INSTANCE.getPrimaryColor().getRed(), ClickGUI.INSTANCE.getPrimaryColor().getGreen(), ClickGUI.INSTANCE.getPrimaryColor().getBlue()));

        // player info
        glScaled(2.75, 2.75, 2.75); {
            float scaledX = 7 * 0.36363636F;
            float scaledY = 9 * 0.36363636F;
            FontUtil.drawStringWithShadow(mc.player.getName(), scaledX, scaledY, new Color(ClickGUI.INSTANCE.getPrimaryColor().getRed(), ClickGUI.INSTANCE.getPrimaryColor().getGreen(), ClickGUI.INSTANCE.getPrimaryColor().getBlue()).getRGB());
        }

        glScaled(0.36363636F, 0.36363636F, 0.36363636F);

        // player info
        glScaled(1.35, 1.35, 1.35); {
            float scaledX = (SCREEN_WIDTH - FontUtil.getStringWidth("GUI") - 25) * 0.74074074F;
            float scaledY = 7 * 0.74074074F;
            FontUtil.drawStringWithShadow("GUI", scaledX, scaledY, -1);
        }

        glScaled(0.74074074, 0.74074074, 0.74074074);

        RenderUtil.drawRect(SCREEN_WIDTH - (FontUtil.getStringWidth("GUI") * 1.35F) - 21.5F, 20, (FontUtil.getStringWidth("GUI") * 1.35F) + 3, 2, Color.WHITE);

        glPopMatrix();
    }

    @Override
    public void handleLeftClick(int mouseX, int mouseY) {

    }

    @Override
    public void handleRightClick(int mouseX, int mouseY) {

    }

    @Override
    public void handleKeyPress(char typedCharacter, int key) {

    }

    @Override
    public void handleScroll(int scroll) {

    }
}
