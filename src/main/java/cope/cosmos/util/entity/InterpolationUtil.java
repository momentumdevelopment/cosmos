package cope.cosmos.util.entity;

import cope.cosmos.util.Wrapper;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

/**
 * @author linustouchtips
 * @since 05/08/2021
 */
public class InterpolationUtil implements Wrapper {

    /**
     * Gets the interpolated position of an entity (i.e. position based on render ticks)
     * @param entity The entity to get the position for
     * @param ticks The render ticks
     * @return The interpolated vector of an entity
     */
    public static Vec3d getInterpolatedPosition(Entity entity, float ticks) {
        return new Vec3d(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ)
                .add(new Vec3d(entity.posX - entity.lastTickPosX, entity.posY - entity.lastTickPosY, entity.posZ - entity.lastTickPosZ)
                        .scale(ticks)
                );
    }
}
