package cope.cosmos.client.features.modules.visual;

import cope.cosmos.asm.mixins.accessor.IRenderGlobal;
import cope.cosmos.asm.mixins.accessor.IShaderGroup;
import cope.cosmos.client.events.ShaderColorEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.world.EntityUtil;
import cope.cosmos.util.world.InterpolationUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
public class ESP extends Module {
    public static ESP INSTANCE;

    public ESP() {
        super("ESP", Category.VISUAL, "Allows you to see players through walls");
        INSTANCE = this;
    }

    public static Setting<Mode> mode = new Setting<>("Mode", "The mode for the render style", Mode.SHADER);
    public static Setting<Double> width = new Setting<>(() -> mode.getValue().equals(Mode.SHADER), "Width", "Line width for the visual", 0.0, 1.25, 5.0, 1);

    public static Setting<Boolean> players = new Setting<>("Players", "Highlight players", true);
    public static Setting<Color> playersColor = new Setting<>("Color", "Color to highlight players", new Color(151, 0, 206, 255)).setParent(players);

    public static Setting<Boolean> passives = new Setting<>("Passives", "Highlight passives", true);
    public static Setting<Color> passivesColor = new Setting<>("Color", "Color to highlight passives", new Color(151, 0, 206, 255)).setParent(passives);

    public static Setting<Boolean> neutrals = new Setting<>("Neutrals", "Highlight neutrals", true);
    public static Setting<Color> neutralsColor = new Setting<>("Color", "Color to highlight neutrals", new Color(151, 0, 206, 255)).setParent(neutrals);

    public static Setting<Boolean> hostiles = new Setting<>("Hostiles", "Highlight hostiles", true);
    public static Setting<Color> hostilesColor = new Setting<>("Color", "Color to highlight hostiles", new Color(151, 0, 206, 255)).setParent(hostiles);

    public static Setting<Boolean> items = new Setting<>("Items", "Highlight items", true);
    public static Setting<Color> itemsColor = new Setting<>("Color", "Color to highlight items", new Color(151, 0, 206, 255)).setParent(items);

    public static Setting<Boolean> crystals = new Setting<>("Crystals", "Highlight crystals", true);
    public static Setting<Color> crystalsColor = new Setting<>("Color", "Color to highlight crystals", new Color(151, 0, 206, 255)).setParent(crystals);

    public static Setting<Boolean> vehicles = new Setting<>("Vehicles", "Highlight vehicles", true);
    public static Setting<Color> vehiclesColor = new Setting<>("Color", "Color to highlight vehicles", new Color(151, 0, 206, 255)).setParent(vehicles);

