package cope.cosmos.client.ui.clickgui.feature.features.setting;

import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.ui.clickgui.feature.ClickType;
import cope.cosmos.client.ui.clickgui.feature.features.module.ModuleFeature;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.string.ColorUtil;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.lwjgl.opengl.GL11.glScaled;

/**
 * @author linustouchtips
 * @since 02/02/2022
 */
public class DrawnFeature extends SettingFeature<AtomicBoolean> {

    // feature offset
    private float featureHeight;

    // animation
    private int hoverAnimation;

    public DrawnFeature(ModuleFeature moduleFeature, Setting<AtomicBoolean> setting) {
        super(moduleFeature, setting);
    }

    @Override
    public void drawFeature() {
        super.drawFeature();

        // feature height
        featureHeight = getModuleFeature().getCategoryFrameFeature().getPosition().y + getModuleFeature().getCategoryFrameFeature().getTitle() + getModuleFeature().getSettingFeatureOffset() + getModuleFeature().getCategoryFrameFeature().getScroll() + 2;

        // hover alpha animation
        if (isMouseOver(getModuleFeature().getCategoryFrameFeature().getPosition().x, featureHeight, getModuleFeature().getCategoryFrameFeature().getWidth(), HEIGHT) && hoverAnimation < 25) {
            hoverAnimation += 5;
        }

        else if (!isMouseOver(getModuleFeature().getCategoryFrameFeature().getPosition().x, featureHeight, getModuleFeature().getCategoryFrameFeature().getWidth(), HEIGHT) && hoverAnimation > 0) {
            hoverAnimation -= 5;
        }

        // feature background
        RenderUtil.drawRect(getModuleFeature().getCategoryFrameFeature().getPosition().x, featureHeight, getModuleFeature().getCategoryFrameFeature().getWidth(), HEIGHT, new Color(12 + hoverAnimation, 12 + hoverAnimation, 17 + hoverAnimation, 255));
        RenderUtil.drawRect(getModuleFeature().getCategoryFrameFeature().getPosition().x, featureHeight, 2, HEIGHT, ColorUtil.getPrimaryColor());

        glScaled(0.55, 0.55, 0.55); {
            float scaledX = (getModuleFeature().getCategoryFrameFeature().getPosition().x + 6) * 1.81818181F;
            float scaledY = (featureHeight + 5) * 1.81818181F;
            float scaledWidth = (getModuleFeature().getCategoryFrameFeature().getPosition().x + getModuleFeature().getCategoryFrameFeature().getWidth() - (FontUtil.getStringWidth(String.valueOf(getModuleFeature().getModule().isDrawn())) * 0.55F) - 3) * 1.81818181F;

            // drawn name
            FontUtil.drawStringWithShadow(getSetting().getName(), scaledX, scaledY, -1);

            // drawn value
            FontUtil.drawStringWithShadow(String.valueOf(getModuleFeature().getModule().isDrawn()), scaledWidth, scaledY, -1);
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
                boolean previousDrawn = getModuleFeature().getModule().isDrawn();

                // update values
                getModuleFeature().getModule().setDrawn(!previousDrawn);
            }

            // play a sound to make the user happy :)
            getCosmos().getSoundManager().playSound("click");
        }
    }
}
