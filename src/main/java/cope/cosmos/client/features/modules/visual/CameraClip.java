package cope.cosmos.client.features.modules.visual;

import cope.cosmos.client.events.CameraClipEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author Sxmurai, linustouchtips
 * @since 10/17/2021
 */
@SuppressWarnings("unused")
public class CameraClip extends Module {
    public static CameraClip INSTANCE;

    public CameraClip() {
        super("CameraClip", Category.VISUAL, "Clips your third person camera through blocks");
        INSTANCE = this;
    }

    public static Setting<Double> distance = new Setting<>("Distance", "How many blocks the camera should clip through", 1.0, 5.0, 20.0, 0);

    @SubscribeEvent
    public void onCameraClip(CameraClipEvent event) {
        // override the vanilla camera clip distance
        event.setDistance(distance.getValue());
    }
}
