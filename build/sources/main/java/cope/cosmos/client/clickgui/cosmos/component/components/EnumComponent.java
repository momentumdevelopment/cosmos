package cope.cosmos.client.clickgui.cosmos.component.components;

import cope.cosmos.client.clickgui.cosmos.component.SettingComponent;
import cope.cosmos.client.features.modules.client.ClickGUI;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.world.SoundUtil;
import net.minecraft.util.math.Vec2f;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class EnumComponent extends SettingComponent<Enum<?>> {

    private int hoverAnimation = 0;

    public EnumComponent(Setting<Enum<?>> setting, ModuleComponent moduleComponent) {
        super(setting, moduleComponent);
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
        Color settingColor = isSubSetting() ? new Color(ClickGUI.INSTANCE.getSecondaryColor().getRed() + hoverAnimation, ClickGUI.INSTANCE.getSecondaryColor().getGreen() + hoverAnimation, ClickGUI.INSTANCE.getSecondaryColor().getBlue() + hoverAnimation) : new Color(ClickGUI.INSTANCE.getComplexionColor().getRed() + hoverAnimation, ClickGUI.INSTANCE.getComplexionColor().getGreen() + hoverAnimation, ClickGUI.INSTANCE.getComplexionColor().getBlue() + hoverAnimation);
        RenderUtil.drawRect(position.x, position.y, WIDTH, HEIGHT, settingColor);
        RenderUtil.drawRect(position.x, position.y, WIDTH, HEIGHT, settingColor);

        // setting name & value
        glScaled(0.55, 0.55, 0.55); {
            float scaledX = (position.x + 4) * 1.81818181F;
            float scaledWidth = (position.x + WIDTH - (FontUtil.getStringWidth(Setting.formatEnum(getSetting().getValue())) * 0.55F) - 3) * 1.81818181F;
            float scaledY = (position.y + 5) * 1.81818181F;

            FontUtil.drawStringWithShadow(getSetting().getName(), scaledX, scaledY, -1);
            FontUtil.drawStringWithShadow(Setting.formatEnum(getSetting().getValue()), scaledWidth, scaledY, -1);
        }

        glScaled(1.81818181, 1.81818181, 1.81818181);

        super.drawSettingComponent(position);
    }

    @Override
    public void handleLeftClick(int mouseX, int mouseY) {
        if (mouseOver(getPosition().x, getPosition().y, WIDTH, HEIGHT)) {
            SoundUtil.clickSound();

            Enum<?> nextSettingValue = getSetting().getNextMode();
            getSetting().setValue(nextSettingValue);
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
}
