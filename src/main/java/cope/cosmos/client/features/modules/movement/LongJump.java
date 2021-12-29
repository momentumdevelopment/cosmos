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

/**
 * @author Doogie13
 * @since 27/12/2021
 * */

public class LongJump extends Module {

    public static Setting<Double> speed = new Setting<>("Boost", 0d, .6, 1d, 2).setDescription("Initial speed");
    public static Setting<Boolean> low = new Setting<>("Low",false).setDescription("Do a lower hop");
    public static Setting<Double> accelerate = new Setting<>("Accelerate",0.92,0.92,1.10,2).setDescription("Increase or decrease speed every tick");
    public static Setting<Double> glide = new Setting<>("Glide", 0.0, .65, 1d, 2).setDescription("Decrease fall speed");
    public static Setting<Boolean> disable = new Setting<>("AutoDisable", true).setDescription("Disable on rubberband");

    public LongJump INSTANCE;
    double currentSpeed;

    public LongJump() {
        super("LongJump", Category.MOVEMENT, "Jump further after taking damage");
    }

    @SubscribeEvent
    public void onMotion(MotionEvent event) {

        // Vanilla movement
        event.setCanceled(true);

        // If we are onGround and are actually trying to move
        if (mc.player.onGround && MotionUtil.isMoving()) {
            // Set the event motion to .42
            event.setY(.42);

            // Only make the motion .42 if we want to do a normal hop
            mc.player.motionY = low.getValue() ? 0 : .42;

            // Reset our current speed
            currentSpeed = speed.getValue();

            // Apply speed effect if present
            if (mc.player.isPotionActive(MobEffects.SPEED)) {
                int amplifier = mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier();
                currentSpeed *= 1 + (0.2 * (amplifier + 1));
            }

        } else if (mc.player.motionY < 0) {

            // If we are falling, slow our fall
            event.setY(event.getY() * glide.getValue());

        }

        // Set motion to the speed (or minimum air speed, whichever is higher)
        event.setX(MotionUtil.getMoveSpeed(Math.max(currentSpeed + .20855, 0.28055))[0]);
        event.setZ(MotionUtil.getMoveSpeed(Math.max(currentSpeed + .20855, 0.28055))[1]);

        // Multiply speed by acceleration (for next tick)
        currentSpeed *= accelerate.getValue();

    }

    @SubscribeEvent
    void onPacket(PacketEvent.PacketReceiveEvent event) {

        // Disable on rubberband or teleport
        if (event.getPacket() instanceof SPacketPlayerPosLook && disable.getValue())
            disable();

    }

}
