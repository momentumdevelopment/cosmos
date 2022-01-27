package cope.cosmos.client.features.modules.visual;

import cope.cosmos.client.events.RenderHeldItemAlphaEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author aesthetical
 * @since 1/26/2022
 */
public class ViewModel extends Module {
    public static ViewModel INSTANCE;

    public ViewModel() {
        super("ViewModel", Category.VISUAL, "Changes how your hands render");
        INSTANCE = this;
    }

    public static final Setting<Double> itemAlpha = new Setting<>("ItemAlpha", 0.0, 255.0, 255.0, 1).setDescription("The held item alpha to use");

    public static final Setting<Boolean> modifyFov = new Setting<>("ModifyFOV", false).setDescription("If to modify the item FOV");
    public static final Setting<Double> fov = new Setting<>("FOV", 70.0, 130.0, 200.0, 0).setDescription("The item FOV to use").setParent(modifyFov);

    public static final Setting<Double> translateX = new Setting<>("TranslateX", -2.0, 0.0, 2.0, 1).setDescription("The x translation coordinate");
    public static final Setting<Double> translateY = new Setting<>("TranslateY", -2.0, 0.0, 2.0, 1).setDescription("The y translation coordinate");
    public static final Setting<Double> translateZ = new Setting<>("TranslateZ", -2.0, 0.0, 2.0, 1).setDescription("The z translation coordinate");

    public static final Setting<Double> scaleX = new Setting<>("ScaleX", -2.0, 1.0, 2.0, 1).setDescription("The x scale factor");
    public static final Setting<Double> scaleY = new Setting<>("ScaleY", -2.0, 1.0, 2.0, 1).setDescription("The y scale factor");
    public static final Setting<Double> scaleZ = new Setting<>("ScaleZ", -2.0, 1.0, 2.0, 1).setDescription("The z scale factor");

    public static final Setting<Float> rotateX = new Setting<>("RotateX", -200.0f, 0.0f, 200.0f, 1).setDescription("The rotation x factor");
    public static final Setting<Float> rotateY = new Setting<>("RotateY", -200.0f, 0.0f, 200.0f, 1).setDescription("The rotation y factor");
    public static final Setting<Float> rotateZ = new Setting<>("RotateZ", -200.0f, 0.0f, 200.0f, 1).setDescription("The rotation z factor");

    @SubscribeEvent
    public void onRenderHand(RenderSpecificHandEvent event) {
        GlStateManager.translate(translateX.getValue(), translateY.getValue(), translateZ.getValue());
        GlStateManager.scale(scaleX.getValue(), scaleY.getValue(), scaleZ.getValue());

        // rotations
        GlStateManager.rotate(rotateX.getValue(), 1.0f, 0.0f, 0.0f);
        GlStateManager.rotate(rotateY.getValue(), 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(rotateZ.getValue(), 0.0f, 0.0f, 1.0f);
    }

    @SubscribeEvent
    public void onItemRenderFov(EntityViewRenderEvent.FOVModifier event) {
        if (modifyFov.getValue()) {
            event.setFOV(fov.getValue().floatValue());
        }
    }

    @SubscribeEvent
    public void onRenderItemAlpha(RenderHeldItemAlphaEvent event) {
        // divide our alpha value by 255 to get the color value (OpenGL uses 0-1)
        event.setAlpha((float) (itemAlpha.getValue() / 255.0));
    }
}
