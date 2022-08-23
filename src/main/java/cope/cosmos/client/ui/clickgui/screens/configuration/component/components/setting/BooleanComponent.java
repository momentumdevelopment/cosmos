package cope.cosmos.client.ui.clickgui.screens.configuration.component.components.setting;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.client.SettingUpdateEvent;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.ui.clickgui.screens.configuration.component.ClickType;
import cope.cosmos.client.ui.clickgui.screens.configuration.component.components.module.ModuleComponent;
import cope.cosmos.client.ui.util.animation.Animation;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.string.ColorUtil;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

import static org.lwjgl.opengl.GL11.glScaled;

/**
 * @author linustouchtips
 * @since 01/31/2022
 */
public class BooleanComponent extends SettingComponent<Boolean> {

    // feature offset
    private float featureHeight;

    // animation
    private final Animation animation = new Animation(100, getSetting().getValue());
    private int hoverAnimation;

    public BooleanComponent(ModuleComponent moduleComponent, Setting<Boolean> setting) {
        super(moduleComponent, setting);
        Cosmos.EVENT_BUS.register(this);
    }

    @Override
    public void drawComponent() {
        super.drawComponent();

        // feature height
        featureHeight = getModuleComponent().getCategoryFrameComponent().getPosition().y + getModuleComponent().getCategoryFrameComponent().getTitle() + getModuleComponent().getSettingComponentOffset() + getModuleComponent().getCategoryFrameComponent().getScroll() + 2;

        // hover alpha animation
        if (isMouseOver(getModuleComponent().getCategoryFrameComponent().getPosition().x, featureHeight, getModuleComponent().getCategoryFrameComponent().getWidth(), HEIGHT) && hoverAnimation < 25) {
            hoverAnimation += 5;
        }

        else if (!isMouseOver(getModuleComponent().getCategoryFrameComponent().getPosition().x, featureHeight, getModuleComponent().getCategoryFrameComponent().getWidth(), HEIGHT) && hoverAnimation > 0) {
            hoverAnimation -= 5;
        }

        // feature background
        RenderUtil.drawRect(getModuleComponent().getCategoryFrameComponent().getPosition().x, featureHeight, getModuleComponent().getCategoryFrameComponent().getWidth(), HEIGHT, new Color(12 + hoverAnimation, 12 + hoverAnimation, 17 + hoverAnimation, 255));
        RenderUtil.drawRect(getModuleComponent().getCategoryFrameComponent().getPosition().x, featureHeight, 2, HEIGHT, ColorUtil.getPrimaryColor());

        // checkbox background
        RenderUtil.drawRoundedRect(getModuleComponent().getCategoryFrameComponent().getPosition().x + getModuleComponent().getCategoryFrameComponent().getWidth() - 12, featureHeight + 2, 10, 10, 2, new Color(22 + hoverAnimation, 22 + hoverAnimation, 28 + hoverAnimation));

        if (animation.getAnimationFactor() > 0) {
            RenderUtil.drawRoundedRect(getModuleComponent().getCategoryFrameComponent().getPosition().x + getModuleComponent().getCategoryFrameComponent().getWidth() - 7 - (5 * animation.getAnimationFactor()), featureHeight + 7 - (5 * animation.getAnimationFactor()), 10 * animation.getAnimationFactor(), 10 * animation.getAnimationFactor(), 2, ColorUtil.getPrimaryColor());
        }

        // setting name
        glScaled(0.55, 0.55, 0.55); {
            float scaledX = (getModuleComponent().getCategoryFrameComponent().getPosition().x + 6) * 1.81818181F;
            float scaledY = (featureHeight + 5) * 1.81818181F;
            FontUtil.drawStringWithShadow(getSetting().getName(), scaledX, scaledY, -1);
        }

        glScaled(1.81818181, 1.81818181, 1.81818181);
    }

    @Override
    public void onClick(ClickType in) {
        // toggle the boolean if clicked
        if (in.equals(ClickType.LEFT) && isMouseOver(getModuleComponent().getCategoryFrameComponent().getPosition().x, featureHeight, getModuleComponent().getCategoryFrameComponent().getWidth(), HEIGHT)) {

            // module feature bounds
            float highestPoint = featureHeight;
            float lowestPoint = highestPoint + HEIGHT;

            // check if it's able to be interacted with
            if (highestPoint >= getModuleComponent().getCategoryFrameComponent().getPosition().y + getModuleComponent().getCategoryFrameComponent().getTitle() + 2 && lowestPoint <= getModuleComponent().getCategoryFrameComponent().getPosition().y + getModuleComponent().getCategoryFrameComponent().getTitle() + getModuleComponent().getCategoryFrameComponent().getHeight() + 2) {
                boolean previousValue = getSetting().getValue();

                // update values
                animation.setState(!previousValue);
                getSetting().setValue(!previousValue);
            }

            // play a sound to make the user happy :)
            getCosmos().getSoundManager().playSound("click");
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onSettingUpdate(SettingUpdateEvent event) {

        // this setting component has been update
        if (event.getSetting().equals(getSetting())) {

            // update anim hard, helps sync animations to actual setting value
            animation.setStateHard((((Setting<Boolean>) event.getSetting()).getValue()));
        }
    }
}
