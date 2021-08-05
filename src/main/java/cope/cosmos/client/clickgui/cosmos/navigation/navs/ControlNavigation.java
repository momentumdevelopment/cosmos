package cope.cosmos.client.clickgui.cosmos.navigation.navs;

import cope.cosmos.client.clickgui.cosmos.navigation.Navigation;
import cope.cosmos.client.clickgui.util.Util;
import cope.cosmos.client.manager.managers.AnimationManager;
import cope.cosmos.util.Wrapper;
import cope.cosmos.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class ControlNavigation extends Navigation implements Wrapper, Util {

    private int hoverAnimation = 0;

    public AnimationManager animationManager;
    public boolean open;

    public ControlNavigation() {
        // should be closed when opening gui
        open = false;
        animationManager = new AnimationManager(300, false);
    }

    @Override
    public void drawNavigation() {
        SCREEN_WIDTH = new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth();
        SCREEN_HEIGHT = new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight();

        glPushMatrix();

        float halfWidth = SCREEN_WIDTH / 2;

        // hover animation
        if (mouseOver(halfWidth - 15, 25, 30, 20) && hoverAnimation < 5)
            hoverAnimation += 1;

        else if (!mouseOver(halfWidth - 15, 25, 30, 20) && hoverAnimation > 0)
            hoverAnimation -= 1;

        // background
        RenderUtil.drawRect(0, SCREEN_HEIGHT - 30, SCREEN_WIDTH, 30, new Color(23, 23, 29));

        // dropdown button
        // RenderUtil.drawRect(0, 20, 30, 3, Color.WHITE);
        // RenderUtil.drawTriangle(halfWidth, 24 + hoverAnimation, 3, 180, -1);

        // logo
        glColor4d(1, 1, 1, 1);
        mc.getTextureManager().bindTexture(new ResourceLocation("cosmos", "textures/imgs/logotransparent.png"));
        GuiScreen.drawModalRectWithCustomSizedTexture(2, (int) (SCREEN_HEIGHT - 28), 0, 0, 104, 26, 104, 26);

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
