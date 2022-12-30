package cope.cosmos.client.features.modules.movement;

import cope.cosmos.asm.mixins.accessor.IEntity;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.exploits.PacketFlightModule;
import cope.cosmos.util.player.PlayerUtil;

/**
 * @author linustouchtips
 * @since 10/10/2022
 */
public class ParkourModule extends Module {
    public static ParkourModule INSTANCE;

    public ParkourModule() {
        super("Parkour", new String[] {"EdgeJump"}, Category.MOVEMENT, "Jumps at the edge of blocks");
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {

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

        // check if on ground
        if (mc.player.onGround) {

            // check if player should jump
            if (!mc.player.isSneaking() && !mc.gameSettings.keyBindJump.isPressed()) {

                // check if player is at the edge of a block
                if (mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0, -0.5, 0).expand(-0.001, 0, -0.001)).isEmpty()) {

                    // jump
                    mc.player.jump();
                }
            }
        }
    }
}
