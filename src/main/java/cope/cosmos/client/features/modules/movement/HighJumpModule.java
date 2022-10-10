package cope.cosmos.client.features.modules.movement;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;

/**
 * @author linustouchtips
 * @since 10/09/2022
 */
public class HighJumpModule extends Module {
    public static HighJumpModule INSTANCE;

    public HighJumpModule() {
        super("HighJump", new String[] {"JetPack"}, Category.MOVEMENT, "Allows you to jump higher");
        INSTANCE = this;
    }

    // **************************** general ****************************

    public static Setting<Double> height = new Setting<>("Height", 0.1, 0.3, 5.0, 1)
            .setDescription("Maximum height to be pulled down");

    public static Setting<Boolean> inAir = new Setting<>("InAir", false)
            .setDescription("Allows you to jump in the air");

    @Override
    public void onTick() {

        // jump input
        if (mc.gameSettings.keyBindJump.isPressed()) {

            // check if player is on ground
            if (mc.player.onGround || inAir.getValue()) {

                // jump motion
                mc.player.motionY = height.getValue();
            }
        }
    }
}
