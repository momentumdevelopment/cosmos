package cope.cosmos.utility.player;

import cope.cosmos.utility.IUtility;
import net.minecraft.entity.Entity;

public class MotionUtil implements IUtility {

    public static void setMoveSpeed(double speed, float stepHeight) {
        Entity currentMover = mc.player.isRiding() ? mc.player.getRidingEntity() : mc.player;

        if (currentMover != null) {
            float forward = mc.player.movementInput.moveForward;
            float strafe = mc.player.movementInput.moveStrafe;
            float yaw = mc.player.rotationYaw;

            if (!MotionUtil.isMoving()) {
                currentMover.motionX = 0;
                currentMover.motionZ = 0;
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

            currentMover.motionX = (double) forward * speed * cos + (double) strafe * speed * sin;
            currentMover.motionZ = (double) forward * speed * sin - (double) strafe * speed * cos;
            currentMover.stepHeight = stepHeight;

            if (!MotionUtil.isMoving()) {
                currentMover.motionX = 0;
                currentMover.motionZ = 0;
            }
        }
    }
    
    public static double[] getMoveSpeed(double speed) {
        float forward = mc.player.movementInput.moveForward;
        float strafe = mc.player.movementInput.moveStrafe;
        float yaw = mc.player.rotationYaw;

        if (!MotionUtil.isMoving()) {
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

    public static void stopMotion(double fall) {
        Entity currentMover = mc.player.isRiding() ? mc.player.getRidingEntity() : mc.player;

        if (currentMover != null) {
            currentMover.setVelocity(0, fall, 0);
        }
    }

    public static boolean isMoving() {
        return (mc.player.moveForward != 0 || mc.player.moveStrafing != 0);
    }

    public static boolean hasMoved() {
        return Math.pow(mc.player.motionX, 2) + Math.pow(mc.player.motionY, 2) + Math.pow(mc.player.motionZ, 2) >= 9.0E-4;
    }
}