    @Override
    public void onUpdate() {
        if (mode.getValue().equals(Mode.SHADER)) {
            mc.world.loadedEntityList.forEach(entity -> {
                if (!entity.equals(mc.player) && hasHighlight(entity)) {
                    entity.setGlowing(true);
                }
            });

            ShaderGroup outlineShaderGroup = ((IRenderGlobal) mc.renderGlobal).getEntityOutlineShader();
            List<Shader> shaders = ((IShaderGroup) outlineShaderGroup).getListShaders();

            shaders.forEach(shader -> {
                ShaderUniform outlineRadius = shader.getShaderManager().getShaderUniform("Radius");

                if (outlineRadius != null)
                    outlineRadius.set(width.getValue().floatValue());
            });
        } else if (mode.getValue().equals(Mode.CSGO)) {
            GlStateManager.disableDepth();
            float viewerYaw = mc.getRenderManager().playerViewY;
            mc.world.loadedEntityList.stream()
                    .filter(Objects::nonNull)
                    .filter(entity -> mc.player != entity)
                    .filter(ESP::hasHighlight)
                    .forEach(entity -> {
                        GL11.glPushMatrix();
                        Vec3d interp = InterpolationUtil.getInterpolatedPos(entity, mc.getRenderPartialTicks())
                                .subtract(InterpolationUtil.getInterpolatedPos(mc.getRenderViewEntity(), mc.getRenderPartialTicks()))
                                .add(entity.getEntityBoundingBox().getCenter().subtract(entity.getPositionVector()));

                        GlStateManager.translate(interp.x, interp.y, interp.z);
                        GlStateManager.rotate(-viewerYaw, 0.0f, 1.0f, 0.0f);
                        GL11.glDisable(GL11.GL_TEXTURE_2D);

                        GL11.glEnable(GL11.GL_LINE_SMOOTH);
                        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
                        double w = entity.getEntityBoundingBox().maxX - entity.getEntityBoundingBox().minX + 0.2;
                        double h = entity.getEntityBoundingBox().maxY - entity.getEntityBoundingBox().minY + 0.2;
                        GlStateManager.glLineWidth(width.getValue().floatValue());
                        GL11.glBegin(GL11.GL_LINES);
                        GL11.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);

                        GL11.glVertex3d(-w / 2.0, h / 2.0, 0.0);
                        GL11.glVertex3d(w / 2.0, h / 2.0, 0.0);

                        GL11.glVertex3d(w / 2.0, h / 2.0, 0.0);
                        GL11.glVertex3d(w / 2.0, -h / 2.0, 0.0);

                        GL11.glVertex3d(w / 2.0, -h / 2.0, 0.0);
                        GL11.glVertex3d(-w / 2.0, -h / 2.0, 0.0);

                        GL11.glVertex3d(-w / 2.0, -h / 2.0, 0.0);
                        GL11.glVertex3d(-w / 2.0, h / 2.0, 0.0);

                        GL11.glEnd();

                        GlStateManager.glLineWidth(width.getValue().floatValue() / 2.0f);

                        GL11.glBegin(GL11.GL_LINES);
                        if (mc.player.canEntityBeSeen(entity)) {
                            GL11.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
                        } else {
                            GL11.glColor4f(1.0f, 1.0f, 0.0f, 1.0f);
                        }
                        GL11.glVertex3d(-w / 2.0, h / 2.0, 0.0);
                        GL11.glVertex3d(w / 2.0, h / 2.0, 0.0);

                        GL11.glVertex3d(w / 2.0, h / 2.0, 0.0);
                        GL11.glVertex3d(w / 2.0, -h / 2.0, 0.0);

                        GL11.glVertex3d(w / 2.0, -h / 2.0, 0.0);
                        GL11.glVertex3d(-w / 2.0, -h / 2.0, 0.0);

                        GL11.glVertex3d(-w / 2.0, -h / 2.0, 0.0);
                        GL11.glVertex3d(-w / 2.0, h / 2.0, 0.0);

                        GL11.glEnd();

                        // Health bar.

                        if (entity instanceof EntityLivingBase) {
                            float healthPercentage = ((EntityLivingBase) entity).getHealth() / ((EntityLivingBase) entity).getMaxHealth();
                            float healthBarHeight = (float) h * healthPercentage;
                            double xOffset = -0.1;
                            GlStateManager.glLineWidth(width.getValue().floatValue());
                            GL11.glBegin(GL11.GL_LINES);
                            GL11.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
                            GL11.glVertex3d(w / 2.0 - xOffset, h / 2.0, 0.0);
                            GL11.glVertex3d(w / 2.0 - xOffset, -h / 2.0, 0.0);
                            GL11.glEnd();

                            GlStateManager.glLineWidth(width.getValue().floatValue() / 2.0f);

                            GL11.glBegin(GL11.GL_LINES);
                            GL11.glColor4f(0.0f, 1.0f, 0.0f, 1.0f);
                            GL11.glVertex3d(w / 2.0 - xOffset, -h / 2.0, 0.0);
                            GL11.glVertex3d(w / 2.0 - xOffset, -h / 2.0 + healthBarHeight, 0.0);
                            GL11.glEnd();
                        }
                        GL11.glDisable(GL11.GL_LINE_SMOOTH);

                        GL11.glPopMatrix();

                    });
            GlStateManager.enableDepth();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (mode.getValue().equals(Mode.SHADER)) {
            mc.world.loadedEntityList.forEach(entity -> {
                if (entity.isGlowing()) {
                    entity.setGlowing(false);
                }
            });
        }
    }

    @SubscribeEvent
    public void onShaderColor(ShaderColorEvent event) {
        if (mode.getValue().equals(Mode.SHADER)) {
            event.setColor(getColorByEntity(event.getEntity()));
            event.setCanceled(hasHighlight(event.getEntity()));
        }
    }

    public static boolean hasHighlight(Entity entity) {
        return players.getValue() && entity instanceof EntityPlayer || passives.getValue() && EntityUtil.isPassiveMob(entity) || neutrals.getValue() && EntityUtil.isNeutralMob(entity) || hostiles.getValue() && EntityUtil.isHostileMob(entity) || vehicles.getValue() && EntityUtil.isVehicleMob(entity) || items.getValue() && entity instanceof EntityItem || crystals.getValue() && entity instanceof EntityEnderCrystal;
    }

    public Color getColorByEntity(Entity entity) {
        if (entity instanceof EntityPlayer)
            return playersColor.getValue();

        else if (EntityUtil.isPassiveMob(entity))
            return passivesColor.getValue();

        else if (EntityUtil.isNeutralMob(entity))
            return neutralsColor.getValue();

        else if (EntityUtil.isHostileMob(entity))
            return hostilesColor.getValue();

        else if (EntityUtil.isVehicleMob(entity))
            return vehiclesColor.getValue();

        else if (entity instanceof EntityItem)
            return itemsColor.getValue();

        else if (entity instanceof EntityEnderCrystal)
            return crystalsColor.getValue();

        return Color.WHITE;
    }

    public enum Mode {
        SHADER,
        CSGO
    }
}
