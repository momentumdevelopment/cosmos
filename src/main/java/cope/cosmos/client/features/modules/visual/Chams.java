package cope.cosmos.client.features.modules.visual;

import cope.cosmos.client.events.RenderCrystalEvent;
import cope.cosmos.client.events.RenderLivingEntityEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.shader.Shader;
import cope.cosmos.client.shader.shaders.*;
import cope.cosmos.util.world.EntityUtil;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glUseProgram;

/**
 * @author linustouchtips, Gopro336
 * @since 06/08/2021
 */
@SuppressWarnings("unused")
public class Chams extends Module {
    public static Chams INSTANCE;

    public Chams() {
        super("Chams", Category.VISUAL, "Renders entity models through walls");
        INSTANCE = this;
    }

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.MODEL).setDescription("Mode for Chams");

    public static Setting<ChamsShader> shaderMode = new Setting<>("Shader", ChamsShader.COSMOS).setDescription("Shader for Chams");

    public static Setting<Float> shaderSpeed = new Setting<>("ShaderSpeed", 0.001f, 0.005f, 0.08f, 4).setVisible(() -> mode.getValue().equals(Mode.SHADER));

    // entity highlights
    public static Setting<Boolean> players = new Setting<>("Players", true).setDescription("Renders chams on players");
    public static Setting<Boolean> local = new Setting<>("Local", false).setDescription("Renders chams on the local player").setParent(players);
    public static Setting<Boolean> mobs = new Setting<>("Mobs", true).setDescription("Renders chams on mobs");
    public static Setting<Boolean> monsters = new Setting<>("Monsters", true).setDescription("Renders chams on monsters");
    public static Setting<Boolean> crystals = new Setting<>("Crystals", true).setDescription("Renders chams on crystals");
    public static Setting<Double> scale = new Setting<>("Scale", 0.0, 1.0, 2.0, 2).setDescription("Scale for crystal model").setParent(crystals);

    // render options
    public static Setting<Double> width = new Setting<>("Width", 0.0, 3.0, 5.0, 2).setDescription("Line width for the model").setVisible(() -> mode.getValue().equals(Mode.WIRE) || mode.getValue().equals(Mode.WIRE_MODEL));
    public static Setting<Boolean> texture = new Setting<>("Texture", false).setDescription("Enables entity texture");
    public static Setting<Boolean> lighting = new Setting<>("Lighting", true).setDescription("Disables vanilla lighting");
    public static Setting<Boolean> blend = new Setting<>("Blend", false).setDescription("Enables blended texture");
    public static Setting<Boolean> transparent = new Setting<>("Transparent", true).setDescription("Makes entity models transparent");
    public static Setting<Boolean> depth = new Setting<>("Depth", true).setDescription("Enables entity depth");
    public static Setting<Boolean> walls = new Setting<>("Walls", true).setDescription("Renders chams models through walls");

    // colors
    public static Setting<Boolean> xqz = new Setting<>("XQZ", true).setDescription("Colors chams models through walls");
    public static Setting<Color> xqzColor = new Setting<>("XQZColor", new Color(0, 70, 250, 50)).setDescription("Color of models through walls").setParent(xqz);

    public static Setting<Boolean> highlight = new Setting<>("Highlight", true).setDescription("Colors chams models when visible");
    public static Setting<Color> highlightColor = new Setting<>("HighlightColor", new Color(250, 0, 250, 50)).setDescription("Color of models when visible").setParent(highlight);

    // texture for enchantment glint
    private final ResourceLocation GLINT_TEXTURE = new ResourceLocation("textures/misc/enchanted_item_glint.png");

    private final SmokeShader smokeShader = new SmokeShader();
    private final CosmosShader cosmosShader = new CosmosShader();
    private final LightsShader lightsShader = new LightsShader();

    @SubscribeEvent
    public void onTick(TickEvent event) {
        getShader().update(shaderSpeed.getValue());
    }

    @SubscribeEvent
    public void onRenderLivingEntity(RenderLivingEntityEvent event) {
        if (hasChams(event.getEntityLivingBase())) {
            // cancel the vanilla rendering
            event.setCanceled(!texture.getValue());

            // make the model transparent
            if (transparent.getValue()) {
                GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
            }

            glPushMatrix();
            glPushAttrib(GL_ALL_ATTRIB_BITS);

            // remove the texture
            if (!texture.getValue() && !mode.getValue().equals(Mode.SHINE)) {
                glDisable(GL_TEXTURE_2D);
            }

            // blend the textures
            if (blend.getValue()) {
                glEnable(GL_BLEND);
            }

            // remove lighting
            if (lighting.getValue()) {
                glDisable(GL_LIGHTING);
            }

            // remove visual depth
            if (depth.getValue()) {
                glDepthMask(false);
            }

            // remove depth
            if (walls.getValue()) {
                glDisable(GL_DEPTH_TEST);
            }

            // update the rendering mode of the polygons
            switch (mode.getValue()) {
                case WIRE:
                    glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
                    break;
                case WIRE_MODEL:
                case MODEL:
                case SHINE:
                    glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
                    break;
                case SHADER:
                    getShader().startShader();
                    break;
            }

            // anti-alias
            glEnable(GL_LINE_SMOOTH);
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
            glLineWidth(width.getValue().floatValue());

            // color the model (walls)
            if (xqz.getValue()) {
                glColor4d(xqzColor.getValue().getRed() / 255F, xqzColor.getValue().getGreen() / 255F, xqzColor.getValue().getBlue() / 255F, xqzColor.getValue().getAlpha() / 255F);
            }

            // render the model
            event.getModelBase().render(event.getEntityLivingBase(), event.getLimbSwing(), event.getLimbSwingAmount(), event.getAgeInTicks(), event.getNetHeadYaw(), event.getHeadPitch(), event.getScaleFactor());

            // re-enable depth
            if (walls.getValue() && !mode.getValue().equals(Mode.WIRE_MODEL)) {
                glEnable(GL_DEPTH_TEST);
            }

            // change to outline polygon mode for wire and model
            if (mode.getValue().equals(Mode.WIRE_MODEL)) {
                glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            }

            // color the model (non-walls)
            if (highlight.getValue()) {
                Color modeColor = mode.getValue().equals(Mode.WIRE_MODEL) ? new Color(xqzColor.getValue().getRed(), xqzColor.getValue().getGreen(), xqzColor.getValue().getBlue(), 255) : highlightColor.getValue();
                glColor4d(modeColor.getRed() / 255F, modeColor.getGreen() / 255F, modeColor.getBlue() / 255F, modeColor.getAlpha() / 255F);
            }

            // render the model
            event.getModelBase().render(event.getEntityLivingBase(), event.getLimbSwing(), event.getLimbSwingAmount(), event.getAgeInTicks(), event.getNetHeadYaw(), event.getHeadPitch(), event.getScaleFactor());

            if (mode.getValue().equals(Mode.SHINE)) {
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);

                for (float i = 0; i < 2; i++) {
                    // bind the enchantment glint texture
                    mc.getRenderManager().renderEngine.bindTexture(GLINT_TEXTURE);

                    // begin the texture matrix
                    GlStateManager.matrixMode(GL_TEXTURE);
                    GlStateManager.loadIdentity();
                    float textureScale = 0.33333334F;

                    // apply scales and rotations to the texture
                    GlStateManager.scale(textureScale, textureScale, textureScale);
                    GlStateManager.rotate(30 - (i * 60), 0, 0, 1);
                    GlStateManager.translate(0, (event.getEntityLivingBase().ticksExisted + mc.getRenderPartialTicks()) * (0.001F + (i * 0.003F)) * 4, 0);
                    GlStateManager.matrixMode(GL_MODELVIEW);
                    glTranslatef(0, 0, 0);

                    // render the model
                    event.getModelBase().render(event.getEntityLivingBase(), event.getLimbSwing(), event.getLimbSwingAmount(), event.getAgeInTicks(), event.getNetHeadYaw(), event.getHeadPitch(), event.getScaleFactor());

                    // load the matrix
                    GlStateManager.matrixMode(5890);
                    GlStateManager.loadIdentity();
                    GlStateManager.matrixMode(5888);
                }

                GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            }

            if (mode.getValue().equals(Mode.SHADER)) {

                // unbind here
                glUseProgram(0);
            }

            // reset depth
            if (walls.getValue() && mode.getValue().equals(Mode.WIRE_MODEL)) {
                glEnable(GL_DEPTH_TEST);
            }

            // reset lighting
            if (lighting.getValue()) {
                glEnable(GL_LIGHTING);
            }

            // reset visual depth
            if (depth.getValue()) {
                glDepthMask(true);
            }

            // reset blend
            if (blend.getValue()) {
                glDisable(GL_BLEND);
            }

            // reset texture
            if (!texture.getValue() && !mode.getValue().equals(Mode.SHINE)) {
                glEnable(GL_TEXTURE_2D);
            }

            glPopAttrib();
            glPopMatrix();
        }
    }

    @SubscribeEvent
    public void onRenderCrystalPre(RenderCrystalEvent.RenderCrystalPreEvent event) {
        // cancel vanilla model rendering
        event.setCanceled(crystals.getValue());
    }

    @SubscribeEvent
    public void onRenderCrystalPost(RenderCrystalEvent.RenderCrystalPostEvent event) {
        if (crystals.getValue()) {
            // make the model transparent
            if (transparent.getValue()) {
                GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
            }

            glPushMatrix();
            glPushAttrib(GL_ALL_ATTRIB_BITS);

            // model rotations
            float rotation = event.getEntityEnderCrystal().innerRotation + event.getPartialTicks();
            float rotationMoved = MathHelper.sin(rotation * 0.2F) / 2 + 0.5F;
            rotationMoved += Math.pow(rotationMoved, 2);

            // scale and translate the model
            glTranslated(event.getX(), event.getY(), event.getZ());
            glScaled(scale.getValue(), scale.getValue(), scale.getValue());

            // remove the texture
            if (!texture.getValue() && !mode.getValue().equals(Mode.SHINE)) {
                glDisable(GL_TEXTURE_2D);
            }

            // blend the textures
            if (blend.getValue()) {
                glEnable(GL_BLEND);
            }

            // remove lighting
            if (lighting.getValue()) {
                glDisable(GL_LIGHTING);
            }

            // remove visual depth
            if (depth.getValue()) {
                glDepthMask(false);
            }

            // remove depth
            if (walls.getValue()) {
                glDisable(GL_DEPTH_TEST);
            }

            // update the rendering mode of the polygons
            switch (mode.getValue()) {
                case WIRE:
                    glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
                    break;
                case WIRE_MODEL:
                case MODEL:
                case SHINE:
                    glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
                    break;
                case SHADER:
                    getShader().startShader();
                    break;
            }

            // anti-alias
            glEnable(GL_LINE_SMOOTH);
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
            glLineWidth(width.getValue().floatValue());

            // color the model (walls)
            if (xqz.getValue()) {
                glColor4d(xqzColor.getValue().getRed() / 255F, xqzColor.getValue().getGreen() / 255F, xqzColor.getValue().getBlue() / 255F, xqzColor.getValue().getAlpha() / 255F);
            }

            // render the model
            if (event.getEntityEnderCrystal().shouldShowBottom()) {
                event.getModelBase().render(event.getEntityEnderCrystal(), 0, rotation * 3, rotationMoved * 0.2F, 0, 0, 0.0625F);
            }

            else {
                event.getModelNoBase().render(event.getEntityEnderCrystal(), 0, rotation * 3, rotationMoved * 0.2F, 0, 0, 0.0625F);
            }

            // re-enable depth
            if (walls.getValue() && !mode.getValue().equals(Mode.WIRE_MODEL)) {
                glEnable(GL_DEPTH_TEST);
            }

            // change to outline polygon mode for wire and model
            if (mode.getValue().equals(Mode.WIRE_MODEL)) {
                glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            }

            if (highlight.getValue()) {
                Color modeColor = mode.getValue().equals(Mode.WIRE_MODEL) ? new Color(xqzColor.getValue().getRed(), xqzColor.getValue().getGreen(), xqzColor.getValue().getBlue(), 255) : highlightColor.getValue();
                glColor4d(modeColor.getRed() / 255F, modeColor.getGreen() / 255F, modeColor.getBlue() / 255F, modeColor.getAlpha() / 255F);
            }
            if (event.getEntityEnderCrystal().shouldShowBottom()) {
                event.getModelBase().render(event.getEntityEnderCrystal(), 0, rotation * 3, rotationMoved * 0.2F, 0, 0, 0.0625F);
            }

            else {
                event.getModelNoBase().render(event.getEntityEnderCrystal(), 0, rotation * 3, rotationMoved * 0.2F, 0, 0, 0.0625F);
            }

            if (mode.getValue().equals(Mode.SHINE)) {
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);

                for (int i = 0; i < 2; ++i) {
                    // bind the enchantment glint texture
                    mc.getRenderManager().renderEngine.bindTexture(GLINT_TEXTURE);

                    // begin the texture matrix
                    GlStateManager.matrixMode(GL_TEXTURE);
                    GlStateManager.loadIdentity();
                    float textureScale = 0.33333334F;
                    GlStateManager.scale(textureScale, textureScale, textureScale);
                    GlStateManager.rotate(30 - (i * 60), 0, 0, 1);
                    GlStateManager.translate(0, (event.getEntityEnderCrystal().ticksExisted + mc.getRenderPartialTicks()) * (0.001F + (i * 0.003F)) * 4, 0);
                    GlStateManager.matrixMode(GL_MODELVIEW);
                    glTranslatef(0, 0, 0);

                    // render the model
                    if (event.getEntityEnderCrystal().shouldShowBottom()) {
                        event.getModelBase().render(event.getEntityEnderCrystal(), 0, rotation * 3, rotationMoved * 0.2F, 0, 0, 0.0625F);
                    }

                    else {
                        event.getModelNoBase().render(event.getEntityEnderCrystal(), 0, rotation * 3, rotationMoved * 0.2F, 0, 0, 0.0625F);
                    }

                    // load the matrix
                    GlStateManager.matrixMode(5890);
                    GlStateManager.loadIdentity();
                    GlStateManager.matrixMode(5888);
                }

                GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            }

            if (mode.getValue().equals(Mode.SHADER)) {
                // unbind here
                glUseProgram(0);
            }

            // change to outline polygon mode for wire and model
            if (walls.getValue() && mode.getValue().equals(Mode.WIRE_MODEL)) {
                glEnable(GL_DEPTH_TEST);
            }

            // reset lighting
            if (lighting.getValue()) {
                glEnable(GL_LIGHTING);
            }

            // reset visual depth
            if (depth.getValue()) {
                glDepthMask(true);
            }

            // reset blend
            if (blend.getValue()) {
                glDisable(GL_BLEND);
            }

            // reset texture
            if (!texture.getValue() && !mode.getValue().equals(Mode.SHINE)) {
                glEnable(GL_TEXTURE_2D);
            }

            // reset scale
            glScaled(1 / scale.getValue(), 1 / scale.getValue(), 1 / scale.getValue());

            glPopAttrib();
            glPopMatrix();
        }
    }

    /**
     * Checks whether or not a given entity has chams applied to it
     * @param entity The given entity
     * @return Whether or not the given entity has chams applied to it
     */
    public boolean hasChams(EntityLivingBase entity) {
        return entity instanceof EntityOtherPlayerMP && players.getValue() || (entity instanceof EntityPlayerSP && local.getValue()) || (EntityUtil.isPassiveMob(entity) || EntityUtil.isNeutralMob(entity)) && mobs.getValue() || EntityUtil.isHostileMob(entity) && monsters.getValue();
    }

    public enum ChamsShader {
        SMOKE,
        COSMOS,
        LIGHTS
    }

    public Shader getShader() {
        switch (shaderMode.getValue()){
            case COSMOS:
                return cosmosShader;
            case SMOKE:
                return smokeShader;
            case LIGHTS:
                return lightsShader;
        }
        return null;
    }

    public enum Mode {

        /**
         * Fills in the model
         */
        MODEL,

        /**
         * Outlines the model
         */
        WIRE,

        /**
         * Fills and outlines the model
         */
        WIRE_MODEL,

        /**
         * Adds the enchantment glint to the model
         */
        SHINE,

        /**
         * Renders a shader on the entity
         */
        SHADER
    }
}