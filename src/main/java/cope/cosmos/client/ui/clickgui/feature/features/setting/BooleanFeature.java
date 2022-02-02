package cope.cosmos.client.ui.clickgui.feature.features.setting;

import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.ui.clickgui.feature.ClickType;
import cope.cosmos.client.ui.clickgui.feature.features.module.ModuleFeature;
import cope.cosmos.client.ui.util.Animation;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.string.ColorUtil;

import java.awt.*;

import static org.lwjgl.opengl.GL11.glScaled;

/**
 * @author linustouchtips
 * @since 01/31/2022
 */
public class BooleanFeature extends SettingFeature<Boolean> {

    // feature offset
    private float featureHeight;

    // animation
    private final Animation animation = new Animation(100, getSetting().getValue());
    private int hoverAnimation;

    public BooleanFeature(ModuleFeature moduleFeature, Setting<Boolean> setting) {
        super(moduleFeature, setting);
    }

    @Override
    public void drawFeature() {
        super.drawFeature();

        // feature height
        featureHeight = (float) (getModuleFeature().getCategoryFrameFeature().getPosition().y + getModuleFeature().getCategoryFrameFeature().getTitle() + getModuleFeature().getSettingFeatureOffset() + 2);

        // hover alpha animation
        if (isMouseOver(getModuleFeature().getCategoryFrameFeature().getPosition().x, featureHeight, getModuleFeature().getCategoryFrameFeature().getWidth(), HEIGHT) && hoverAnimation < 25) {
            hoverAnimation += 5;
        }

        else if (!isMouseOver(getModuleFeature().getCategoryFrameFeature().getPosition().x, featureHeight, getModuleFeature().getCategoryFrameFeature().getWidth(), HEIGHT) && hoverAnimation > 0) {
            hoverAnimation -= 5;
        }

        // feature background
        RenderUtil.drawRect(getModuleFeature().getCategoryFrameFeature().getPosition().x, featureHeight, getModuleFeature().getCategoryFrameFeature().getWidth(), HEIGHT, new Color(12 + hoverAnimation, 12 + hoverAnimation, 17 + hoverAnimation, 255));

        // checkbox background
        RenderUtil.drawRoundedRect(getModuleFeature().getCategoryFrameFeature().getPosition().x + getModuleFeature().getCategoryFrameFeature().getWidth() - 12, featureHeight + 2, 10, 10, 2, new Color(22 + hoverAnimation, 22 + hoverAnimation, 28 + hoverAnimation));

        if (animation.getAnimationFactor() > 0) {
            RenderUtil.drawRoundedRect(getModuleFeature().getCategoryFrameFeature().getPosition().x + getModuleFeature().getCategoryFrameFeature().getWidth() - 7 - (5 * animation.getAnimationFactor()), featureHeight + 7 - (5 * animation.getAnimationFactor()), 10 * animation.getAnimationFactor(), 10 * animation.getAnimationFactor(), 2, ColorUtil.getPrimaryColor());
        }

        // setting name
        glScaled(0.55, 0.55, 0.55); {
            float scaledX = (getModuleFeature().getCategoryFrameFeature().getPosition().x + 4) * 1.81818181F;
            float scaledY = (featureHeight + 5) * 1.81818181F;
            FontUtil.drawStringWithShadow(getSetting().getName(), scaledX, scaledY, -1);
        }

        glScaled(1.81818181, 1.81818181, 1.81818181);
    }

    @Override
    public void onClick(ClickType in) {
        // toggle the boolean if clicked
        if (in.equals(ClickType.LEFT) && isMouseOver(getModuleFeature().getCategoryFrameFeature().getPosition().x, featureHeight, getModuleFeature().getCategoryFrameFeature().getWidth(), HEIGHT)) {

            // module feature bounds
            float highestPoint = featureHeight;
            float lowestPoint = highestPoint + HEIGHT;

            // check if it's able to be interacted with
            if (highestPoint >= getModuleFeature().getCategoryFrameFeature().getPosition().y + getModuleFeature().getCategoryFrameFeature().getTitle() + 2 && lowestPoint <= getModuleFeature().getCategoryFrameFeature().getPosition().y + getModuleFeature().getCategoryFrameFeature().getTitle() + getModuleFeature().getCategoryFrameFeature().getHeight() + 2) {
                boolean previousValue = getSetting().getValue();

                // update values
                animation.setState(!previousValue);
                getSetting().setValue(!previousValue);
            }

            // play a sound to make the user happy :)
            getCosmos().getSoundManager().playSound("click");
        }
    }
}
