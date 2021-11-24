package cope.cosmos.client.clickgui.cosmos.component.components;

import cope.cosmos.client.clickgui.cosmos.component.SettingComponent;
import cope.cosmos.client.features.modules.client.ClickGUI;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.system.MathUtil;
import cope.cosmos.util.system.Timer;
import cope.cosmos.util.system.Timer.Format;
import cope.cosmos.util.world.SoundUtil;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.math.Vec2f;
import org.lwjgl.input.Keyboard;

import java.awt.*;

import static org.lwjgl.opengl.GL11.glScaled;

@SuppressWarnings("unused")
public class NumberComponent<T extends Number> extends SettingComponent<Number> {

    private final float HEIGHT = 20;
    private final float SLIDER_HEIGHT = 6;

    private int hoverAnimation = 0;

    private String typedValue;
    private final Timer insertionTimer = new Timer();
    private boolean insertion;
    private boolean typing;

    private float pixAdd;
    private float lastDraggingMouseX = 0;

    public NumberComponent(Setting<Number> setting, ModuleComponent moduleComponent) {
        super(setting, moduleComponent);

        // typing should be reset when gui is opened
        typing = false;
        insertion = false;

        // initialize the typed value as the setting value
        typedValue = String.valueOf(setting.getValue());
    }

    @Override
    public void drawSettingComponent(Vec2f position) {
        setPosition(position);

        // set the typed value to the slider value
        typedValue = getSetting().getValue() + getInsertionPoint();

        // hover animation
        if (mouseOver(position.x, position.y, WIDTH, HEIGHT) && hoverAnimation < 25)
            hoverAnimation += 5;

        else if (!mouseOver(position.x, position.y, WIDTH, HEIGHT) && hoverAnimation > 0)
            hoverAnimation -= 5;

        // background
        Color settingColor = isSubSetting() ? new Color(ClickGUI.INSTANCE.getSecondaryColor().getRed() + hoverAnimation, ClickGUI.INSTANCE.getSecondaryColor().getGreen() + hoverAnimation, ClickGUI.INSTANCE.getSecondaryColor().getBlue() + hoverAnimation) : new Color(ClickGUI.INSTANCE.getComplexionColor().getRed() + hoverAnimation, ClickGUI.INSTANCE.getComplexionColor().getGreen() + hoverAnimation, ClickGUI.INSTANCE.getComplexionColor().getBlue() + hoverAnimation);
        RenderUtil.drawRect(position.x, position.y, WIDTH, HEIGHT, settingColor);
        RenderUtil.drawRect(position.x, position.y, WIDTH, HEIGHT, settingColor);

        // setting name & value
        glScaled(0.55, 0.55, 0.55); {
            float scaledX = (position.x + 4) * 1.81818181F;
            float scaledWidth = (position.x + WIDTH - (FontUtil.getStringWidth(typedValue) * 0.55F) - 3) * 1.81818181F;
            float scaledY = (position.y + 5) * 1.81818181F;

            FontUtil.drawStringWithShadow(getSetting().getName(), scaledX, scaledY, -1);
            FontUtil.drawStringWithShadow(typedValue, scaledWidth, scaledY, -1);
        }

        glScaled(1.81818181, 1.81818181, 1.81818181);

        // slider
        RenderUtil.drawRoundedRect(position.x + 4, position.y + 14, pixAdd - 2, 3, 2, new Color(ClickGUI.INSTANCE.getPrimaryColor().getRed(), ClickGUI.INSTANCE.getPrimaryColor().getGreen(), ClickGUI.INSTANCE.getPrimaryColor().getBlue()));
        RenderUtil.drawPolygon(position.x + 2 + pixAdd, position.y + 15.5, 2, 360, new Color(ClickGUI.INSTANCE.getPrimaryColor().getRed(), ClickGUI.INSTANCE.getPrimaryColor().getGreen(), ClickGUI.INSTANCE.getPrimaryColor().getBlue()));
    }

    @Override
    public void handleLeftClick(int mouseX, int mouseY) {
        if (mouseOver(getPosition().x, getPosition().y + SLIDER_HEIGHT, WIDTH, HEIGHT - SLIDER_HEIGHT))
            SoundUtil.clickSound();
    }

