package cope.cosmos.client.features.modules.movement;

import cope.cosmos.client.events.motion.movement.MotionEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 10/02/2022
 */
public class FastSwimModule extends Module {
    public static FastSwimModule INSTANCE;

    public FastSwimModule() {
        super("FastSwim", Category.MOVEMENT, "Allows you to swim faster");
        INSTANCE = this;
    }

    // **************************** speeds ****************************

    public static Setting<Double> waterSpeed = new Setting<>("WaterSpeed", 0.0, 1.0, 3.0, 2)
            .setDescription("Speed while in water");

    public static Setting<Double> lavaSpeed = new Setting<>("LavaSpeed", 0.0, 1.0, 3.0, 2)
            .setDescription("Speed while in lava");

    // **************************** general ****************************

    public static Setting<Boolean> depthStrider = new Setting<>("DepthStrider", true)
            .setDescription("Takes into account the depth strider modifier");

    @SubscribeEvent
    public void onMotion(MotionEvent event) {

    }
}
