package cope.cosmos.client.ui.clickgui.screens.configuration.component.components.setting;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.client.SettingUpdateEvent;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.ui.clickgui.screens.configuration.component.ClickType;
import cope.cosmos.client.ui.clickgui.screens.configuration.component.components.module.ModuleComponent;
import cope.cosmos.client.ui.util.animation.Animation;
import cope.cosmos.client.ui.util.ColorHSB;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.string.ColorUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author linustouchtips
 * @since 02/03/2022
 */
public class ColorComponent extends SettingComponent<Color> {

    // feature offset
    private float featureHeight;

    // open/close state
    private boolean open;

    // selected color in the color picker
    private ColorHSB selectedColor;

    // animation
    private final Animation animation = new Animation(300, false);
    private int hoverAnimation;

    public ColorComponent(ModuleComponent moduleComponent, Setting<Color> setting) {
        super(moduleComponent, setting);
        Cosmos.EVENT_BUS.register(this);

        // initial value
        selectedColor = new ColorHSB(setting.getValue());
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

        // color
        RenderUtil.drawRoundedRect(getModuleComponent().getCategoryFrameComponent().getPosition().x + getModuleComponent().getCategoryFrameComponent().getWidth() - 12, featureHeight + 2, 10, 10, 2, getSetting().getValue());

        // setting name
        glScaled(0.55, 0.55, 0.55); {
            float scaledX = (getModuleComponent().getCategoryFrameComponent().getPosition().x + 6) * 1.81818181F;
            float scaledY = (featureHeight + 5) * 1.81818181F;
            FontUtil.drawStringWithShadow(getSetting().getName(), scaledX, scaledY, -1);
        }

        glScaled(1.81818181, 1.81818181, 1.81818181);

        if (animation.getAnimationFactor() > 0) {

            // center of the picker
            Vec2f circleCenter = new Vec2f(getModuleComponent().getCategoryFrameComponent().getPosition().x + ((getModuleComponent().getCategoryFrameComponent().getWidth() - 34) / 2F) + 4, featureHeight + HEIGHT + 34);

            if (getMouse().isLeftHeld()) {
                if (isWithinCircle(circleCenter.x, circleCenter.y, 32)) {

                    // rectangular coordinates
                    float xDistance = getMouse().getPosition().x - circleCenter.x;
                    float yDistance = getMouse().getPosition().y - circleCenter.y;

                    // convert to polar coordinates
                    double radius = Math.hypot(xDistance, yDistance);
                    double angle = -Math.toDegrees(Math.atan2(yDistance, xDistance) + (Math.PI / 2)) % 360;

                    // holds hsb
                    selectedColor.setHue(angle / 360F);
                    selectedColor.setSaturation(radius / 32F);
                }

                if (isWithinRect(getModuleComponent().getCategoryFrameComponent().getPosition().x + getModuleComponent().getCategoryFrameComponent().getWidth() - 24, featureHeight + HEIGHT + 4, 62)) {
                    selectedColor.setBrightness(1 - ((getMouse().getPosition().y - (featureHeight + HEIGHT + 4)) / 64));
                }

                if (isWithinRect(getModuleComponent().getCategoryFrameComponent().getPosition().x + getModuleComponent().getCategoryFrameComponent().getWidth() - 10, featureHeight + HEIGHT + 4, 62)) {
                    selectedColor.setTransparency(1 - ((getMouse().getPosition().y - (featureHeight + HEIGHT + 4)) / 64));
                }
            }

            // convert hsb values to rgb
            int color = Color.HSBtoRGB((float) selectedColor.getHue(), (float) selectedColor.getSaturation(), (float) selectedColor.getBrightness());

            // update color value
            getSetting().setValue(new Color((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F, MathHelper.clamp((float) selectedColor.getTransparency(), 0, 1)));

            // color picker
            mc.getTextureManager().bindTexture(new ResourceLocation("cosmos", "textures/imgs/picker.png"));
            Gui.drawModalRectWithCustomSizedTexture((int) getModuleComponent().getCategoryFrameComponent().getPosition().x + 4, (int) featureHeight + HEIGHT + 2, 0, 0, getModuleComponent().getCategoryFrameComponent().getWidth() - 34, 64, getModuleComponent().getCategoryFrameComponent().getWidth() - 34, 64);
            RenderUtil.drawPolygon((float) (circleCenter.x + ((selectedColor.getSaturation() * 32) * Math.cos(Math.toRadians(selectedColor.getHue() * 360) + (Math.PI / 2)))), (float) (circleCenter.y - ((selectedColor.getSaturation() * 32) * Math.sin(Math.toRadians(selectedColor.getHue() * 360) + (Math.PI / 2)))), 1.5F, 360, Color.WHITE);

            // brightness slider
            drawGradientRoundedRect(getModuleComponent().getCategoryFrameComponent().getPosition().x + getModuleComponent().getCategoryFrameComponent().getWidth() - 24, featureHeight + HEIGHT + 2, 3, 62, 2, false);
            RenderUtil.drawPolygon(getModuleComponent().getCategoryFrameComponent().getPosition().x + getModuleComponent().getCategoryFrameComponent().getWidth() - 22.5, featureHeight + HEIGHT + (64 * (1 - selectedColor.getBrightness())) + 2, 2, 360, Color.WHITE);

            // transparency slider
            drawGradientRoundedRect(getModuleComponent().getCategoryFrameComponent().getPosition().x + getModuleComponent().getCategoryFrameComponent().getWidth() - 10, featureHeight + HEIGHT + 2, 3, 62, 2, true);
            RenderUtil.drawPolygon(getModuleComponent().getCategoryFrameComponent().getPosition().x + getModuleComponent().getCategoryFrameComponent().getWidth() - 8.5, featureHeight + HEIGHT + (64 * (1 - selectedColor.getTransparency())) + 2, 2, 360, Color.WHITE);

            // HAHAHAHAHAHAHAHAHAHAHAHAHAHAHAHAHAHAHAHAHAHAHAHAHAHAHAHAHAHAHAHAHAHAHAHAHAHAHA :|
            getModuleComponent().addSettingComponentOffset((float) ((HEIGHT * 4.857) * animation.getAnimationFactor()));
            getModuleComponent().getCategoryFrameComponent().addComponentOffset((HEIGHT * 4.857) * animation.getAnimationFactor());
        }
    }

    @Override
    public void onClick(ClickType in) {
        // open the color picker if clicked
        if (in.equals(ClickType.RIGHT) && isMouseOver(getModuleComponent().getCategoryFrameComponent().getPosition().x, featureHeight, getModuleComponent().getCategoryFrameComponent().getWidth(), HEIGHT)) {

            // module feature bounds
            float highestPoint = featureHeight;
            float lowestPoint = highestPoint + HEIGHT;

            // check if it's able to be interacted with
            if (highestPoint >= getModuleComponent().getCategoryFrameComponent().getPosition().y + getModuleComponent().getCategoryFrameComponent().getTitle() + 2 && lowestPoint <= getModuleComponent().getCategoryFrameComponent().getPosition().y + getModuleComponent().getCategoryFrameComponent().getTitle() + getModuleComponent().getCategoryFrameComponent().getHeight() + 2) {
                open = !open;
                animation.setState(open);
            }

            // play a sound to make the user happy :)
            getCosmos().getSoundManager().playSound("click");
        }
    }

    @SubscribeEvent
    public void onSettingUpdate(SettingUpdateEvent event) {
        if (event.getSetting().equals(getSetting())) {

            // sync value
            selectedColor = new ColorHSB((Color) event.getSetting().getValue());
        }
    }

    /**
     * Draws a curved rectangle with a gradient color
     * @param x The lower x
     * @param y The lower y
     * @param width The upper x
     * @param height The upper y
     * @param radius The radius of the rounded edges
     * @param transparency Whether to allow transparency in colors
     */
    public void drawGradientRoundedRect(float x, float y, float width, float height, int radius, boolean transparency) {
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
            int color = Color.HSBtoRGB((float) selectedColor.getHue(), (float) selectedColor.getSaturation(), 1);
            float red = (color >> 16 & 255) / 255F;
            float green = (color >> 8 & 255) / 255F;
            float blue = (color & 255) / 255F;

            glColor4f(red, green, blue, 1);
            for (i = 0; i <= 90; i++) {
                glVertex2d(x + radius + Math.sin(i * Math.PI / 180.0D) * radius * -1.0D, y + radius + Math.cos(i * Math.PI / 180.0D) * radius * -1.0D);
            }

            if (transparency) {
                glColor4f(1, 1, 1, 1);
            } else {
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

    /**
     * Checks whether a point is within a given circle
     * @param x The center point (x)
     * @param y The center point (y)
     * @param radius The radius of the circle
     * @return Whether the point is within the given circle
     */
    public boolean isWithinCircle(double x, double y, double radius) {
        return Math.sqrt(StrictMath.pow(getMouse().getPosition().x - x, 2) + StrictMath.pow(getMouse().getPosition().y - y, 2)) <= radius;
    }

    /**
     * Checks whether a point is within a given rectangle
     * @param x The lower x
     * @param y THe lower y
     * @param height The upper y
     * @return Whether the point is within the given rectangle
     */
    public boolean isWithinRect(float x, float y, float height) {
        return getMouse().getPosition().x > (x - 2) && getMouse().getPosition().y > y && getMouse().getPosition().x < (x + 6) && getMouse().getPosition().y < (y + height);
    }
}
