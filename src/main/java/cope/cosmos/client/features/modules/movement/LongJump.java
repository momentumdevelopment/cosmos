package cope.cosmos.client.features.modules.movement;

import cope.cosmos.client.events.MotionEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import net.minecraft.init.MobEffects;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LongJump extends Module {

    public static Setting<Double> speed = new Setting<>("Speed", 0.2, 0.4, 1d, 2);
    public static Setting<Double> accelerate = new Setting<>("Accelerate",0.92,0.92,1.10,2);
    public static Setting<Double> glide = new Setting<>("Glide", 0.0, 0.5, 1d, 2);
    public LongJump INSTANCE;
    double currentSpeed;

    public LongJump() {
        super("LongJump", Category.MOVEMENT, "Jump further after taking damage");
    }

    @SubscribeEvent
    public void onMotion(MotionEvent event) {

        event.setCanceled(true);

        if (mc.player.onGround) {
            event.setY(.42);
            mc.player.motionY = .42;
            currentSpeed = speed.getValue();

            if (mc.player.isPotionActive(MobEffects.SPEED)) {
                int amplifier = mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier();
                currentSpeed *= 1 + (0.2 * (amplifier + 1));
            }

        } else if (mc.player.motionY < 0) {

            event.setY(event.getY() * glide.getValue());

        }

        event.setX(dir(currentSpeed)[0]);
        event.setZ(dir(currentSpeed)[1]);

        currentSpeed *= accelerate.getValue();

    }

    double[] dir(double currentSpeed) {

        double forward = mc.player.moveForward;
        double strafe = mc.player.moveStrafing;
        double yaw = mc.player.rotationYaw;

        if (strafe > 0) {
            yaw += forward > 0 ? -45 : 45;
        } else if (strafe < 0) {
            yaw += forward > 0 ? 45 : -45;
        }

        strafe = 0;

        if (forward > 0) {
            forward = 1;
        } else if (forward < 0) {
            forward = -1;
        }

        double cos = Math.cos(Math.toRadians(yaw + 90));
        double sin = Math.sin(Math.toRadians(yaw + 90));

        return new double[]{((forward * currentSpeed * cos) + (strafe * currentSpeed * sin)), ((forward * currentSpeed * sin) - (strafe * currentSpeed * cos))};
    }

}
