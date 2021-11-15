package cope.cosmos.client.clickgui.util;

import cope.cosmos.asm.mixins.accessor.IShaderGroup;
import cope.cosmos.util.Wrapper;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * @author Gopro336
 */
public class BlurUtil implements Wrapper {

    private static final ResourceLocation BLUR = new ResourceLocation("shader/blur/blur.json");
    private static ShaderGroup blurShader;
    private static Framebuffer framebuffer;
    private static int lastScale;
    private static int lastScaleWidth;
    private static int lastScaleHeight;

    public static void initShaderAndFBuffer() {
        try {
            blurShader = new ShaderGroup(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), BLUR);
            blurShader.createBindFramebuffers(mc.displayWidth, mc.displayHeight);
            framebuffer = ((IShaderGroup) blurShader).getMainFramebuffer();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void configureShader(float intensity, float blurWidth, float blurHeight) {
        ((IShaderGroup) blurShader).getListShaders().get(0).getShaderManager().getShaderUniform("Radius").set(intensity);
        ((IShaderGroup) blurShader).getListShaders().get(1).getShaderManager().getShaderUniform("Radius").set(intensity);
        ((IShaderGroup) blurShader).getListShaders().get(0).getShaderManager().getShaderUniform("BlurDir").set(blurWidth, blurHeight);
        ((IShaderGroup) blurShader).getListShaders().get(1).getShaderManager().getShaderUniform("BlurDir").set(blurHeight, blurWidth);
    }

    public static void blurRect(int x, int y, int width, int height, float intensity, float blurWidth, float blurHeight) {
        ScaledResolution resolution = new ScaledResolution(mc);
        initShaderAndFBuffer();

        int scaleFactor = resolution.getScaleFactor();
        int widthFactor = resolution.getScaledWidth();
        int heightFactor = resolution.getScaledHeight();

        if (lastScale != scaleFactor
                || lastScaleWidth != widthFactor
                || lastScaleHeight != heightFactor
                || framebuffer == null
                || blurShader == null) {
            initShaderAndFBuffer();
        }

        lastScale = scaleFactor;
        lastScaleWidth = widthFactor;
        lastScaleHeight = heightFactor;

        if (!OpenGlHelper.isFramebufferEnabled()) return;

        GL11.glScissor(
                x * scaleFactor,
                (mc.displayHeight - (y * scaleFactor) - height * scaleFactor),
                width * scaleFactor,
                height * scaleFactor - 12
        );

        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        configureShader(intensity, blurWidth, blurHeight);
        framebuffer.bindFramebuffer(true);
        blurShader.render(mc.getRenderPartialTicks());
        mc.getFramebuffer().bindFramebuffer(true);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ZERO, GL11.GL_ONE);
        framebuffer.framebufferRenderExt(mc.displayWidth, mc.displayHeight, false);
        GlStateManager.disableBlend();
        GL11.glScalef(scaleFactor, scaleFactor, 0);
    }
}