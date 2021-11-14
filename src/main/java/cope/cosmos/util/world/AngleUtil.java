package cope.cosmos.util.world;

import cope.cosmos.util.Wrapper;
import cope.cosmos.util.player.Rotation;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AngleUtil implements Wrapper {

    public static float[] calculateAngles(BlockPos blockPos) {
        return calculateAngle(InterpolationUtil.interpolateEntityTime(mc.player, mc.getRenderPartialTicks()), new Vec3d(blockPos));
    }

    public static float[] calculateCenter(BlockPos blockPos) {
        return calculateAngle(InterpolationUtil.interpolateEntityTime(mc.player, mc.getRenderPartialTicks()), new Vec3d(blockPos).add(new Vec3d(0.5, 0, 0.5)));
    }

    public static float[] calculateAngle(Vec3d to) {
        return calculateAngle(mc.player.getPositionEyes(1), to);
    }

    public static float[] calculateAngle(Vec3d from, Vec3d to) {
        return calculateAngles(to.subtract(from));
    }

     public static float[] calculateAngles(Vec3d vector) {
        float yaw = (float) (Math.toDegrees(Math.atan2(vector.z, vector.x)) - 90);
        float pitch = (float) Math.toDegrees(-Math.atan2(vector.y, Math.hypot(vector.x, vector.z)));

        return new float[] {
                MathHelper.wrapDegrees(yaw),
                MathHelper.wrapDegrees(pitch)
        };
    }

    public static Vec3d getVectorForRotation(Rotation rotation) {
        float yawCos = MathHelper.cos(-rotation.getYaw() * 0.017453292F - (float) Math.PI);
        float yawSin = MathHelper.sin(-rotation.getYaw() * 0.017453292F - (float) Math.PI);
        float pitchCos = -MathHelper.cos(-rotation.getPitch() * 0.017453292F);
        float pitchSin = MathHelper.sin(-rotation.getPitch() * 0.017453292F);
        return new Vec3d(yawSin * pitchCos, pitchSin, yawCos * pitchCos);
    }

    public static float calculateAngleDifference(float from, float to) {
        return Math.abs(from - to) % 180;
    }
}
