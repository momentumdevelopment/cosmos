package cope.cosmos.client.features.modules.visual;

import cope.cosmos.client.events.CameraClipEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.event.annotation.Subscription;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author aesthetical, linustouchtips
 * @since 10/17/2021
 */
@SuppressWarnings("unused")
public class CameraClip extends Module {
    public static CameraClip INSTANCE;

    public CameraClip() {
        super("CameraClip", Category.VISUAL, "Clips your third person camera through blocks");
        INSTANCE = this;
    }

    public static Setting<Double> distance = new Setting<>("Distance", 1.0, 5.0, 20.0, 0).setDescription("How many blocks the camera should clip through");

    @Subscription
    public void onCameraClip(CameraClipEvent event) {
        // override the vanilla camera clip distance
        event.setDistance(distance.getValue());
    }
}
