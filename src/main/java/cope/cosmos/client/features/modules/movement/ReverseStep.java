package cope.cosmos.client.features.modules.movement;

import cope.cosmos.asm.mixins.accessor.IEntity;
import cope.cosmos.client.events.MotionEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.PlayerUtil;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@SuppressWarnings("unused")
public class ReverseStep extends Module {
    public static ReverseStep INSTANCE;

    public ReverseStep() {
        super("ReverseStep", Category.MOVEMENT, "Allows you to fall faster");
        INSTANCE = this;
    }

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.MOTION).setDescription("Mode for pulling down");
    public static Setting<Double> speed = new Setting<>("Speed", 0.0, 1.0, 10.0, 2).setDescription("Pull down speed").setVisible(() -> !mode.getValue().equals(Mode.TICK_SHIFT));
    public static Setting<Double> tickShift = new Setting<>("ShiftTicks", 1.0, 1.0, 5.0, 0).setDescription("Ticks to shift").setVisible(() -> mode.getValue().equals(Mode.TICK_SHIFT));
    public static Setting<Double> height = new Setting<>("Height", 0.0, 2.0, 5.0, 1).setDescription("Maximum height to be pulled down");
    public static Setting<Boolean> hole = new Setting<>("OnlyHole", false).setDescription("Only pulls you down into holes");
    public static Setting<Boolean> webs = new Setting<>("Webs", false).setDescription("Pulls you down in webs");

    @SubscribeEvent
    public void onMotion(MotionEvent event) {
        if (PlayerUtil.isInLiquid() || mc.player.capabilities.isFlying || mc.player.isElytraFlying() || mc.player.isOnLadder() || mc.gameSettings.keyBindJump.isKeyDown() || mc.player.fallDistance > height.getValue() || hole.getValue() && !getCosmos().getHoleManager().isInHole(mc.player) || ((IEntity) mc.player).getInWeb() && !webs.getValue()) {
            getCosmos().getTickManager().setClientTicks(1);
            return;
        }

        if (mc.player.onGround) {
            switch (mode.getValue()) {
                case MOTION:
                    mc.player.connection.sendPacket(new CPacketPlayer(false));
                    mc.player.motionY = -speed.getValue();
                    break;
                case TICK_SHIFT:
                    // shift ticks
                    // getCosmos().getTickManager().shiftServerTicks(tickShift.getValue().intValue());
                    mc.player.motionY = -speed.getValue();
                    break;
                case TIMER:
                    getCosmos().getTickManager().setClientTicks(speed.getValue().floatValue() * 2);
                    break;
            }
        }
    }

    public enum Mode {
        MOTION, TICK_SHIFT, TIMER
    }
}
