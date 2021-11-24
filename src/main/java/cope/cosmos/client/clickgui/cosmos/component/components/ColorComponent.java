package cope.cosmos.client.clickgui.cosmos.component.components;

import cope.cosmos.client.clickgui.cosmos.component.SettingComponent;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.AnimationManager;
import cope.cosmos.util.Wrapper;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.world.SoundUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

@SuppressWarnings("unused")
public class ColorComponent extends SettingComponent<Color> implements Wrapper {

    // height of the picker
    float PICKER_HEIGHT = 64;

    private int hoverAnimation = 0;

    private Vec2f selectorPosition;
    private float brightnessPosition;
    private float transparencyPosition;

    private final ColorHolder selectedColor;

    private final AnimationManager animationManager = new AnimationManager(200, false);
    private boolean open;

    public ColorComponent(Setting<Color> setting, ModuleComponent moduleComponent) {
        super(setting, moduleComponent);

        // convert the color to HSB
        float[] hsbColor = Color.RGBtoHSB(setting.getValue().getRed(), setting.getValue().getGreen(), setting.getValue().getBlue(), null);

        // set the selected color to the initial setting color
        selectedColor = new ColorHolder(hsbColor[0], hsbColor[1], hsbColor[2], setting.getValue().getAlpha() / 255F);

        // open should be set to false by default
        open = false;
        animationManager.setStateHard(false);
    }

