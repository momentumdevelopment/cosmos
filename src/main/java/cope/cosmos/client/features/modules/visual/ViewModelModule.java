package cope.cosmos.client.features.modules.visual;

import cope.cosmos.client.events.render.player.RenderHeldItemAlphaEvent;
import cope.cosmos.client.events.render.player.RenderHeldItemEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author aesthetical
 * @since 1/26/2022
 */
public class ViewModelModule extends Module {
    public static ViewModelModule INSTANCE;

    public ViewModelModule() {
        super("ViewModel", Category.VISUAL, "Changes how your hands render");
        INSTANCE = this;
    }

    public static Setting<Double> itemAlpha = new Setting<>("ItemAlpha", 0.0, 255.0, 255.0, 1).setDescription("The held item alpha to use");

    public static Setting<Double> translateX = new Setting<>("TranslateX", -2.0, 0.0, 2.0, 1).setDescription("The x translation coordinate");
    public static Setting<Double> translateY = new Setting<>("TranslateY", -2.0, 0.0, 2.0, 1).setDescription("The y translation coordinate");
    public static Setting<Double> translateZ = new Setting<>("TranslateZ", -2.0, 0.0, 2.0, 1).setDescription("The z translation coordinate");

    public static Setting<Double> scaleX = new Setting<>("ScaleX", -2.0, 1.0, 2.0, 1).setDescription("The x scale factor");
    public static Setting<Double> scaleY = new Setting<>("ScaleY", -2.0, 1.0, 2.0, 1).setDescription("The y scale factor");
    public static Setting<Double> scaleZ = new Setting<>("ScaleZ", -2.0, 1.0, 2.0, 1).setDescription("The z scale factor");

    public static Setting<Float> rotateX = new Setting<>("RotateX", -200.0F, 0.0F, 200.0F, 1).setDescription("The rotation x factor");
    public static Setting<Float> rotateY = new Setting<>("RotateY", -200.0F, 0.0F, 200.0F, 1).setDescription("The rotation y factor");
    public static Setting<Float> rotateZ = new Setting<>("RotateZ", -200.0F, 0.0F, 200.0F, 1).setDescription("The rotation z factor");

    @SubscribeEvent
    public void onRenderHand(RenderSpecificHandEvent event) {
        GlStateManager.translate(translateX.getValue(), translateY.getValue(), translateZ.getValue());
    }

    @SubscribeEvent
    public void onHeldItemRender(RenderHeldItemEvent event) {
        // scale items
        GlStateManager.scale(scaleX.getValue(), scaleY.getValue(), scaleZ.getValue());

        // rotate items
        GlStateManager.rotate(rotateX.getValue(), 1, 0, 0);
        GlStateManager.rotate(rotateY.getValue(), 0, 1, 0);
        GlStateManager.rotate(rotateZ.getValue(), 0, 0, 1);
    }

    @SubscribeEvent
    public void onRenderItemAlpha(RenderHeldItemAlphaEvent event) {
        // divide our alpha value by 255 to get the color value (OpenGL uses 0-1)
        event.setAlpha(itemAlpha.getValue().floatValue() / 255F);
    }
}
