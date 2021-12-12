package cope.cosmos.client.features.modules.movement;

import cope.cosmos.client.events.LivingUpdateEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.MotionUtil;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 06/08/2021
 */
@SuppressWarnings("unused")
public class Sprint extends Module {
    public static Sprint INSTANCE;

    public Sprint() {
        super("Sprint", Category.MOVEMENT, "Sprints continuously");
        INSTANCE = this;
    }

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.DIRECTIONAL).setDescription("Mode for sprint");
    public static Setting<Boolean> safe = new Setting<>("Safe", false).setDescription("Stops sprinting when you don't have the required hunger");
    public static Setting<Boolean> strict = new Setting<>("Strict", false).setDescription("Stops sprinting when sneaking and using items");

    @Override
    public void onUpdate() {
        // reset sprint state
        mc.player.setSprinting(false);

        // verify if the player's food level allows sprinting
        if (mc.player.getFoodStats().getFoodLevel() <= 6 && safe.getValue()) {
            return;
        }

        // verify whether or not the player can actually sprint
        if ((mc.player.isHandActive() || mc.player.isSneaking()) && strict.getValue()) {
            return;
        }

        // update player sprint state
        switch (mode.getValue()) {
            case DIRECTIONAL:
                mc.player.setSprinting(MotionUtil.isMoving());
                break;
            case NORMAL:
                mc.player.setSprinting(MotionUtil.isMoving() && !mc.player.collidedHorizontally && mc.gameSettings.keyBindForward.isKeyDown());
                break;
        }
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (MotionUtil.isMoving() && mode.getValue().equals(Mode.DIRECTIONAL)) {
            // verify if the player's food level allows sprinting
            if (mc.player.getFoodStats().getFoodLevel() <= 6 && safe.getValue()) {
                return;
            }

            // verify whether or not the player can actually sprint
            if ((mc.player.isHandActive() || mc.player.isSneaking()) && strict.getValue()) {
                return;
            }

            // cancel vanilla sprint direction
            event.setCanceled(true);
        }
    }

    public enum Mode {
        /**
         * Allows you to sprint in all directions
         */
        DIRECTIONAL,

        /**
         * Allows sprinting when moving forward
         */
        NORMAL
    }
}