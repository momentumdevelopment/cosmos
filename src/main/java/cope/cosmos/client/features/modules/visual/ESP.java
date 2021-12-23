package cope.cosmos.client.features.modules.visual;

import cope.cosmos.asm.mixins.accessor.IEntityRenderer;
import cope.cosmos.asm.mixins.accessor.IRenderGlobal;
import cope.cosmos.asm.mixins.accessor.IShaderGroup;
import cope.cosmos.client.events.SettingUpdateEvent;
import cope.cosmos.client.events.ShaderColorEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.client.Colors;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.shader.shaders.OutlineShader;
import cope.cosmos.client.shader.shaders.RainbowOutlineShader;
import cope.cosmos.util.client.ColorUtil;
import cope.cosmos.util.world.EntityUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * @author linustouchtips
 * @since 07/21/2021
 */
@SuppressWarnings("unused")
public class ESP extends Module {
    public static ESP INSTANCE;

    public ESP() {
        super("ESP", Category.VISUAL, "Allows you to see players through walls");
        INSTANCE = this;
    }

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.SHADER).setDescription("The mode for the render style");
    public static Setting<Double> width = new Setting<>("Width", 0.0, 1.25, 5.0, 1).setDescription( "Line width for the visual").setVisible(() -> mode.getValue().equals(Mode.GLOW));

    public static Setting<Boolean> players = new Setting<>("Players", true).setDescription("Highlight players");
    public static Setting<Boolean> passives = new Setting<>("Passives", true).setDescription("Highlight passives");
    public static Setting<Boolean> neutrals = new Setting<>("Neutrals", true).setDescription("Highlight neutrals");
    public static Setting<Boolean> hostiles = new Setting<>("Hostiles", true).setDescription("Highlight hostiles");
    public static Setting<Boolean> items = new Setting<>("Items", true).setDescription("Highlight items");
    public static Setting<Boolean> crystals = new Setting<>("Crystals", true).setDescription("Highlight crystals");
    public static Setting<Boolean> vehicles = new Setting<>("Vehicles", true).setDescription("Highlight vehicles");

    // framebuffer
    private Framebuffer framebuffer;

    // shaders
    private final OutlineShader outlineShader = new OutlineShader();
    private final RainbowOutlineShader rainbowOutlineShader = new RainbowOutlineShader();

    @Override
    public void onUpdate() {
        if (mode.getValue().equals(Mode.GLOW)) {
            // set all entities in the world glowing
            mc.world.loadedEntityList.forEach(entity -> {
                if (!entity.equals(mc.player) && hasHighlight(entity)) {
                    entity.setGlowing(true);
                }
            });

            // get the shaders
            ShaderGroup outlineShaderGroup = ((IRenderGlobal) mc.renderGlobal).getEntityOutlineShader();
            List<Shader> shaders = ((IShaderGroup) outlineShaderGroup).getListShaders();

            // update the shader radius
            shaders.forEach(shader -> {
                ShaderUniform outlineRadius = shader.getShaderManager().getShaderUniform("Radius");

                if (outlineRadius != null) {
                    outlineRadius.set(width.getValue().floatValue());
                }
            });
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // remove glow effect from all entities
        if (mode.getValue().equals(Mode.GLOW)) {
            mc.world.loadedEntityList.forEach(entity -> {
                if (entity.isGlowing()) {
                    entity.setGlowing(false);
                }
            });
        }
    }

    @SubscribeEvent
    public void onSettingUpdate(SettingUpdateEvent event) {
        if (event.getSetting().equals(mode) && !event.getSetting().getValue().equals(Mode.GLOW)) {
            // remove glow effect from all entities
            mc.world.loadedEntityList.forEach(entity -> {
                if (entity.isGlowing()) {
                    entity.setGlowing(false);
                }
            });
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.getType().equals(RenderGameOverlayEvent.ElementType.HOTBAR)) {
            if (mode.getValue().equals(Mode.SHADER)) {
                GlStateManager.enableAlpha();
                GlStateManager.pushMatrix();
                GlStateManager.pushAttrib();

                // delete our old framebuffer, we'll create a new one
                if (framebuffer != null) {
                    framebuffer.deleteFramebuffer();
                }

                // create a new framebuffer
                framebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);

                // bind our new framebuffer (i.e. set it as the current active buffer)
                framebuffer.bindFramebuffer(true);

                // prevent entity shadows from rendering
                boolean previousShadows = mc.gameSettings.entityShadows;
                mc.gameSettings.entityShadows = false;

                // https://hackforums.net/showthread.php?tid=4811280
                ((IEntityRenderer) mc.entityRenderer).setupCamera(event.getPartialTicks(), 0);

                // draw all entities
                mc.world.loadedEntityList.forEach(entity -> {
                    if (entity != mc.player && hasHighlight(entity)) {
                        mc.getRenderManager().renderEntityStatic(entity, event.getPartialTicks(), true);
                    }
                });

                // reset shadows
                mc.gameSettings.entityShadows = previousShadows;

                glEnable(GL_BLEND);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

                // rebind the mc framebuffer
                mc.getFramebuffer().bindFramebuffer(true);

                // remove lighting
                mc.entityRenderer.disableLightmap();
                RenderHelper.disableStandardItemLighting();

                glPushMatrix();

                // draw the shader
                if (!Colors.rainbow.getValue().equals(Colors.Rainbow.NONE)) {
                    rainbowOutlineShader.startShader();
                }

                else {
                    outlineShader.startShader();
                }

                // prepare overlay render
                mc.entityRenderer.setupOverlayRendering();

                ScaledResolution scaledResolution = new ScaledResolution(mc);

                // draw the framebuffer
                glBindTexture(GL_TEXTURE_2D, framebuffer.framebufferTexture);
                glBegin(GL_QUADS);
                glTexCoord2d(0, 1);
                glVertex2d(0, 0);
                glTexCoord2d(0, 0);
                glVertex2d(0, scaledResolution.getScaledHeight());
                glTexCoord2d(1, 0);
                glVertex2d(scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight());
                glTexCoord2d(1, 1);
                glVertex2d(scaledResolution.getScaledWidth(), 0);
                glEnd();
                glUseProgram(0);

                // stop drawing our shader
                glUseProgram(0);
                glPopMatrix();

                // reset lighting
                mc.entityRenderer.disableLightmap();

                GlStateManager.popMatrix();
                GlStateManager.popAttrib();
            }
        }
    }

    @SubscribeEvent
    public void onShaderColor(ShaderColorEvent event) {
        if (mode.getValue().equals(Mode.GLOW)) {
            // change the shader color
            if (hasHighlight(event.getEntity())) {

                event.setColor(ColorUtil.getPrimaryColor());

                // remove vanilla team color
                event.setCanceled(true);
            }
        }
    }

    /**
     * Checks if the {@link Entity} entity has an ESP highlight
     * @param entity The entity to check
     * @return Whether the entity has an ESP highlight
     */
    public boolean hasHighlight(Entity entity) {
        return players.getValue() && entity instanceof EntityPlayer || passives.getValue() && EntityUtil.isPassiveMob(entity) || neutrals.getValue() && EntityUtil.isNeutralMob(entity) || hostiles.getValue() && EntityUtil.isHostileMob(entity) || vehicles.getValue() && EntityUtil.isVehicleMob(entity) || items.getValue() && (entity instanceof EntityItem || entity instanceof EntityExpBottle || entity instanceof EntityXPOrb) || crystals.getValue() && entity instanceof EntityEnderCrystal;
    }

    public enum Mode {

        /**
         * Draws the Minecraft Glow shader
         */
        GLOW,

        /**
         * Draws a 2D shader on the GPU over the entity
         */
        SHADER,
    }
}
