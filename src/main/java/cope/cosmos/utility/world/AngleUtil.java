package cope.cosmos.utility.world;

import cope.cosmos.utility.IUtility;
import cope.cosmos.utility.player.Rotation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AngleUtil implements IUtility {

    public static float[] calculateAngles(Vec3d to) {
        float yaw = (float) (Math.toDegrees(Math.atan2(to.subtract(mc.player.getPositionEyes(1)).z, to.subtract(mc.player.getPositionEyes(1)).x)) - 90);
        float pitch = (float) Math.toDegrees(-Math.atan2(to.subtract(mc.player.getPositionEyes(1)).y, Math.hypot(to.subtract(mc.player.getPositionEyes(1)).x, to.subtract(mc.player.getPositionEyes(1)).z)));

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
