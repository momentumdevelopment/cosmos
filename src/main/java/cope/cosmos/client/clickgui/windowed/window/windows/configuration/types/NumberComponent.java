package cope.cosmos.client.clickgui.windowed.window.windows.configuration.types;

import cope.cosmos.client.clickgui.windowed.window.windows.configuration.SettingComponent;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.client.ColorUtil;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.system.MathUtil;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

import java.awt.*;

public class NumberComponent extends TypeComponent<Number> {

    private float lastDraggingMouseX = 0;

    public NumberComponent(SettingComponent settingComponent, Setting<Number> setting) {
        super(settingComponent, setting);
    }

    @Override
    public void drawType(Vec2f position, float width, float height, float boundHeight) {
        setPosition(position);
        setWidth(width);
        setHeight(height);
        setBoundHeight(boundHeight);

        String value = getSetting().getValue().toString();
        FontUtil.drawStringWithShadow(value, position.x + width - FontUtil.getStringWidth(value) - 2, position.y + 3, -1);

        // NOTE: Check if not expanding window
        if (mouseOver(getPosition().x, getPosition().y + 20, width, 4)) {
            // make sure user is holding the left mouse
            if (getGUI().getMouse().isLeftHeld()) {
                lastDraggingMouseX = getGUI().getMouse().getMousePosition().x;

                // the percentage of the slider that is filled
                float percentFilled = ((getGUI().getMouse().getMousePosition().x - getPosition().x) * 130 / ((getPosition().x + (width - 6)) - getPosition().x));

                Number max = getSetting().getMax();
                Number min = getSetting().getMin();

                // set the value based on the type
                if (getSetting().getValue() instanceof Double) {
                    getSetting().setValue(MathUtil.roundDouble(percentFilled * ((max.doubleValue() - min.doubleValue()) / 130.0D) + min.doubleValue(), getSetting().getRoundingScale()));
                }

                else if (getSetting().getValue() instanceof Float) {
                    getSetting().setValue(MathUtil.roundFloat(percentFilled * (float) ((max.floatValue() - min.floatValue()) / 130.0D) + min.floatValue(), getSetting().getRoundingScale()));
                }
            }
        }

        float pixAdd = lastDraggingMouseX == 0 ? (((getPosition().x + (width - 6)) - getPosition().x) * (getSetting().getValue().floatValue() - getSetting().getMin().floatValue()) / (getSetting().getMax().floatValue() - getSetting().getMin().floatValue())) : (lastDraggingMouseX - getPosition().x);

        // if less than min, setting is min
        if (mouseOver(getPosition().x, getPosition().y + 20, 5, 4)) {
            if (getGUI().getMouse().isLeftHeld()) {
                getSetting().setValue(getSetting().getMin());
            }
        }

        // if greater than max, setting is max
        else if (mouseOver(getPosition().x + (width - 6), getPosition().y + 20, 5, 4)) {
            if (getGUI().getMouse().isLeftHeld()) {
                getSetting().setValue(getSetting().getMax());
            }
        }

        // if the setting value is max or min, the slider pixels don't need any added space
        if (getSetting().getValue().equals(getSetting().getMin())) {
            pixAdd = 3;
        }

        if (getSetting().getValue().equals(getSetting().getMax())) {
            pixAdd = width - 6;
        }

        // slider
        RenderUtil.drawRoundedRect(position.x + 4, position.y + 21, pixAdd - 2, 3, 2, new Color(MathHelper.clamp(ColorUtil.getPrimaryColor().getRed() - 10, 0, 255), MathHelper.clamp(ColorUtil.getPrimaryColor().getGreen() - 10, 0, 255), MathHelper.clamp(ColorUtil.getPrimaryColor().getBlue() - 10, 0, 255)));
        RenderUtil.drawRoundedRect(position.x + 4 + pixAdd, position.y + 21, width - 8 - pixAdd, 3, 2, new Color(0, 0, 0, 70));
        RenderUtil.drawPolygon(position.x + 2 + pixAdd, position.y + 22.5, 2, 360, ColorUtil.getPrimaryColor());
    }

    @Override
    public void handleLeftClick() {

    }

    @Override
    public void handleRightClick() {

    }

    @Override
    public void handleKeyPress(char typedCharacter, int key) {

    }
}
