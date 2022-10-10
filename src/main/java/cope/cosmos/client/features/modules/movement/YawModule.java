package cope.cosmos.client.features.modules.movement;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;

/**
 * @author linustouchtips
 * @since 10/09/2022
 */
public class YawModule extends Module {
    public static YawModule INSTANCE;

    public YawModule() {
        super("Yaw", new String[] {"YawLock"}, Category.MOVEMENT, "Locks yaw to cardinal axis");
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {

        // rotation rounded to nearest cardinal direction
        float yaw = Math.round(mc.player.rotationYaw / 90F) * 90;

        // lock riding entity yaw
        if (mc.player.isRiding()) {
            mc.player.getRidingEntity().rotationYaw = yaw;
        }

        // lock yaw
        mc.player.rotationYaw = yaw;
        mc.player.rotationYawHead = yaw;
    }
}
