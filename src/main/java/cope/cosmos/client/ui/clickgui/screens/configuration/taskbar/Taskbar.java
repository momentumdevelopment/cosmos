package cope.cosmos.client.ui.clickgui.screens.configuration.taskbar;

import cope.cosmos.client.ui.clickgui.screens.DrawableComponent;
import cope.cosmos.client.ui.clickgui.screens.configuration.component.ClickType;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.string.ColorUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author linustouchtips
 * @since 02/19/2022
 */
public class Taskbar extends DrawableComponent {

    @Override
    public void drawComponent() {
        ScaledResolution resolution = new ScaledResolution(mc);

        // taskbar
        RenderUtil.drawRect(0, resolution.getScaledHeight() - 34, resolution.getScaledWidth(), 34, new Color(23, 23, 29));

        glPushMatrix();

        glColor4d(1, 1, 1, 1);

        // logo
        mc.getTextureManager().bindTexture(new ResourceLocation("cosmos", "textures/imgs/logotransparent.png"));
        GuiScreen.drawModalRectWithCustomSizedTexture(resolution.getScaledWidth() - 104, resolution.getScaledHeight() - 31, 0, 0, 104, 28, 104, 28);

        glPopMatrix();

        glPushMatrix();

        // player name scaled width
        float scaledWidth = (FontUtil.getStringWidth(mc.player.getName()) + 8) * 2.75F;

        RenderUtil.drawRect(0, resolution.getScaledHeight() - 44, scaledWidth, 44, new Color(23, 23, 29));

        // player info
        glScaled(2.75, 2.75, 2.75); {
            float scaledX = 7 * 0.36363636F;
            float scaledY = (resolution.getScaledHeight() - 35) * 0.36363636F;
            FontUtil.drawStringWithShadow(mc.player.getName(), scaledX, scaledY, ColorUtil.getPrimaryColor().getRGB());
        }

        glScaled(0.36363636F, 0.36363636F, 0.36363636F);

        glPopMatrix();
    }

    @Override
    public void onClick(ClickType in) {

    }

    @Override
    public void onType(int in) {

    }

    @Override
    public void onScroll(int in) {

    }
}
