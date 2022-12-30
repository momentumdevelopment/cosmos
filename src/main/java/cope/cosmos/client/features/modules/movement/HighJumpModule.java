package cope.cosmos.client.features.modules.movement;

import cope.cosmos.asm.mixins.accessor.IEntity;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.exploits.PacketFlightModule;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.PlayerUtil;

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
            .setAlias("JumpStrength", "JumpHeight")
            .setDescription("Maximum height to be pulled down");

    public static Setting<Boolean> inAir = new Setting<>("InAir", false)
            .setAlias("JetPack", "DoubleJump")
            .setDescription("Allows you to jump in the air");

    @Override
    public void onTick() {

        // cannot jump in water
        if (PlayerUtil.isInLiquid()) {
            return;
        }

        // cannot jump while flying
        if (PlayerUtil.isFlying() || PacketFlightModule.INSTANCE.isEnabled()) {
            return;
        }

        // cannot jump while in web
        if (((IEntity) mc.player).getInWeb()) {
            return;
        }

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