    @Override
    public void drawSettingComponent(Vec2f position) {
        setPosition(position);

        // hover animation
        if (mouseOver(position.x, position.y, WIDTH, HEIGHT) && hoverAnimation < 25)
            hoverAnimation += 5;

        else if (!mouseOver(position.x, position.y, WIDTH, HEIGHT) && hoverAnimation > 0)
            hoverAnimation -= 5;

        // background
        Color settingColor = isSubSetting() ? new Color(12 + hoverAnimation, 12 + hoverAnimation, 17 + hoverAnimation) : new Color(18 + hoverAnimation, 18 + hoverAnimation, 24 + hoverAnimation);
        RenderUtil.drawRect(position.x, position.y, WIDTH, HEIGHT, settingColor);
        RenderUtil.drawRect(position.x, position.y, WIDTH, HEIGHT, settingColor);

        // color window
        RenderUtil.drawRoundedRect(position.x + WIDTH - 12, position.y + 2, 10, 10, 2, getSetting().getValue());

        // setting name
        glScaled(0.55, 0.55, 0.55);{
            float scaledX = (position.x + 4) * 1.81818181F;
            float scaledY = (position.y + 5) * 1.81818181F;
            FontUtil.drawStringWithShadow(getSetting().getName(), scaledX, scaledY, -1);
        }

        glScaled(1.81818181, 1.81818181, 1.81818181);

        float pickerAnimation = (float) MathHelper.clamp(animationManager.getAnimationFactor(), 0, 1);

        // background
        // RenderUtil.drawRect(position.x, position.y + HEIGHT, WIDTH, (PICKER_HEIGHT + 4) * pickerAnimation, new Color(35, 35, 35));

        // picker position
        float PICKER_X = position.x + 2;
        float PICKER_Y = position.y + HEIGHT + 2;

        // center of the picker
        Vec2f centerPosition = new Vec2f(PICKER_X + ((WIDTH - 34) / 2), PICKER_Y + (PICKER_HEIGHT / 2));

        // radius of the picker
        float RADIUS = PICKER_HEIGHT / 2;

        if (pickerAnimation > 0) {
            if (mouseOver(PICKER_X, PICKER_Y, WIDTH - 34, PICKER_HEIGHT) && getGUI().getMouse().isLeftHeld() && !getModuleComponent().getParentWindow().isDragging()) {
                // set the selected color as the radius and angle
                if (isWithinCircle(centerPosition.x, centerPosition.y, RADIUS, getGUI().getMouse().getMousePosition().x, getGUI().getMouse().getMousePosition().y)) {
                    setSelectorPosition(new Vec2f(getGUI().getMouse().getMousePosition().x, getGUI().getMouse().getMousePosition().y));

                    // rectangular coords
                    float xDistance = selectorPosition.x - centerPosition.x;
                    float yDistance = selectorPosition.y - centerPosition.y;

                    // convert to polar coords
                    double radius = Math.hypot(xDistance, yDistance);
                    double angle = -Math.toDegrees(Math.atan2(yDistance, xDistance) + (Math.PI / 2)) % 360;

                    selectedColor.setHue((float) (angle / 360));
                    selectedColor.setSaturation((float) (radius / RADIUS));
                }
            }

            if (mouseOver(PICKER_X + WIDTH - 26, PICKER_Y + 2, 3, PICKER_HEIGHT - 2) && getGUI().getMouse().isLeftHeld() && !getModuleComponent().getParentWindow().isDragging()) {
                if (isWithinRect(PICKER_X + WIDTH - 26, PICKER_Y, PICKER_HEIGHT, getGUI().getMouse().getMousePosition().x, getGUI().getMouse().getMousePosition().y)) {
                    setBrightnessPosition(getGUI().getMouse().getMousePosition().y);
                    selectedColor.setBrightness(1 - ((brightnessPosition - (PICKER_Y + 2)) / PICKER_HEIGHT));
                }
            }

            if (mouseOver(PICKER_X + WIDTH - 12, PICKER_Y + 2, 3, PICKER_HEIGHT - 2) && getGUI().getMouse().isLeftHeld() && !getModuleComponent().getParentWindow().isDragging()) {
                if (isWithinRect(PICKER_X + WIDTH - 12, PICKER_Y, PICKER_HEIGHT, getGUI().getMouse().getMousePosition().x, getGUI().getMouse().getMousePosition().y)) {
                    setTransparencyPosition(getGUI().getMouse().getMousePosition().y);
                    selectedColor.setTransparency(1 - ((transparencyPosition - (PICKER_Y + 2)) / PICKER_HEIGHT));
                }
            }

            // convert final selected color;
            int color = Color.HSBtoRGB(selectedColor.getHue(), selectedColor.getSaturation(), selectedColor.getBrightness());

            // update setting value
            getSetting().setValue(new Color((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F, MathHelper.clamp(selectedColor.getTransparency(), 0, 1)));

            // picker
            drawColorPicker((int) PICKER_X, (int) PICKER_Y);
            drawSelector((float) (centerPosition.x + ((selectedColor.getSaturation() * RADIUS) * Math.cos(Math.toRadians(selectedColor.getHue() * 360) + (Math.PI / 2)))), (float) (centerPosition.y - ((selectedColor.getSaturation() * RADIUS) * Math.sin(Math.toRadians(selectedColor.getHue() * 360) + (Math.PI / 2)))), 1.5F);

            // brightness slider
            drawSlider(PICKER_X + WIDTH - 26, PICKER_Y + 2, 3, PICKER_HEIGHT - 2, 2, false);
            drawSelector(PICKER_X + WIDTH - 24.5F, PICKER_Y + 2 + (PICKER_HEIGHT * (1 - selectedColor.getBrightness())), 2);

            // transparency slider
            drawSlider(PICKER_X + WIDTH - 12, PICKER_Y + 2, 3, PICKER_HEIGHT - 2, 2, true);
            drawSelector(PICKER_X + WIDTH - 10.5F, PICKER_Y + 2 + (PICKER_HEIGHT * (1 - selectedColor.getTransparency())), 2);

            if (isSubSetting())
                getSettingComponent().setSettingOffset(pickerAnimation * 4.857F);

            getModuleComponent().getParentWindow().setModuleOffset(pickerAnimation * 4.857F);
            getModuleComponent().setSettingOffset(pickerAnimation * 4.857F);
        }
    }

    public void drawColorPicker(int x, int y) {
        mc.getTextureManager().bindTexture(new ResourceLocation("cosmos", "textures/imgs/picker.png"));
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, (int) (WIDTH - 34), (int) PICKER_HEIGHT, WIDTH - 34, PICKER_HEIGHT);
    }

