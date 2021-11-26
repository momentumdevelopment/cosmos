package cope.cosmos.client.clickgui.windowed.window.windows.configuration.types;

import cope.cosmos.client.clickgui.windowed.window.windows.configuration.SettingComponent;
import cope.cosmos.client.clickgui.windowed.window.windows.configuration.TypeComponent;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.Wrapper;
import cope.cosmos.util.render.RenderUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class ColorComponent extends TypeComponent<Color> implements Wrapper {

    private Vec2f selectorPosition;
    private float brightnessPosition;
    private float transparencyPosition;

    private final ColorHolder selectedColor;

    public ColorComponent(SettingComponent settingComponent, Setting<Color> setting) {
        super(settingComponent, setting);

        // convert the color to HSB
        float[] hsbColor = Color.RGBtoHSB(setting.getValue().getRed(), setting.getValue().getGreen(), setting.getValue().getBlue(), null);

        // set the selected color to the initial setting color
        selectedColor = new ColorHolder(hsbColor[0], hsbColor[1], hsbColor[2], setting.getValue().getAlpha() / 255F);
    }

    @Override
    public void drawType(Vec2f position, float width, float height, float boundHeight) {
        setPosition(position);
        setWidth(width);
        setHeight(height);
        setBoundHeight(boundHeight);

        Vec2f centerPosition = new Vec2f(getPosition().x + 35, getPosition().y + (boundHeight / 2) + 34);

        if (mouseOver(getPosition().x + 6, getPosition().y + (boundHeight / 2) + 2, 64, 64)) {
            if (getGUI().getMouse().isLeftHeld()) {
                // set the selected color as the radius and angle
                if (isWithinCircle(centerPosition.x, centerPosition.y, 32, getGUI().getMouse().getMousePosition().x, getGUI().getMouse().getMousePosition().y)) {
                    setSelectorPosition(new Vec2f(getGUI().getMouse().getMousePosition().x, getGUI().getMouse().getMousePosition().y));

                    // rectangular coords
                    float xDistance = selectorPosition.x - centerPosition.x;
                    float yDistance = selectorPosition.y - centerPosition.y;

                    // convert to polar coords
                    double radius = Math.hypot(xDistance, yDistance);
                    double angle = -Math.toDegrees(Math.atan2(yDistance, xDistance) + (Math.PI / 2)) % 360;

                    selectedColor.setHue((float) (angle / 360));
                    selectedColor.setSaturation((float) (radius / 32));
                }
            }
        }

        if (mouseOver(getPosition().x + 80, getPosition().y + (boundHeight / 2) + 4, 3, 62) && getGUI().getMouse().isLeftHeld()) {
            if (isWithinRect(getPosition().x + 80, getPosition().y + (boundHeight / 2) + 2, 64, getGUI().getMouse().getMousePosition().x, getGUI().getMouse().getMousePosition().y)) {
                setBrightnessPosition(getGUI().getMouse().getMousePosition().y);
                selectedColor.setBrightness(1 - ((brightnessPosition - (getPosition().y + (boundHeight / 2) + 4)) / 64));
            }
        }

        if (mouseOver(getPosition().x + 94, getPosition().y + (boundHeight / 2) + 4, 3, 62) && getGUI().getMouse().isLeftHeld()) {
            if (isWithinRect(getPosition().x + 94, getPosition().y + (boundHeight / 2) + 2, 64, getGUI().getMouse().getMousePosition().x, getGUI().getMouse().getMousePosition().y)) {
                setTransparencyPosition(getGUI().getMouse().getMousePosition().y);
                selectedColor.setTransparency(1 - ((transparencyPosition - (getPosition().y + (boundHeight / 2) + 4)) / 64));
            }
        }

        // convert final selected color;
        int color = Color.HSBtoRGB(selectedColor.getHue(), selectedColor.getSaturation(), selectedColor.getBrightness());
        Color convertedColor = new Color((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F, MathHelper.clamp(selectedColor.getTransparency(), 0, 1));

        // update setting value
        if (!getSetting().getValue().equals(convertedColor)) {
            getSetting().setValue(convertedColor);
        }

        // picker
        drawColorPicker((int) getPosition().x + 6, (int) (getPosition().y + (boundHeight / 2) + 2), 64, 64);
        drawSelector((float) (centerPosition.x + ((selectedColor.getSaturation() * 32) * Math.cos(Math.toRadians(selectedColor.getHue() * 360) + (Math.PI / 2)))), (float) (centerPosition.y - ((selectedColor.getSaturation() * 32) * Math.sin(Math.toRadians(selectedColor.getHue() * 360) + (Math.PI / 2)))), 1.5F);

        // brightness slider
        drawSlider(getPosition().x + 80, getPosition().y + (boundHeight / 2) + 4, 3, 62, 2, false);
        drawSelector(getPosition().x + 81.5F, getPosition().y + (boundHeight / 2) + 4 + (64 * (1 - selectedColor.getBrightness())), 2);

        // transparency slider
        drawSlider(getPosition().x + 94, getPosition().y + (boundHeight / 2) + 4, 3, 62, 2, true);
        drawSelector(getPosition().x + 95.5F, getPosition().y + (boundHeight / 2) + 4 + (64 * (1 - selectedColor.getTransparency())), 2);

        RenderUtil.drawRect(position.x + width - 18.5F, position.y + 2.5F, 16, 16, getSetting().getValue());
    }

    @Override
    public void handleLeftClick() {
        if (mouseOver(getPosition().x + 6, getPosition().y + getBoundHeight() + 2, 64, 64)) {
            getCosmos().getSoundManager().playSound("click");
        }

        if (mouseOver(getPosition().x + 80, getPosition().y + (getBoundHeight() / 2) + 4, 3, 62)) {
            getCosmos().getSoundManager().playSound("click");
        }

        if (mouseOver(getPosition().x + 94, getPosition().y + (getBoundHeight() / 2) + 4, 3, 62)) {
            getCosmos().getSoundManager().playSound("click");
        }
    }

    @Override
    public void handleRightClick() {

    }

    @Override
    public void handleKeyPress(char typedCharacter, int key) {

    }

    public void drawColorPicker(int x, int y, float width, float height) {
        mc.getTextureManager().bindTexture(new ResourceLocation("cosmos", "textures/imgs/picker.png"));
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, (int) width, (int) height, width, height);
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
