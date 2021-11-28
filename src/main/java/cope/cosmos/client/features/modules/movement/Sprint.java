package cope.cosmos.client.features.modules.movement;

import cope.cosmos.client.events.LivingUpdateEvent;
import cope.cosmos.client.events.MotionEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.MotionUtil;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@SuppressWarnings("unused")
public class Sprint extends Module {
    public static Sprint INSTANCE;

    public Sprint() {
        super("Sprint", Category.MOVEMENT, "Sprints continuously");
        INSTANCE = this;
    }

    public static Setting<Mode> mode = new Setting<>("Mode", "Mode for sprint", Mode.DIRECTIONAL);
    public static Setting<Boolean> safe = new Setting<>("Safe", "Stops sprinting when you don't have the required hunger", false);
    public static Setting<Boolean> strict = new Setting<>("Strict", "Stops sprinting when sneaking and using items", false);

    @Override
    public void onUpdate() {
        // reset sprint state
        mc.player.setSprinting(false);

        switch (mode.getValue()) {
            case DIRECTIONAL:
                mc.player.setSprinting(handleSprint() && MotionUtil.isMoving());
                break;
            case NORMAL:
                mc.player.setSprinting(handleSprint() && MotionUtil.isMoving() && !mc.player.collidedHorizontally && mc.gameSettings.keyBindForward.isKeyDown());
                break;
        }
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingUpdateEvent event) {
        event.setCanceled(handleSprint() && MotionUtil.isMoving() && mode.getValue().equals(Mode.DIRECTIONAL));
    }

    public boolean handleSprint() {
        if (mc.player.getFoodStats().getFoodLevel() <= 6 && safe.getValue()) {
            return false;
        }

        else {
            return (!mc.player.isHandActive() && !mc.player.isSneaking()) || !strict.getValue();
        }
    }

    public enum Mode {
        DIRECTIONAL, NORMAL
    }
}