package cope.cosmos.client.features.modules.visual;

import cope.cosmos.client.events.render.player.RenderEatingEvent;
import cope.cosmos.client.events.render.player.RenderHeldItemEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumHandSide;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author Surge, linustouchtips
 * @since 31/03/2022
 */
public class ViewModelModule extends Module {
    public static ViewModelModule INSTANCE;

    public ViewModelModule() {
        super("ViewModel", new String[] {"HandProgress", "SmallShield"}, Category.VISUAL, "Changes how items are rendered in first person");
        INSTANCE = this;
    }

    // TODO: Normalize eating anims

    // **************************** left hand ****************************

    public static Setting<Double> leftX = new Setting<>("LeftX", -2D, 0D, 2D, 2)
            .setDescription("The X position of the left item");

    public static Setting<Double> leftY = new Setting<>("LeftY", -2D, 0D, 2D, 2)
            .setDescription("The Y position of the left item");

    public static Setting<Double> leftZ = new Setting<>("LeftZ", -2D, 0D, 2D, 2)
            .setDescription("The Z position of the left item");

    public static Setting<Float> leftYaw = new Setting<>("LeftYaw", -180F, 0F, 180F, 1)
            .setDescription("The yaw rotation of the left item");

    public static Setting<Float> leftPitch = new Setting<>("LeftPitch", -180F, 0F, 180F, 1)
            .setDescription("The pitch rotation of the left item");

    public static Setting<Float> leftRoll = new Setting<>("LeftRoll", -180F, 0F, 180F, 1)
            .setDescription("The roll rotation of the left item");

    public static Setting<Float> leftScale = new Setting<>("LeftScale", 0F, 1F, 2F, 1)
            .setDescription("The scale of the left item");

    // **************************** right hand ****************************

    public static Setting<Double> rightX = new Setting<>("RightX", -2D, 0D, 2D, 2)
            .setDescription("The X position of the right item");

    public static Setting<Double> rightY = new Setting<>("RightY", -2D, 0D, 2D, 2)
            .setDescription("The Y position of the right item");

    public static Setting<Double> rightZ = new Setting<>("RightZ", -2D, 0D, 2D, 2)
            .setDescription("The Z position of the right item");

    public static Setting<Float> rightYaw = new Setting<>("RightYaw", -180F, 0F, 180F, 1)
            .setDescription("The yaw rotation of the right item");

    public static Setting<Float> rightPitch = new Setting<>("RightPitch", -180F, 0F, 180F, 1)
            .setDescription("The pitch rotation of the right item");

    public static Setting<Float> rightRoll = new Setting<>("RightRoll", -180F, 0F, 180F, 1)
            .setDescription("The roll rotation of the right item");

    public static Setting<Float> rightScale = new Setting<>("RightScale", 0F, 1F, 2F, 1)
            .setDescription("The scale of the right item");

    @SubscribeEvent
    public void onRenderItemPre(RenderHeldItemEvent.Pre event) {

        // Translate and scale items
        switch (event.getSide()) {
            case LEFT:
                GlStateManager.translate(leftX.getValue(), leftY.getValue(), leftZ.getValue());
                GlStateManager.scale(leftScale.getValue(), leftScale.getValue(), leftScale.getValue());
                break;
            case RIGHT:
                GlStateManager.translate(rightX.getValue(), rightY.getValue(), rightZ.getValue());
                GlStateManager.scale(rightScale.getValue(), rightScale.getValue(), rightScale.getValue());
                break;
        }
    }

    @SubscribeEvent
    public void onRenderItemPost(RenderHeldItemEvent.Post event) {

        // Rotate items
        switch (event.getSide()) {
            case LEFT:
                GlStateManager.rotate(leftYaw.getValue(), 0, 1, 0);
                GlStateManager.rotate(leftPitch.getValue(), 1, 0, 0);
                GlStateManager.rotate(leftRoll.getValue(), 0, 0, 1);
                break;

            case RIGHT:
                GlStateManager.rotate(rightYaw.getValue(), 0, 1, 0);
                GlStateManager.rotate(rightPitch.getValue(), 1, 0, 0);
                GlStateManager.rotate(rightRoll.getValue(), 0, 0, 1);
                break;
        }
    }

    @SubscribeEvent
    public void onEatingRender(RenderEatingEvent event) {

        // Prevent the eating animation
        event.setCanceled(true);
        event.setScale(event.getHandSide().equals(EnumHandSide.LEFT) ? leftScale.getValue() : rightScale.getValue());
    }
}
