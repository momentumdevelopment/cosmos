package cope.cosmos.client.features.modules.visual;

import cope.cosmos.client.events.render.other.CameraClipEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author aesthetical, linustouchtips
 * @since 10/17/2021
 */
public class CameraClipModule extends Module {
    public static CameraClipModule INSTANCE;

    public CameraClipModule() {
        super("CameraClip", new String[] {"ViewClip"}, Category.VISUAL, "Clips your third person camera through blocks");
        INSTANCE = this;
    }

    // **************************** general settings ****************************

    public static Setting<Double> distance = new Setting<>("Distance", 1.0, 5.0, 20.0, 0)
            .setAlias("Clip", "ClipDistance")
            .setDescription("How many blocks the camera should clip through");

    @SubscribeEvent
    public void onCameraClip(CameraClipEvent event) {

        // override the vanilla camera clip distance
        event.setDistance(distance.getValue());
    }
}
