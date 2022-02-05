package cope.cosmos.client.ui.clickgui.feature.features.setting;

import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.ui.clickgui.feature.ClickType;
import cope.cosmos.client.ui.clickgui.feature.features.module.ModuleFeature;
import cope.cosmos.util.math.MathUtil;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.string.ColorUtil;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

import static org.lwjgl.opengl.GL11.glScaled;

/**
 * @author linustouchtips
 * @since 02/02/2022
 */
public class NumberFeature extends SettingFeature<Number> {

    // feature offset
    private float featureHeight;

    // animation
    private int hoverAnimation;

    public NumberFeature(ModuleFeature moduleFeature, Setting<Number> setting) {
        super(moduleFeature, setting);

        // slider feature height is 20
        HEIGHT = 20;
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
            float scaledWidth = (getModuleFeature().getCategoryFrameFeature().getPosition().x + getModuleFeature().getCategoryFrameFeature().getWidth() - (FontUtil.getStringWidth(String.valueOf(getSetting().getValue())) * 0.55F) - 3) * 1.81818181F;

            // drawn name
            FontUtil.drawStringWithShadow(getSetting().getName(), scaledX, scaledY, -1);

            // drawn value
            FontUtil.drawStringWithShadow(String.valueOf(getSetting().getValue()), scaledWidth, scaledY, -1);
        }

        glScaled(1.81818181, 1.81818181, 1.81818181);

        // module feature bounds
        float highestPoint = featureHeight;
        float lowestPoint = highestPoint + HEIGHT;

        // check if it's able to be interacted with
        if (highestPoint >= getModuleFeature().getCategoryFrameFeature().getPosition().y + getModuleFeature().getCategoryFrameFeature().getTitle() + 2 && lowestPoint <= getModuleFeature().getCategoryFrameFeature().getPosition().y + getModuleFeature().getCategoryFrameFeature().getTitle() + getModuleFeature().getCategoryFrameFeature().getHeight() + 2) {
            if (getMouse().isLeftHeld()) {
                if (isMouseOver(getModuleFeature().getCategoryFrameFeature().getPosition().x, featureHeight + 13, getModuleFeature().getCategoryFrameFeature().getWidth(), HEIGHT - 13)) {
                    // the percentage of the slider that is filled
                    float percentFilled = ((getMouse().getPosition().x - getModuleFeature().getCategoryFrameFeature().getPosition().x) * 130 / ((getModuleFeature().getCategoryFrameFeature().getPosition().x + (getModuleFeature().getCategoryFrameFeature().getWidth() - 6)) - getModuleFeature().getCategoryFrameFeature().getPosition().x));

                    Number max = getSetting().getMax();
                    Number min = getSetting().getMin();

                    // set the value based on the type
                    if (getSetting().getValue() instanceof Double) {
                        double valueSlid = MathUtil.roundDouble(percentFilled * ((max.doubleValue() - min.doubleValue()) / 130.0D) + min.doubleValue(), getSetting().getRoundingScale());

                        // exclude number
                        if (getSetting().isExclusion(valueSlid)) {
                            getSetting().setValue(valueSlid + Math.pow(1, -getSetting().getRoundingScale()));
                        }

                        else {
                            getSetting().setValue(valueSlid);
                        }
                    }

                    else if (getSetting().getValue() instanceof Float) {
                        float valueSlid = MathUtil.roundFloat(percentFilled * (float) ((max.floatValue() - min.floatValue()) / 130.0D) + min.floatValue(), getSetting().getRoundingScale());

                        // exclude number
                        if (getSetting().isExclusion(valueSlid)) {
                            getSetting().setValue(valueSlid + Math.pow(1, -getSetting().getRoundingScale()));
                        }

                        else {
                            getSetting().setValue(valueSlid);
                        }
                    }
                }

                // if less than min, setting is min
                else if (isMouseOver(getModuleFeature().getCategoryFrameFeature().getPosition().x, featureHeight + 13, 5, HEIGHT - 13)) {
                    getSetting().setValue(getSetting().getMin());
                }

                // if greater than max, setting is max
                else if (isMouseOver(getModuleFeature().getCategoryFrameFeature().getPosition().x + (getModuleFeature().getCategoryFrameFeature().getWidth() - 5), featureHeight + 13, 5, HEIGHT - 13)) {
                    getSetting().setValue(getSetting().getMax());
                }
            }
        }

        // slider length
        float sliderWidth = (getModuleFeature().getCategoryFrameFeature().getWidth() - 10) * (getSetting().getValue().floatValue() / getSetting().getMax().floatValue());

        // clamp
        if (sliderWidth < 2) {
            sliderWidth = 2;
        }

        if (sliderWidth > 91) {
            sliderWidth = 91;
        }

        // slider
        RenderUtil.drawRoundedRect(getModuleFeature().getCategoryFrameFeature().getPosition().x + 6, featureHeight + 14, sliderWidth - 2, 3, 2, new Color(MathHelper.clamp(ColorUtil.getPrimaryColor().getRed() - 10, 0, 255), MathHelper.clamp(ColorUtil.getPrimaryColor().getGreen() - 10, 0, 255), MathHelper.clamp(ColorUtil.getPrimaryColor().getBlue() - 10, 0, 255)));
        RenderUtil.drawRoundedRect(getModuleFeature().getCategoryFrameFeature().getPosition().x + sliderWidth + 6, featureHeight + 14, getModuleFeature().getCategoryFrameFeature().getWidth() - sliderWidth - 10, 3, 2, new Color(23 + hoverAnimation, 23 + hoverAnimation, 29 + hoverAnimation, 255));
        RenderUtil.drawPolygon(getModuleFeature().getCategoryFrameFeature().getPosition().x + 4 + sliderWidth, featureHeight + 15.5, 2, 360, ColorUtil.getPrimaryColor());
    }

    @Override
    public void onClick(ClickType in) {
        if (in.equals(ClickType.LEFT) && isMouseOver(getModuleFeature().getCategoryFrameFeature().getPosition().x, featureHeight, getModuleFeature().getCategoryFrameFeature().getWidth(), HEIGHT - 13)) {
            // play a sound to make the user happy :)
            getCosmos().getSoundManager().playSound("click");
        }
    }
}
