package cope.cosmos.client.manager.managers;

import cope.cosmos.client.manager.Manager;
import cope.cosmos.util.Wrapper;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;

import static org.lwjgl.opengl.GL11.*;

public class ProgressManager extends Manager implements Wrapper {
    public ProgressManager() {
        super("ProgressManager", "Renders the client custom splash screen");
    }

    private static ResourceLocation splash;
    private static TextureManager textureManager;

    public static void update() {
        if (mc == null || mc.getLanguageManager() == null)
            return;
        
        drawSplash(mc.getTextureManager());
    }
    
    public static void setProgress() {
        update();
    }
    
    public static void drawSplash(TextureManager mcTextureManager) {
        if (textureManager == null)
            textureManager = mcTextureManager;

        ScaledResolution scaledresolution;
        int scaleFactor;

        try {
            scaledresolution = new ScaledResolution(mc);
            scaleFactor = scaledresolution.getScaleFactor();

            Framebuffer framebuffer = new Framebuffer(scaledresolution.getScaledWidth() * scaleFactor, scaledresolution.getScaledHeight() * scaleFactor, true);
            framebuffer.bindFramebuffer(false);

            GlStateManager.matrixMode(GL_PROJECTION);
            GlStateManager.loadIdentity();
            GlStateManager.ortho(0.0D, scaledresolution.getScaledWidth(), scaledresolution.getScaledHeight(), 0.0D, 1000.0D, 3000.0D);
            GlStateManager.matrixMode(GL_MODELVIEW);
            GlStateManager.loadIdentity();
            GlStateManager.translate(0.0F, 0.0F, -2000.0F);
            GlStateManager.disableLighting();
            GlStateManager.disableFog();
            GlStateManager.disableDepth();
            GlStateManager.enableTexture2D();

            if (splash == null)
                splash = new ResourceLocation("cosmos", "textures/imgs/splash.jpg");

            mcTextureManager.bindTexture(splash);

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            Gui.drawScaledCustomSizeModalRect(0, 0, 0, 0, 1920, 1080, scaledresolution.getScaledWidth(), scaledresolution.getScaledHeight(), 1920, 1080);

            framebuffer.unbindFramebuffer();
            framebuffer.framebufferRender(scaledresolution.getScaledWidth() * scaleFactor, scaledresolution.getScaledHeight() * scaleFactor);

            GlStateManager.enableAlpha();
            GlStateManager.alphaFunc(GL_GREATER, 0.1F);

            mc.updateDisplay();
        } catch (Exception ignored) {

        }
    }
}
