package cope.cosmos.client.features.modules.visual;

import cope.cosmos.client.events.RenderCrystalEvent;
import cope.cosmos.client.events.RenderLivingEntityEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.utility.client.ColorUtil;
import cope.cosmos.utility.world.EntityUtil;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

@SuppressWarnings("unused")
public class Chams extends Module {
    public static Chams INSTANCE;

    public Chams() {
        super("Chams", Category.VISUAL, "Renders entity models through walls");
        INSTANCE = this;
    }

    public static Setting<Mode> mode = new Setting<>("Mode", "Mode for Chams", Mode.MODEL);
    public static Setting<Double> width = new Setting<>(() -> mode.getValue().equals(Mode.WIRE) || mode.getValue().equals(Mode.WIREMODEL),"Width", "Line width for the model", 0.0, 3.0, 5.0, 2).setParent(mode);

    public static Setting<Boolean> players = new Setting<>("Players", "Renders chams on players", true);
    public static Setting<Boolean> local = new Setting<>("Local", "Renders chams on the local player", false).setParent(players);

    public static Setting<Boolean> mobs = new Setting<>("Mobs", "Renders chams on mobs", true);
    public static Setting<Boolean> monsters = new Setting<>("Monsters", "Renders chams on monsters", true);

    public static Setting<Boolean> crystals = new Setting<>("Crystals", "Renders chams on crystals", true);
    public static Setting<Double> scale = new Setting<>("Scale", "Scale for crystal model", 0.0, 1.0, 2.0, 2).setParent(crystals);

    public static Setting<Boolean> texture = new Setting<>("Texture", "Enables entity texture", false);
    public static Setting<Boolean> lighting = new Setting<>("Lighting", "Disables vanilla lighting", true);
    public static Setting<Boolean> blend = new Setting<>("Blend", "Enables blended texture", false);
    public static Setting<Boolean> transparent = new Setting<>("Transparent", "Makes entity models transparent", true);
    public static Setting<Boolean> depth = new Setting<>("Depth", "Enables entity depth", true);
    public static Setting<Boolean> walls = new Setting<>("Walls", "Renders chams models through walls", true);

    public static Setting<Boolean> xqz = new Setting<>("XQZ", "Colors chams models through walls", true);
    public static Setting<Color> xqzColor = new Setting<>("XQZColor", "Color of models through walls", new Color(0, 70, 250, 50)).setParent(xqz);

    public static Setting<Boolean> highlight = new Setting<>("Highlight", "Colors chams models when visible", true);
    public static Setting<Color> highlightColor = new Setting<>("HighlightColor", "Color of models when visible", new Color(250, 0, 250, 50)).setParent(highlight);

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
                case WIREMODEL:
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

            if (walls.getValue() && !mode.getValue().equals(Mode.WIREMODEL))
                glEnable(GL_DEPTH_TEST);

            if (mode.getValue().equals(Mode.WIREMODEL))
                glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

            if (highlight.getValue())
                ColorUtil.setColor(mode.getValue().equals(Mode.WIREMODEL) ? new Color(xqzColor.getValue().getRed(), xqzColor.getValue().getGreen(), xqzColor.getValue().getBlue(), 255) : highlightColor.getValue());

            event.getModelBase().render(event.getEntityLivingBase(), event.getLimbSwing(), event.getLimbSwingAmount(), event.getAgeInTicks(), event.getNetHeadYaw(), event.getHeadPitch(), event.getScaleFactor());

            if (walls.getValue() && mode.getValue().equals(Mode.WIREMODEL))
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
        }
    }

    @SubscribeEvent
    public void onRenderCrystalPre(RenderCrystalEvent.RenderCrystalPreEvent event) {
        event.setCanceled(nullCheck() && INSTANCE.isEnabled() && crystals.getValue());
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
                case WIREMODEL:
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

            if (walls.getValue() && !mode.getValue().equals(Mode.WIREMODEL))
                glEnable(GL_DEPTH_TEST);

            if (mode.getValue().equals(Mode.WIREMODEL))
                glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

            if (highlight.getValue())
                ColorUtil.setColor(mode.getValue().equals(Mode.WIREMODEL) ? new Color(xqzColor.getValue().getRed(), xqzColor.getValue().getGreen(), xqzColor.getValue().getBlue(), 255) : highlightColor.getValue());

            if (event.getEntityEnderCrystal().shouldShowBottom())
                event.getModelBase().render(event.getEntityEnderCrystal(), 0, rotation * 3, rotationMoved * 0.2F, 0, 0, 0.0625F);
            else
                event.getModelNoBase().render(event.getEntityEnderCrystal(), 0, rotation * 3, rotationMoved * 0.2F, 0, 0, 0.0625F);

            if (walls.getValue() && mode.getValue().equals(Mode.WIREMODEL))
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
        }
    }

    public enum Mode {
        MODEL, WIRE, WIREMODEL, SHINE
    }
}
