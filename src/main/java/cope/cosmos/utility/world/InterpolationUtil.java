package cope.cosmos.utility.world;

import cope.cosmos.utility.IUtility;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class InterpolationUtil implements IUtility {

    public static Vec3d getInterpolatedPos(Entity entity, float ticks) {
        return new Vec3d(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ).add(getInterpolatedAmount(entity, ticks));
    }

    public static Vec3d getInterpolatedAmount(Entity entity, double ticks) {
        return getInterpolatedAmount(entity, ticks, ticks, ticks);
    }

    public static Vec3d getInterpolatedAmount(Entity entity, double x, double y, double z) {
        return new Vec3d((entity.posX - entity.lastTickPosX) * x, (entity.posY - entity.lastTickPosY) * y, (entity.posZ - entity.lastTickPosZ) * z);
    }

    public static Vec3d interpolateEntityTime(Entity entity, float time) {
        return new Vec3d(entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) time, entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) time, entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) time);
    }

    public static boolean isNearlyEqual(double value, double value2, double threshold) {
        double boundValuePositive = value + threshold;
        double boundValueNegative = value - threshold;
        return !(value2 > boundValuePositive) && !(value2 < boundValueNegative);
    }
}
