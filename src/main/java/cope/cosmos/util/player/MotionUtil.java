package cope.cosmos.util.player;

import cope.cosmos.util.Wrapper;
import cope.cosmos.util.math.MathUtil;
import net.minecraft.entity.Entity;

/**
 * @author linustouchtips
 * @since 04/11/2021
 */
public class MotionUtil implements Wrapper {

    public static double[] getMoveSpeed(double speed) {
        float forward = mc.player.movementInput.moveForward;
        float strafe = mc.player.movementInput.moveStrafe;
        float yaw = mc.player.rotationYaw;

        if (!isMoving()) {
            return new double[] { 0, 0 };
        }

        else if (forward != 0) {
            if (strafe >= 1) {
                yaw += (float) (forward > 0 ? -45 : 45);
                strafe = 0;
            }

            else if (strafe <= -1) {
                yaw += (float) (forward > 0 ? 45 : -45);
                strafe = 0;
            }

            if (forward > 0)
                forward = 1;

            else if (forward < 0)
                forward = -1;
        }

        double sin = Math.sin(Math.toRadians(yaw + 90));
        double cos = Math.cos(Math.toRadians(yaw + 90));

        double motionX = (double) forward * speed * cos + (double) strafe * speed * sin;
        double motionZ = (double) forward * speed * sin - (double) strafe * speed * cos;

        return new double[] {motionX, motionZ};
    }

    /**
     * Checks if the player is moving
     * @return Whether the player is moving
     */
    public static boolean isMoving() {
        return mc.player.moveForward != 0 || mc.player.moveStrafing != 0;
    }

    /**
     * Checks if the player has moved in the last tick
     * @return Whether the player has moved in the last tick
     */
    public static boolean hasMoved() {
        return StrictMath.pow(mc.player.motionX, 2) + StrictMath.pow(mc.player.motionY, 2) + StrictMath.pow(mc.player.motionZ, 2) >= 9.0E-4;
    }
}
