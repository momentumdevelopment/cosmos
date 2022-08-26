package cope.cosmos.client.ui.clickgui.screens.configuration.component.components.setting;

import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.ui.clickgui.screens.configuration.component.ClickType;
import cope.cosmos.client.ui.clickgui.screens.configuration.component.components.module.ModuleComponent;
import cope.cosmos.util.math.MathUtil;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.string.ColorUtil;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

import static org.lwjgl.opengl.GL11.glScaled;

/**
 * @author linustouchtips, Surge
 * @since 02/02/2022
 */
public class NumberComponent extends SettingComponent<Number> {

    // feature offset
    private float featureHeight;

    // animation
    private int hoverAnimation;

    public NumberComponent(ModuleComponent moduleComponent, Setting<Number> setting) {
        super(moduleComponent, setting);

        // slider feature height is 20
        HEIGHT = 20;
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

        glScaled(0.55, 0.55, 0.55); {
            float scaledX = (getModuleComponent().getCategoryFrameComponent().getPosition().x + 6) * 1.81818181F;
            float scaledY = (featureHeight + 5) * 1.81818181F;
            float scaledWidth = (getModuleComponent().getCategoryFrameComponent().getPosition().x + getModuleComponent().getCategoryFrameComponent().getWidth() - (FontUtil.getStringWidth(String.valueOf(getSetting().getValue())) * 0.55F) - 3) * 1.81818181F;

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
        if (highestPoint >= getModuleComponent().getCategoryFrameComponent().getPosition().y + getModuleComponent().getCategoryFrameComponent().getTitle() + 2 && lowestPoint <= getModuleComponent().getCategoryFrameComponent().getPosition().y + getModuleComponent().getCategoryFrameComponent().getTitle() + getModuleComponent().getCategoryFrameComponent().getHeight() + 2) {
            if (getMouse().isLeftHeld()) {
                if (isMouseOver(getModuleComponent().getCategoryFrameComponent().getPosition().x, featureHeight + 13, getModuleComponent().getCategoryFrameComponent().getWidth(), HEIGHT - 13)) {
                    // the percentage of the slider that is filled
                    float percentFilled = ((getMouse().getPosition().x - getModuleComponent().getCategoryFrameComponent().getPosition().x) * 130 / ((getModuleComponent().getCategoryFrameComponent().getPosition().x + (getModuleComponent().getCategoryFrameComponent().getWidth() - 6)) - getModuleComponent().getCategoryFrameComponent().getPosition().x));

                    Number max = getSetting().getMax();
                    Number min = getSetting().getMin();

                    // set the value based on the type
                    if (getSetting().getValue() instanceof Double) {
                        double valueSlid = MathHelper.clamp(MathUtil.roundDouble(percentFilled * ((max.doubleValue() - min.doubleValue()) / 130.0D) + min.doubleValue(), getSetting().getRoundingScale()), min.doubleValue(), max.doubleValue());

                        // exclude number
                        if (getSetting().isExclusion(valueSlid)) {
                            getSetting().setValue(valueSlid + Math.pow(1, -getSetting().getRoundingScale()));
                        }

                        else {
                            getSetting().setValue(valueSlid);
                        }
                    }

                    else if (getSetting().getValue() instanceof Float) {
                        float valueSlid = MathHelper.clamp(MathUtil.roundFloat(percentFilled * (float) ((max.floatValue() - min.floatValue()) / 130.0D) + min.floatValue(), getSetting().getRoundingScale()), min.floatValue(), max.floatValue());

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
                else if (isMouseOver(getModuleComponent().getCategoryFrameComponent().getPosition().x, featureHeight + 13, 5, HEIGHT - 13)) {
                    getSetting().setValue(getSetting().getMin());
                }

                // if greater than max, setting is max
                else if (isMouseOver(getModuleComponent().getCategoryFrameComponent().getPosition().x + (getModuleComponent().getCategoryFrameComponent().getWidth() - 5), featureHeight + 13, 5, HEIGHT - 13)) {
                    getSetting().setValue(getSetting().getMax());
                }
            }
        }

        // slider length
        float sliderWidth = 91 * (getSetting().getValue().floatValue() - getSetting().getMin().floatValue()) / (getSetting().getMax().floatValue() - getSetting().getMin().floatValue());

        // clamp
        if (sliderWidth < 2) {
            sliderWidth = 2;
        }

        if (sliderWidth > 91) {
            sliderWidth = 91;
        }

        // slider
        RenderUtil.drawRoundedRect(getModuleComponent().getCategoryFrameComponent().getPosition().x + 6, featureHeight + 14, getModuleComponent().getCategoryFrameComponent().getWidth() - 10, 3, 2, new Color(23 + hoverAnimation, 23 + hoverAnimation, 29 + hoverAnimation, 255));

        if (getSetting().getValue().doubleValue() > getSetting().getMin().doubleValue()) {
            RenderUtil.drawRoundedRect(getModuleComponent().getCategoryFrameComponent().getPosition().x + 6, featureHeight + 14, sliderWidth, 3, 2, ColorUtil.getPrimaryAlphaColor(255));
        }

        // RenderUtil.drawPolygon(getModuleComponent().getCategoryFrameComponent().getPosition().x + 4 + sliderWidth, featureHeight + 15.5, 2, 360, ColorUtil.getPrimaryColor());
    }

    @Override
    public void onClick(ClickType in) {
        if (in.equals(ClickType.LEFT) && isMouseOver(getModuleComponent().getCategoryFrameComponent().getPosition().x, featureHeight, getModuleComponent().getCategoryFrameComponent().getWidth(), HEIGHT - 13)) {
            // play a sound to make the user happy :)
            getCosmos().getSoundManager().playSound("click");
        }
    }
}
