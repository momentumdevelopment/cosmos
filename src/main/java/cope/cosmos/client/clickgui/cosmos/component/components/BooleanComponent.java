package cope.cosmos.client.clickgui.cosmos.component.components;

import cope.cosmos.client.clickgui.cosmos.component.SettingComponent;
import cope.cosmos.client.features.modules.client.ClickGUI;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.AnimationManager;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

import java.awt.*;

import static org.lwjgl.opengl.GL11.glScaled;

public class BooleanComponent extends SettingComponent<Boolean> {

    private final AnimationManager animationManager;

    private int hoverAnimation = 0;

    public BooleanComponent(Setting<Boolean> setting, ModuleComponent moduleComponent) {
        super(setting, moduleComponent);
        animationManager = new AnimationManager(100, setting.getValue());
    }

    @Override
    public void drawSettingComponent(Vec2f position) {
        setPosition(position);

        // hover animation
        if (mouseOver(position.x, position.y, WIDTH, HEIGHT) && hoverAnimation < 25)
            hoverAnimation += 5;

        else if (!mouseOver(position.x, position.y, WIDTH, HEIGHT) && hoverAnimation > 0)
            hoverAnimation -= 5;

        float booleanAnimation = (float) MathHelper.clamp(animationManager.getAnimationFactor(), 0, 1);

        // background
        Color settingColor = isSubSetting() ? new Color(ClickGUI.INSTANCE.getSecondaryColor().getRed() + hoverAnimation, ClickGUI.INSTANCE.getSecondaryColor().getGreen() + hoverAnimation, ClickGUI.INSTANCE.getSecondaryColor().getBlue() + hoverAnimation) : new Color(ClickGUI.INSTANCE.getComplexionColor().getRed() + hoverAnimation, ClickGUI.INSTANCE.getComplexionColor().getGreen() + hoverAnimation, ClickGUI.INSTANCE.getComplexionColor().getBlue() + hoverAnimation);
        RenderUtil.drawRect(position.x, position.y, WIDTH, HEIGHT, settingColor);
        RenderUtil.drawRect(position.x, position.y, WIDTH, HEIGHT, settingColor);

        // checkbox background
        RenderUtil.drawRoundedRect(position.x + WIDTH - 12, position.y + 2, 10, 10, 2, isSubSetting() ? new Color(16 + hoverAnimation, 16 + hoverAnimation, 21 + hoverAnimation) : new Color(22 + hoverAnimation, 22 + hoverAnimation, 28 + hoverAnimation));

        // checkbox filled
        if (booleanAnimation > 0)
            RenderUtil.drawRoundedRect(position.x + WIDTH - 7 - (4 * booleanAnimation), position.y + 7 - (4 * booleanAnimation), 8 * booleanAnimation, 8 * booleanAnimation, 2, new Color(ClickGUI.INSTANCE.getPrimaryColor().getRed(), ClickGUI.INSTANCE.getPrimaryColor().getGreen(), ClickGUI.INSTANCE.getPrimaryColor().getBlue()));

        // setting name
        glScaled(0.55, 0.55, 0.55); {
            float scaledX = (position.x + 4) * 1.81818181F;
            float scaledY = (position.y + 5) * 1.81818181F;
            FontUtil.drawStringWithShadow(getSetting().getName(), scaledX, scaledY, -1);
        }

        glScaled(1.81818181, 1.81818181, 1.81818181);

        super.drawSettingComponent(position);
    }

    @Override
    public void handleLeftClick(int mouseX, int mouseY) {
        if (mouseOver(getPosition().x, getPosition().y, WIDTH, HEIGHT)) {
            // SoundUtil.clickSound();

            boolean currentValue = getSetting().getValue();

            getSetting().setValue(!currentValue);
            animationManager.setState(!currentValue);
        }

        super.handleLeftClick(mouseX, mouseY);
    }

    @Override
    public void handleLeftDrag(int mouseX, int mouseY) {
        super.handleLeftDrag(mouseX, mouseY);
    }

    @Override
    public void handleRightClick(int mouseX, int mouseY) {
        super.handleRightClick(mouseX, mouseY);
    }

    @Override
    public void handleKeyPress(char typedCharacter, int key) {
        super.handleKeyPress(typedCharacter, key);
    }

    @Override
    public void handleScroll(int scroll) {
        super.handleScroll(scroll);
    }

    public AnimationManager getAnimation() {
        return animationManager;
    }
}