    @Override
    public void handleLeftDrag(int mouseX, int mouseY) {
        if (mouseOver(getPosition().x, getPosition().y + SLIDER_HEIGHT, WIDTH, HEIGHT - SLIDER_HEIGHT) && !getModuleComponent().getParentWindow().isDragging()) {
            // make sure user is holding the left mouse
            if (getGUI().getMouse().isLeftHeld()) {
                lastDraggingMouseX = mouseX;

                // the percentage of the slider that is filled
                float percentFilled = ((mouseX - getPosition().x) * 130 / ((getPosition().x + (WIDTH - 6)) - getPosition().x));

                Number max = getSetting().getMax();
                Number min = getSetting().getMin();

                // set the value based on the type
                if (getSetting().getValue() instanceof Double)
                    getSetting().setValue(MathUtil.roundDouble(percentFilled * ((max.doubleValue() - min.doubleValue()) / 130.0D) + min.doubleValue(), getSetting().getRoundingScale()));

                else if (getSetting().getValue() instanceof Float)
                    getSetting().setValue(MathUtil.roundFloat(percentFilled * (float)((max.floatValue() - min.floatValue()) / 130.0D) + min.floatValue(), getSetting().getRoundingScale()));
            }
        }

        pixAdd = lastDraggingMouseX == 0 ? (((getPosition().x + (WIDTH - 6)) - getPosition().x) * (getSetting().getValue().floatValue() - getSetting().getMin().floatValue()) / (getSetting().getMax().floatValue() - getSetting().getMin().floatValue())) : (lastDraggingMouseX - getPosition().x);

        // if less than min, setting is min
        if (mouseOver(getPosition().x, getPosition().y + SLIDER_HEIGHT, 5, HEIGHT - SLIDER_HEIGHT)) {
            if (getGUI().getMouse().isLeftHeld())
                getSetting().setValue(getSetting().getMin());
        }

        // if greater than max, setting is max
        else if (mouseOver(getPosition().x + (WIDTH - 6), getPosition().y + SLIDER_HEIGHT, 5, HEIGHT - SLIDER_HEIGHT)) {
            if (getGUI().getMouse().isLeftHeld())
                getSetting().setValue(getSetting().getMax());
        }

        if (getSetting().getValue().equals(getSetting().getMin()))
            pixAdd = 3;

        if (getSetting().getValue().equals(getSetting().getMax()))
            pixAdd = WIDTH - 6;
    }

    @Override
    public void handleRightClick(int mouseX, int mouseY) {
        if (mouseOver(getPosition().x + (WIDTH - 6), getPosition().y, 5, SLIDER_HEIGHT)) {
            typing = !typing;
            insertionTimer.reset();
        }
    }

    @Override
    public void handleKeyPress(char typedCharacter, int key) {
        if (typing) {
            if (key == Keyboard.KEY_RETURN) {
                try {
                    // set the setting value to the typed value

                    if (getSetting().getValue() instanceof Double) {
                        double typedVal = Double.parseDouble(typedValue);

                        if (typedVal > (double) getSetting().getMax()) {
                            getSetting().setValue(getSetting().getMax());
                            return;
                        }

                        if (typedVal < (double) getSetting().getMin()) {
                            getSetting().setValue(getSetting().getMin());
                            return;
                        }

                        getSetting().setValue(typedVal);
                    }

                    else if (getSetting().getValue() instanceof Float) {
                        float typedVal = Float.parseFloat(typedValue);

                        if (typedVal > (float) getSetting().getMax()) {
                            getSetting().setValue(getSetting().getMax());
                            return;
                        }

                        if (typedVal < (float) getSetting().getMin()) {
                            getSetting().setValue(getSetting().getMin());
                            return;
                        }

                        getSetting().setValue(typedVal);
                    }

                    // set the value, no longer typing
                    typing = false;
                } catch (NumberFormatException ignored) {

                }
            }

            else {
                // store the typed text in a value
                String typedText = "";

                if (ChatAllowedCharacters.isAllowedCharacter(typedCharacter))
                    typedText = typedCharacter + "";

                else {
                    if (key == Keyboard.KEY_BACK) {
                        if (typedValue.length() >= 1)
                            typedValue = typedValue.substring(0, typedValue.length() -1);
                    }
                }

                typedValue += typedText;
            }
        }
    }

    @Override
    public void handleScroll(int scroll) {

    }

    public String getInsertionPoint() {
        if (insertionTimer.passed(500, Format.SYSTEM)) {
            insertionTimer.reset();
            insertion = !insertion;
        }

        return insertion && typing ? "｜" : "";
    }
}