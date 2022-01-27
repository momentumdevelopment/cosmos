package cope.cosmos.util.world;

import cope.cosmos.util.Wrapper;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * @author linustouchtips
 * @since 05/07/2021
 */
public class RaytraceUtil implements Wrapper {

    /**
     * Finds the visibility to a block
     * @param position The block to check
     * @param offset The NCP range bypass offset
     * @return The visibility to the block
     */
    public static boolean isNotVisible(BlockPos position, double offset) {
        if (offset > 50 || offset < -50) {
            return false;
        }

        return mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(position.getX() + 0.5, position.getY() + offset, position.getZ() + 0.5), false, true, false) != null;
    }

    /**
     * Finds the visibility to an entity
     * @param entity The entity to check
     * @param offset The NCP range bypass offset
     * @return The visibility to the entity
     */
    public static boolean isNotVisible(Entity entity, double offset) {
        if (offset > 50 || offset < -50) {
            return false;
        }

        return mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(entity.posX, entity.posY + offset, entity.posZ), false, true, false) != null;
    }
}
