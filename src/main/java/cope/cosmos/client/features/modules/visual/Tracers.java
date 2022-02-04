package cope.cosmos.client.features.modules.visual;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.entity.InterpolationUtil;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.string.ColorUtil;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.math.Vec3d;

/**
 * @author Wolfsurge
 * @since 04/02/22
 */
public class Tracers extends Module {

    // Filters
    public static Setting<Boolean> players = new Setting<>("Players", true).setDescription("Draws lines to players");
    public static Setting<Boolean> passive = new Setting<>("Passive", false).setDescription("Draws lines to passive entities");
    public static Setting<Boolean> mobs = new Setting<>("Mobs", false).setDescription("Draws lines to monsters");

    // Other
    public static Setting<Float> lineWidth = new Setting<>("LineWidth", .1f, .5f, 1.5f, 1).setDescription("How thick to render the lines");
    public static Setting<To> to = new Setting<>("To", To.BODY).setDescription("Where to draw the line to");

    public Tracers() {
        super("Tracers", Category.VISUAL, "Draws lines to entities in the world");
    }

    @Override
    public void onRender3D() {
        // Draw lines to entities if they are valid
        for(Entity entity : mc.world.loadedEntityList) {
            if(isEntityValid(entity)) {
                // Interpolated position of the entity
                Vec3d interpolatedPosition = InterpolationUtil.getInterpolatedPosition(entity, mc.getRenderPartialTicks());

                // Corrected position of the entity
                Vec3d entityVec = new Vec3d(interpolatedPosition.x - mc.getRenderManager().viewerPosX, interpolatedPosition.y - mc.getRenderManager().viewerPosY + getHeight(entity), interpolatedPosition.z - mc.getRenderManager().viewerPosZ);

                // Draw tracer
                RenderUtil.drawTracer(entityVec, lineWidth.getValue(), ColorUtil.getPrimaryColor());
            }
        }
    }

    /**
     * Checks if an entity is valid
     * @param entity The entity to check
     * @return If the entity is valid
     */
    public boolean isEntityValid(Entity entity) {
        return entity instanceof EntityOtherPlayerMP && players.getValue() || entity instanceof EntityLiving && !(entity instanceof EntityMob) && passive.getValue() || entity instanceof EntityMob && mobs.getValue();
    }

    /**
     * Gets where to draw the line to
     * @param entityIn The entity
     * @return The place to draw the line to
     */
    public float getHeight(Entity entityIn) {
        switch (to.getValue()) {
            case BODY:
                return entityIn.height / 2f;
            case HEAD:
                return entityIn.height;
            default:
                return 0;
        }
    }

    /**
     * Place to draw the line to
     */
    enum To {
        // Draw line to the entity's feet
        FEET,
        // Draw line to the middle of the entity's body
        BODY,
        // Draw line to the entity's head
        HEAD;
    }
}