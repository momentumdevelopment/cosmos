package cope.cosmos.client.features.modules.movement;

import cope.cosmos.asm.mixins.accessor.IEntity;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.MotionUtil;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.world.chunk.EmptyChunk;

/**
 * @author linustouchtips
 * @since 02/25/2022
 */
public class EntitySpeedModule extends Module {
    public static EntitySpeedModule INSTANCE;

    public EntitySpeedModule() {
        super("EntitySpeed", Category.MOVEMENT, "Allows you to move faster while riding entities");
        INSTANCE = this;
    }

    public static Setting<Double> speed = new Setting<>("Speed", 0.0, 1.0, 3.0, 2).setDescription("Speed when moving");

    @Override
    public void onUpdate() {
        if (mc.player.isRiding() && mc.player.getRidingEntity() != null) {

            // keep yaw consistent
            if (mc.player.getRidingEntity() instanceof EntityLlama) {
                mc.player.getRidingEntity().rotationYaw = mc.player.rotationYaw;

                // lololol
                ((EntityLlama) mc.player.getRidingEntity()).rotationYawHead = mc.player.rotationYawHead;
            }

            // make sure the entity is not in a liquid
            if (mc.player.getRidingEntity().isInWater() || mc.player.getRidingEntity().isInLava()) {
                return;
            }

            // make sure the entity is not in a web
            if (((IEntity) mc.player.getRidingEntity()).getInWeb()) {
                return;
            }

            // make sure the entity can have speed applied
            if (mc.player.getRidingEntity().fallDistance > 2) {
                return;
            }

            // current speed
            double moveSpeed = speed.getValue();

            // the current movement input values of the user
            float forward = mc.player.movementInput.moveForward;
            float strafe = mc.player.movementInput.moveStrafe;
            float yaw = mc.player.rotationYaw;

            // if we're not inputting any movements, then we shouldn't be adding any motion
            if (!MotionUtil.isMoving()) {
                mc.player.getRidingEntity().motionX = 0;
                mc.player.getRidingEntity().motionZ = 0;
            }

            // prevent entity from moving into unloaded chunks
            else if (mc.world.getChunkFromChunkCoords((int) (mc.player.getRidingEntity().posX + mc.player.getRidingEntity().motionX) >> 4, (int) (mc.player.getRidingEntity().posZ + mc.player.getRidingEntity().motionX) >> 4) instanceof EmptyChunk) {
                mc.player.getRidingEntity().motionX = 0;
                mc.player.getRidingEntity().motionZ = 0;
            }

            else {

                // find the rotations and inputs based on our current movements
                if (strafe > 0) {
                    yaw += forward > 0 ? -45 : 45;
                }

                else if (strafe < 0) {
                    yaw += forward > 0 ? 45 : -45;
                }

                strafe = 0;

                if (forward > 0) {
                    forward = 1;
                }

                else if (forward < 0) {
                    forward = -1;
                }

                // our facing values, according to movement not rotations
                double cos = Math.cos(Math.toRadians(yaw + 90));
                double sin = Math.sin(Math.toRadians(yaw + 90));

                // update the movements
                mc.player.getRidingEntity().motionX = (forward * moveSpeed * cos) + (strafe * moveSpeed * sin);
                mc.player.getRidingEntity().motionZ = (forward * moveSpeed * sin) - (strafe * moveSpeed * cos);

                // if we're not inputting any movements, then we shouldn't be adding any motion
                if (!MotionUtil.isMoving()) {
                    mc.player.getRidingEntity().motionX = 0;
                    mc.player.getRidingEntity().motionZ = 0;
                }
            }
        }
    }
}