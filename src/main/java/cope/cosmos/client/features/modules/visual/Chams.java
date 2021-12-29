package cope.cosmos.client.features.modules.visual;

import cope.cosmos.client.events.RenderCrystalEvent;
import cope.cosmos.client.events.RenderLivingEntityEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.client.ColorUtil;
import cope.cosmos.util.world.EntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author LinusTouchTips
 * @author Gopro336
 * TODO: add shader chams
 */
@SuppressWarnings("unused")
public class Chams extends Module {
    public static Chams INSTANCE;

    public Chams() {
        super("Chams", Category.VISUAL, "Renders entity models through walls");
        INSTANCE = this;
    }

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.MODEL).setDescription("Mode for Chams");
    public static Setting<Shader> shader = new Setting<>("Shader", Shader.WATER).setDescription("Shader mode for Chams").setVisible(() -> mode.getValue().equals(Mode.SHADER));
    public static Setting<Double> width = new Setting<>("Width", 0.0, 3.0, 5.0, 2).setParent(mode).setDescription("Line width for the model").setVisible(() -> mode.getValue().equals(Mode.WIRE) || mode.getValue().equals(Mode.WIRE_MODEL));

    public static Setting<Boolean> players = new Setting<>("Players", true).setDescription("Renders chams on players");
    public static Setting<Boolean> local = new Setting<>("Local", false).setDescription("Renders chams on the local player").setParent(players);

    public static Setting<Boolean> mobs = new Setting<>("Mobs", true).setDescription("Renders chams on mobs");
    public static Setting<Boolean> monsters = new Setting<>("Monsters", true).setDescription("Renders chams on monsters");

    public static Setting<Boolean> crystals = new Setting<>("Crystals", true).setDescription("Renders chams on crystals");
    public static Setting<Float> scale = new Setting<>("Scale", 0.0F, 1.0F, 2.0F, 2).setDescription("Scale for crystal model").setParent(crystals);

    public static Setting<Boolean> texture = new Setting<>("Texture", false).setDescription("Enables entity texture");
    public static Setting<Boolean> lighting = new Setting<>("Lighting", true).setDescription("Disables vanilla lighting");
    public static Setting<Boolean> blend = new Setting<>("Blend", false).setDescription("Enables blended texture");
    public static Setting<Boolean> transparent = new Setting<>("Transparent", true).setDescription("Makes entity models transparent");
    public static Setting<Boolean> depth = new Setting<>("Depth", true).setDescription("Enables entity depth");
    public static Setting<Boolean> walls = new Setting<>("Walls", true).setDescription("Renders chams models through walls");

    public static Setting<Boolean> xqz = new Setting<>("XQZ", true).setDescription("Colors chams models through walls");
    public static Setting<Color> xqzColor = new Setting<>("XQZColor", new Color(0, 70, 250, 50)).setDescription("Color of models through walls").setParent(xqz);

    public static Setting<Boolean> highlight = new Setting<>("Highlight", true).setDescription("Colors chams models when visible");
    public static Setting<Color> highlightColor = new Setting<>("HighlightColor", new Color(250, 0, 250, 50)).setDescription("Color of models when visible").setParent(highlight);

    public static Setting<Boolean> glint = new Setting<>("Glint", false).setDescription("Renders enchantment glint on top of chams");
    public static Setting<Color> glintColor = new Setting<>("GlintColor", new Color(0, 70, 250, 50)).setParent(glint);
    public static Setting<Float> glintSpeed = new Setting<>("GlintSpeed", 0.1F, 4F, 10F, 2).setParent(glint);
    public static Setting<Float> glintScale = new Setting<>("GlintScale", 0.1F, 1F, 10F, 2).setParent(glint);
    public static Setting<Boolean> glintWalls = new Setting<>("Walls", false).setParent(glint);

    public static final ResourceLocation GLINT_TEX = new ResourceLocation("textures/misc/enchanted_item_glint.png");

    @SubscribeEvent
    public void onRenderLivingEntity(RenderLivingEntityEvent event) {
        if (nullCheck() && (event.getEntityLivingBase() instanceof EntityOtherPlayerMP && players.getValue() || (event.getEntityLivingBase() instanceof EntityPlayerSP && local.getValue()) || (EntityUtil.isPassiveMob(event.getEntityLivingBase()) || EntityUtil.isNeutralMob(event.getEntityLivingBase())) && mobs.getValue() || EntityUtil.isHostileMob(event.getEntityLivingBase()) && monsters.getValue())) {
            event.setCanceled(!texture.getValue());

            if (transparent.getValue())
                GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);

            glPushMatrix();
            glPushAttrib(GL_ALL_ATTRIB_BITS);

            if (!texture.getValue() && !mode.getValue().equals(Mode.SHINE))
                glDisable(GL_TEXTURE_2D);

            if (blend.getValue())
                glEnable(GL_BLEND);

            if (lighting.getValue())
                glDisable(GL_LIGHTING);

            if (depth.getValue())
                glDepthMask(false);

            if (walls.getValue())
                glDisable(GL_DEPTH_TEST);

            switch (mode.getValue()) {
                case WIRE:
                    glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
                    break;
                case WIRE_MODEL:
                case MODEL:
                    glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
                    break;
            }

            glEnable(GL_LINE_SMOOTH);
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
            glLineWidth((float) ((double) width.getValue()));

            if (xqz.getValue())
                ColorUtil.setColor(xqzColor.getValue());

            event.getModelBase().render(event.getEntityLivingBase(), event.getLimbSwing(), event.getLimbSwingAmount(), event.getAgeInTicks(), event.getNetHeadYaw(), event.getHeadPitch(), event.getScaleFactor());

            if (walls.getValue() && !mode.getValue().equals(Mode.WIRE_MODEL))
                glEnable(GL_DEPTH_TEST);

            if (mode.getValue().equals(Mode.WIRE_MODEL))
                glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

            if (highlight.getValue())
                ColorUtil.setColor(mode.getValue().equals(Mode.WIRE_MODEL) ? new Color(xqzColor.getValue().getRed(), xqzColor.getValue().getGreen(), xqzColor.getValue().getBlue(), 255) : highlightColor.getValue());

            event.getModelBase().render(event.getEntityLivingBase(), event.getLimbSwing(), event.getLimbSwingAmount(), event.getAgeInTicks(), event.getNetHeadYaw(), event.getHeadPitch(), event.getScaleFactor());

            if (walls.getValue() && mode.getValue().equals(Mode.WIRE_MODEL))
                glEnable(GL_DEPTH_TEST);

            if (lighting.getValue())
                glEnable(GL_LIGHTING);

            if (depth.getValue())
                glDepthMask(true);

            if (blend.getValue())
                glDisable(GL_BLEND);

            if (!texture.getValue() && !mode.getValue().equals(Mode.SHINE))
                glEnable(GL_TEXTURE_2D);

            glPopAttrib();
            glPopMatrix();

            if (glint.getValue()) {
                GL11.glPushMatrix();
                GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glEnable(GL11.GL_BLEND);

                Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(GLINT_TEX);
                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL_FILL);
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glColor4f(glintColor.getValue().getRed() / 255F, glintColor.getValue().getGreen() / 255F, glintColor.getValue().getBlue() / 255F, glintColor.getValue().getAlpha() / 255F);
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);

                for (int i = 0; i < 2; ++i) {
                    GlStateManager.matrixMode(GL11.GL_TEXTURE);
                    GlStateManager.loadIdentity();
                    float textureScale = 0.33333334F * glintScale.getValue();
                    GlStateManager.scale(textureScale, textureScale, textureScale);
                    GlStateManager.rotate(30.0F - (float)i * 60.0F, 0.0F, 0.0F, 1.0F);
                    GlStateManager.translate(0.0F, (event.getEntityLivingBase().ticksExisted + mc.getRenderPartialTicks()) * (0.001F + (float)i * 0.003F) * glintSpeed.getValue(), 0.0F);
                    GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                    GL11.glTranslatef(0.0f, 0.0f, 0.0f);
                    GlStateManager.color(glintColor.getValue().getRed() / 255f, glintColor.getValue().getGreen() / 255f, glintColor.getValue().getBlue() / 255f, glintColor.getValue().getAlpha() / 255f);
                    if (glintWalls.getValue()) {
                        GL11.glDepthMask(true);
                        GL11.glEnable(GL_DEPTH_TEST);
                    }
                    event.getModelBase().render(event.getEntityLivingBase(), event.getLimbSwing(), event.getLimbSwingAmount(), event.getAgeInTicks(), event.getNetHeadYaw(), event.getHeadPitch(), event.getScaleFactor());
                    if (glintWalls.getValue()) {
                        GL11.glDisable(GL_DEPTH_TEST);
                        GL11.glDepthMask(false);
                    }
                }

                GlStateManager.matrixMode(5890);
                GlStateManager.loadIdentity();
                GlStateManager.matrixMode(5888);

                GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

                GlStateManager.color(1F, 1F, 1F, 1F);
                GL11.glPopAttrib();
                GL11.glPopMatrix();
            }
        }
    }

    @SubscribeEvent
    public void onRenderCrystalPre(RenderCrystalEvent.RenderCrystalPreEvent event) {
        event.setCanceled(crystals.getValue());
    }

    @SubscribeEvent
    public void onRenderCrystalPost(RenderCrystalEvent.RenderCrystalPostEvent event) {
        if (nullCheck() && crystals.getValue()) {
            if (transparent.getValue())
                GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);

            glPushMatrix();
            glPushAttrib(GL_ALL_ATTRIB_BITS);

            float rotation = event.getEntityEnderCrystal().innerRotation + event.getPartialTicks();
            float rotationMoved = MathHelper.sin(rotation * 0.2F) / 2 + 0.5F;
            rotationMoved += Math.pow(rotationMoved, 2);

            glTranslated(event.getX(), event.getY(), event.getZ());
            glScaled(scale.getValue(), scale.getValue(), scale.getValue());

            if (!texture.getValue() && !mode.getValue().equals(Mode.SHINE))
                glDisable(GL_TEXTURE_2D);

            if (blend.getValue())
                glEnable(GL_BLEND);

            if (lighting.getValue())
                glDisable(GL_LIGHTING);

            if (depth.getValue())
                glDepthMask(false);

            if (walls.getValue())
                glDisable(GL_DEPTH_TEST);

            switch (mode.getValue()) {
                case WIRE:
                    glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
                    break;
                case WIRE_MODEL:
                case MODEL:
                    glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
                    break;
            }

            glEnable(GL_LINE_SMOOTH);
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
            glLineWidth((float) ((double) width.getValue()));

            if (xqz.getValue())
                ColorUtil.setColor(xqzColor.getValue());

            if (event.getEntityEnderCrystal().shouldShowBottom())
                event.getModelBase().render(event.getEntityEnderCrystal(), 0, rotation * 3, rotationMoved * 0.2F, 0, 0, 0.0625F);
            else
                event.getModelNoBase().render(event.getEntityEnderCrystal(), 0, rotation * 3, rotationMoved * 0.2F, 0, 0, 0.0625F);

            if (walls.getValue() && !mode.getValue().equals(Mode.WIRE_MODEL))
                glEnable(GL_DEPTH_TEST);

            if (mode.getValue().equals(Mode.WIRE_MODEL))
                glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

            if (highlight.getValue())
                ColorUtil.setColor(mode.getValue().equals(Mode.WIRE_MODEL) ? new Color(xqzColor.getValue().getRed(), xqzColor.getValue().getGreen(), xqzColor.getValue().getBlue(), 255) : highlightColor.getValue());

            if (event.getEntityEnderCrystal().shouldShowBottom())
                event.getModelBase().render(event.getEntityEnderCrystal(), 0, rotation * 3, rotationMoved * 0.2F, 0, 0, 0.0625F);
            else
                event.getModelNoBase().render(event.getEntityEnderCrystal(), 0, rotation * 3, rotationMoved * 0.2F, 0, 0, 0.0625F);

            if (walls.getValue() && mode.getValue().equals(Mode.WIRE_MODEL))
                glEnable(GL_DEPTH_TEST);

            if (lighting.getValue())
                glEnable(GL_LIGHTING);

            if (depth.getValue())
                glDepthMask(true);

            if (blend.getValue())
                glDisable(GL_BLEND);

            if (!texture.getValue() && !mode.getValue().equals(Mode.SHINE))
                glEnable(GL_TEXTURE_2D);

            glScaled(1 / scale.getValue(), 1 / scale.getValue(), 1 / scale.getValue());

            glPopAttrib();
            glPopMatrix();

            if (glint.getValue()) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(event.getX(), event.getY(), event.getZ());
                Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(GLINT_TEX);
                GL11.glPushAttrib(1048575);
                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL_FILL);
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glColor4f(glintColor.getValue().getRed() / 255F, glintColor.getValue().getGreen() / 255F, glintColor.getValue().getBlue() / 255F, glintColor.getValue().getAlpha() / 255F);
                GL11.glScalef(scale.getValue(), scale.getValue(), scale.getValue());
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);

                for (int i = 0; i < 2; ++i) {
                    GlStateManager.matrixMode(GL11.GL_TEXTURE);
                    GlStateManager.loadIdentity();
                    float textureScale = 0.33333334F * glintScale.getValue();
                    GlStateManager.scale(textureScale, textureScale, textureScale);
                    GlStateManager.rotate(30.0F - (float)i * 60.0F, 0.0F, 0.0F, 1.0F);
                    GlStateManager.translate(0.0F, (event.getEntityEnderCrystal().ticksExisted + event.getPartialTicks()) * (0.001F + (float)i * 0.003F) * glintSpeed.getValue(), 0.0F);
                    GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                    if (glintWalls.getValue()) {
                        GL11.glDepthMask(true);
                        GL11.glEnable(GL_DEPTH_TEST);
                    }

                    if (event.getEntityEnderCrystal().shouldShowBottom())
                        event.getModelBase().render(event.getEntityEnderCrystal(), 0, rotation * 3, rotationMoved * 0.2F, 0, 0, 0.0625F);
                    else
                        event.getModelNoBase().render(event.getEntityEnderCrystal(), 0, rotation * 3, rotationMoved * 0.2F, 0, 0, 0.0625F);

                    if (glintWalls.getValue()) {
                        GL11.glDisable(GL_DEPTH_TEST);
                        GL11.glDepthMask(false);
                    }
                }

                GlStateManager.matrixMode(5890);
                GlStateManager.loadIdentity();
                GlStateManager.matrixMode(5888);

                GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                GL11.glScalef(1F / scale.getValue(), 1F / scale.getValue(), 1F / scale.getValue());
                GL11.glPopAttrib();
                GL11.glPopMatrix();
            }
        }
    }

    public enum Mode {
        MODEL, WIRE, WIRE_MODEL, SHINE, SHADER
    }

    public enum Shader {
        WATER
    }
}