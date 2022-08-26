package cope.cosmos.client.features.modules.visual;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.entity.EntityUtil;
import cope.cosmos.util.entity.InterpolationUtil;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.string.ColorUtil;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

/**
 * @author Surge
 * @since 04/02/22
 */
public class TracersModule extends Module {
    public static TracersModule INSTANCE;

    public TracersModule() {
        super("Tracers", Category.VISUAL, "Draws lines to entities in the world");
        INSTANCE = this;
    }

    // **************************** entity ****************************

    public static Setting<Boolean> players = new Setting<>("Players", true)
            .setDescription("Draw lines to players");

    public static Setting<Boolean> passive = new Setting<>("Passive", false)
            .setDescription("Draw lines to passive entities");

    public static Setting<Boolean> neutrals = new Setting<>("Neutrals", false)
            .setDescription("Draw lines to neutral entities");

    public static Setting<Boolean> mobs = new Setting<>("Mobs", false)
            .setDescription("Draw lines to monsters");

    // **************************** render ****************************

    public static Setting<Float> width = new Setting<>("Width", 0.1F, 1F, 1.5F, 1)
            .setAlias("LineWidth")
            .setDescription("How thick to render the lines");

    public static Setting<To> to = new Setting<>("To", To.BODY)
            .setDescription("Where to draw the line to");


    @Override
    public void onRender3D() {
        // Draw lines to entities if they are valid
        mc.world.loadedEntityList.forEach(entity -> {

            if (isEntityValid(entity)) {

                // Interpolated position of the entity
                Vec3d interpolatedPosition = InterpolationUtil.getInterpolatedPosition(entity, mc.getRenderPartialTicks());

                // Adds the extra height onto the
                float addedHeight = 0;
                switch (to.getValue()) {
                    case BODY:
                        // Add half of the entity's height to the Y value
                        addedHeight = entity.height / 2F;
                        break;
                    case HEAD:
                        // Add the entity's height to the Y value
                        addedHeight = entity.height;
                }

                // Corrected position of the entity
                Vec3d entityVector = new Vec3d(interpolatedPosition.x - mc.getRenderManager().viewerPosX, interpolatedPosition.y - mc.getRenderManager().viewerPosY + addedHeight, interpolatedPosition.z - mc.getRenderManager().viewerPosZ);

                // Draw tracer
                drawTracer(entityVector, width.getValue(), ColorUtil.getPrimaryColor());
            }
        });
    }

    /**
     * Draws a tracer to the specified vector
     * @param vecTo The vector to draw to
     * @param lineWidth The line width of the tracer
     * @param lineColor The line color of the tracer
     */
    public static void drawTracer(Vec3d vecTo, float lineWidth, Color lineColor) {

        // position to draw to
        Vec3d eyes = new Vec3d(0, 0, 1)
                .rotatePitch(-((float) Math.toRadians(mc.player.rotationPitch))) // Rotate pitch
                .rotateYaw(-((float) Math.toRadians(mc.player.rotationYaw))) // Rotate yaw
                .addVector(0, mc.player.getEyeHeight(), 0); // Add player's eye height

        // draw line
        RenderUtil.drawLine3D(eyes, vecTo, lineColor, lineWidth);
    }

    /**
     * Checks if an entity is valid
     * @param entity The entity to check
     * @return If the entity is valid
     */
    public boolean isEntityValid(Entity entity) {
        return entity instanceof EntityOtherPlayerMP && players.getValue() || EntityUtil.isPassiveMob(entity) && passive.getValue() || EntityUtil.isNeutralMob(entity) && neutrals.getValue() || EntityUtil.isHostileMob(entity) && mobs.getValue();
    }

    public enum To {

        /**
         * Draw the line towards the entity's feet
         */
        FEET,

        /**
         * Draw the line to the middle of the entity's body
         */
        BODY,

        /**
         * Draw the line to the top of the entity's body
         */
        HEAD
    }
}