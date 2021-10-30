package cope.cosmos.client.features.modules.movement;

import cope.cosmos.asm.mixins.accessor.IEntity;
import cope.cosmos.client.events.MotionEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.PlayerUtil;
import cope.cosmos.util.world.HoleUtil;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@SuppressWarnings("unused")
public class ReverseStep extends Module {
    public static ReverseStep INSTANCE;

    public ReverseStep() {
        super("ReverseStep", Category.MOVEMENT, "Allows you to fall faster");
        INSTANCE = this;
    }

    public static Setting<Mode> mode = new Setting<>("Mode", "Mode for pulling down", Mode.MOTION);
    public static Setting<Double> speed = new Setting<>(() -> mode.getValue().equals(Mode.MOTION) || mode.getValue().equals(Mode.TIMER), "Speed", "Pull down speed", 0.0, 1.0, 10.0, 2);
    public static Setting<Double> tickShift = new Setting<>(() -> mode.getValue().equals(Mode.TICKSHIFT), "ShiftTicks", "Ticks to shift", 1.0, 1.0, 5.0, 0);
    public static Setting<Double> height = new Setting<>("Height", "Maximum height to be pulled down", 0.0, 2.0, 5.0, 1);
    public static Setting<Boolean> hole = new Setting<>("OnlyHole", "Only pulls you down into holes", false);
    public static Setting<Boolean> webs = new Setting<>("Webs", "pulls you down in webs", false);

    @SubscribeEvent
    public void onMotion(MotionEvent event) {
        if (PlayerUtil.isInLiquid() || mc.player.capabilities.isFlying || mc.player.isElytraFlying() || mc.player.isOnLadder() || mc.gameSettings.keyBindJump.isKeyDown() || mc.player.fallDistance > height.getValue() || hole.getValue() && !HoleUtil.isAboveHole(height.getValue()) || ((IEntity) mc.player).getInWeb() && !webs.getValue()) {
            getCosmos().getTickManager().setClientTicks(1);
            return;
        }

        event.setCanceled(true);
        if (mc.player.onGround) {
            switch (mode.getValue()) {
                case MOTION:
                    mc.player.connection.sendPacket(new CPacketPlayer(false));
                    mc.player.motionY = -speed.getValue();
                    break;
                case TICKSHIFT:
                    // shift ticks
                    getCosmos().getTickManager().shiftServerTicks(tickShift.getValue().intValue());
                    mc.player.motionY *= 1.75;
                    break;
                case TIMER:
                    getCosmos().getTickManager().setClientTicks(speed.getValue().floatValue() * 2);
                    break;
            }
        }
    }


    public enum Mode {
        MOTION, TICKSHIFT, TIMER
    }
}
