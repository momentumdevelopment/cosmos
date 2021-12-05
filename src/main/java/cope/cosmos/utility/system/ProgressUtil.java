package cope.cosmos.utility.system;

import cope.cosmos.utility.IUtility;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;

public class ProgressUtil implements IUtility {

    public static void drawSplash(TextureManager textureManager) {
        ScaledResolution scaledresolution = new ScaledResolution(mc);
        Framebuffer framebuffer = new Framebuffer(scaledresolution.getScaledWidth() * scaledresolution.getScaleFactor(), scaledresolution.getScaledHeight() * scaledresolution.getScaleFactor(), true);
        framebuffer.bindFramebuffer(false);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0, scaledresolution.getScaledWidth(), scaledresolution.getScaledHeight(), 0, 1000, 3000);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0, 0, -2000);
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        GlStateManager.disableDepth();
        GlStateManager.enableTexture2D();
        textureManager.bindTexture(new ResourceLocation("panels", "textures/splash.jpg"));
        Tessellator.getInstance().getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        Tessellator.getInstance().getBuffer().pos(0, mc.displayHeight, 0).tex(0, 0).color(255, 255, 255, 255).endVertex();
        Tessellator.getInstance().getBuffer().pos( mc.displayWidth, mc.displayHeight, 0).tex(0, 0).color(255, 255, 255, 255).endVertex();
        Tessellator.getInstance().getBuffer().pos(mc.displayWidth, 0, 0).tex(0, 0).color(255, 255, 255, 255).endVertex();
        Tessellator.getInstance().getBuffer().pos(0, 0, 0).tex(0, 0).color(255, 255, 255, 255).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.color(1, 1, 1, 1);
        mc.draw((scaledresolution.getScaledWidth() - 256) / 2, (scaledresolution.getScaledHeight() - 256) / 2, 0, 0, 256, 256, 255, 255, 255, 255);
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        framebuffer.unbindFramebuffer();
        framebuffer.framebufferRender(scaledresolution.getScaledWidth() * scaledresolution.getScaleFactor(), scaledresolution.getScaledHeight() * scaledresolution.getScaleFactor());
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        mc.updateDisplay();
    }
}
