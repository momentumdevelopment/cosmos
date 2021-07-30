package cope.cosmos.util.world;

import cope.cosmos.util.Wrapper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AngleUtil implements Wrapper {

    public static float[] calculateAngles(Entity entity) {
        return calculateAngle(InterpolationUtil.interpolateEntityTime(mc.player, mc.getRenderPartialTicks()), InterpolationUtil.interpolateEntityTime(entity, mc.getRenderPartialTicks()));
    }

    public static float[] calculateAngles(BlockPos blockPos) {
        return calculateAngle(InterpolationUtil.interpolateEntityTime(mc.player, mc.getRenderPartialTicks()), new Vec3d(blockPos));
    }

    public static float[] calculateCenter(Entity entity) {
        return calculateAngle(InterpolationUtil.interpolateEntityTime(mc.player, mc.getRenderPartialTicks()), InterpolationUtil.interpolateEntityTime(entity, mc.getRenderPartialTicks()).add(new Vec3d(entity.width / 2, entity.getEyeHeight() / 2, entity.width / 2)));
    }

    public static float[] calculateCenter(BlockPos blockPos) {
        return calculateAngle(InterpolationUtil.interpolateEntityTime(mc.player, mc.getRenderPartialTicks()), new Vec3d(blockPos).add(new Vec3d(0.5, 0, 0.5)));
    }

    public static float[] calculateAngle(Vec3d from, Vec3d to) {
        return new float[] {
                (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(to.z - from.z, to.x - from.x)) - 90.0), (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2((to.y - from.y) * -1.0, MathHelper.sqrt(Math.pow(to.x - from.x, 2) + Math.pow(to.z - from.z, 2)))))
        };
    }

    public static float calculateAngleDifference(float serverValue, float currentValue, double divisions, int steps) {
        return (float) (serverValue - currentValue / (divisions * steps));
    }

    public static float calculateAngleDifference(float direction, float rotationYaw) {
        float phi = Math.abs(rotationYaw - direction) % 360.0f;
        return (phi > 180.0f) ? (360.0f - phi) : phi;
    }
}