    public void drawSlider(float x, float y, float width, float height, int radius, boolean transparency) {
        glPushAttrib(GL_POINTS);

        glScaled(0.5, 0.5, 0.5); {
            x *= 2;
            y *= 2;
            width *= 2;
            height *= 2;

            width += x;
            height += y;

            glEnable(GL_BLEND);
            glDisable(GL_TEXTURE_2D);
            glEnable(GL_LINE_SMOOTH);
            glBegin(GL_POLYGON);

            int i;

            // convert to rgb
            int color = Color.HSBtoRGB(selectedColor.getHue(), selectedColor.getSaturation(), 1);
            float red = (color >> 16 & 255) / 255F;
            float green = (color >> 8 & 255) / 255F;
            float blue = (color & 255) / 255F;

            glColor4f(red, green, blue, 1);
            for (i = 0; i <= 90; i++) {
                glVertex2d(x + radius + Math.sin(i * Math.PI / 180.0D) * radius * -1.0D, y + radius + Math.cos(i * Math.PI / 180.0D) * radius * -1.0D);
            }

            if (transparency) {
                glColor4f(1, 1, 1, 1);
            }

            else {
                glColor4f(0, 0, 0, 1);
            }

            for (i = 90; i <= 180; i++) {
                glVertex2d(x + radius + Math.sin(i * Math.PI / 180.0D) * radius * -1.0D, height - radius + Math.cos(i * Math.PI / 180.0D) * radius * -1.0D);
            }

            if (transparency) {
                glColor4f(1, 1, 1, 1);
            }

            else {
                glColor4f(0, 0, 0, 1);
            }

            for (i = 0; i <= 90; i++) {
                glVertex2d(width - radius + Math.sin(i * Math.PI / 180.0D) * radius, height - radius + Math.cos(i * Math.PI / 180.0D) * radius);
            }

            glColor4f(red, green, blue, 1);
            for (i = 90; i <= 180; i++) {
                glVertex2d(width - radius + Math.sin(i * Math.PI / 180.0D) * radius, y + radius + Math.cos(i * Math.PI / 180.0D) * radius);
            }

            glEnd();
            glEnable(GL_TEXTURE_2D);
            glDisable(GL_BLEND);
            glDisable(GL_LINE_SMOOTH);
            glDisable(GL_BLEND);
            glEnable(GL_TEXTURE_2D);
        }

        glScaled(2, 2, 2);
        glPopAttrib();
    }

    public void drawSelector(float x, float y, float radius) {
        RenderUtil.drawPolygon(x, y, radius, 360, Color.WHITE);
    }

    private boolean isWithinCircle(double x, double y, double radius, float mouseX, float mouseY) {
        return Math.sqrt(Math.pow(mouseX - x, 2) + Math.pow(mouseY - y, 2)) <= radius;
    }

    private boolean isWithinRect(float x, float y, float height, float mouseX, float mouseY) {
        return mouseX > x && mouseY > y && mouseX < (x + 4) && mouseY < (y + height);
    }

    public Color alphaIntegrate(Color color, float alpha) {
        float red = (float) color.getRed() / 255;
        float green = (float) color.getGreen() / 255;
        float blue = (float) color.getBlue() / 255;
        return new Color(red, green, blue, alpha);
    }

    @Override
    public void handleLeftClick(int mouseX, int mouseY) {
        if (mouseOver(getPosition().x, getPosition().y, WIDTH, PICKER_HEIGHT))
            SoundUtil.clickSound();
    }

    @Override
    public void handleLeftDrag(int mouseX, int mouseY) {

    }

    @Override
    public void handleRightClick(int mouseX, int mouseY) {
        if (mouseOver(getPosition().x, getPosition().y, WIDTH, HEIGHT)) {
            SoundUtil.clickSound();

            open = !open;
            animationManager.setState(open);
        }
    }

    @Override
    public void handleKeyPress(char typedCharacter, int key) {

    }

    @Override
    public void handleScroll(int scroll) {

    }

    public void setSelectorPosition(Vec2f in) {
        selectorPosition = in;
    }

    public void setBrightnessPosition(float in) {
        brightnessPosition = in;
    }

    public void setTransparencyPosition(float in) {
        transparencyPosition = in;
    }

    public static class ColorHolder {

        private float hue, saturation, brightness, transparency;

        public ColorHolder(float hue, float saturation, float brightness, float transparency) {
            this.hue = hue;
            this.saturation = saturation;
            this.brightness = brightness;
            this.transparency = transparency;
        }

        public float getHue() {
            return hue;
        }

        public void setHue(float in) {
            hue = in;
        }

        public float getSaturation() {
            return saturation;
        }

        public void setSaturation(float in) {
            saturation = in;
        }

        public float getBrightness() {
            return brightness;
        }

        public void setBrightness(float in) {
            brightness = in;
        }

        public float getTransparency() {
            return transparency;
        }

        public void setTransparency(float in) {
            transparency = in;
        }
    }
}