package cope.cosmos.client.features.modules.movement;

import cope.cosmos.client.events.MotionEvent;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.MotionUtil;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LongJump extends Module {

    public static Setting<Double> speed = new Setting<>("Boost", 0d, .6, 1d, 2);
    public static Setting<Boolean> low = new Setting<>("Low",false);
    public static Setting<Double> accelerate = new Setting<>("Accelerate",0.92,0.92,1.10,2);
    public static Setting<Double> glide = new Setting<>("Glide", 0.0, .65, 1d, 2);
    public static Setting<Boolean> disable = new Setting<>("AutoDisable", true).setDescription("Disable on rubberband");

    public LongJump INSTANCE;
    double currentSpeed;

    public LongJump() {
        super("LongJump", Category.MOVEMENT, "Jump further after taking damage");
    }

    @SubscribeEvent
    public void onMotion(MotionEvent event) {

        event.setCanceled(true);

        if (mc.player.onGround && MotionUtil.isMoving()) {
            event.setY(.42);
            mc.player.motionY = low.getValue() ? 0 : .42;
            currentSpeed = speed.getValue();

            if (mc.player.isPotionActive(MobEffects.SPEED)) {
                int amplifier = mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier();
                currentSpeed *= 1 + (0.2 * (amplifier + 1));
            }

        } else if (mc.player.motionY < 0) {

            event.setY(event.getY() * glide.getValue());

        }

        event.setX(MotionUtil.getMoveSpeed(Math.max(currentSpeed + .20855, 0.28055))[0]);
        event.setZ(MotionUtil.getMoveSpeed(Math.max(currentSpeed + .20855, 0.28055))[1]);

        currentSpeed *= accelerate.getValue();

    }

    @SubscribeEvent
    void onPacket(PacketEvent.PacketReceiveEvent event) {

        if (event.getPacket() instanceof SPacketPlayerPosLook && disable.getValue())
            disable();

    }

}
