package cope.cosmos.client.clickgui.cosmos.component.components;

import cope.cosmos.client.clickgui.cosmos.component.SettingComponent;
import cope.cosmos.client.features.modules.client.ClickGUI;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import net.minecraft.util.math.Vec2f;

import java.awt.*;

import static org.lwjgl.opengl.GL11.glScaled;

public class DrawnComponent extends SettingComponent<String> {

    private int hoverAnimation = 0;

    public DrawnComponent(ModuleComponent moduleComponent) {
        super(new Setting<>("Drawn", "Dummy Setting", "Dummy"), moduleComponent);
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

        // drawn name & value
        glScaled(0.55, 0.55, 0.55); {
            float scaledX = (position.x + 4) * 1.81818181F;
            float scaledWidth = (position.x + WIDTH - (FontUtil.getStringWidth(String.valueOf(getModuleComponent().getModule().isDrawn())) * 0.55F) - 3) * 1.81818181F;
            float scaledY = (position.y + 5) * 1.81818181F;

            FontUtil.drawStringWithShadow("Drawn", scaledX, scaledY, -1);
            FontUtil.drawStringWithShadow(String.valueOf(getModuleComponent().getModule().isDrawn()), scaledWidth, scaledY, -1);
        }

        glScaled(1.81818181, 1.81818181, 1.81818181);
    }

    @Override
    public void handleLeftClick(int mouseX, int mouseY) {
        if (mouseOver(getPosition().x, getPosition().y, WIDTH, HEIGHT)) {
            // SoundUtil.clickSound();

            boolean drawnValue = getModuleComponent().getModule().isDrawn();
            getModuleComponent().getModule().setDrawn(!drawnValue);
        }
    }

    @Override
    public void handleLeftDrag(int mouseX, int mouseY) {

    }

    @Override
    public void handleRightClick(int mouseX, int mouseY) {

    }

    @Override
    public void handleKeyPress(char typedCharacter, int key) {

    }

    @Override
    public void handleScroll(int scroll) {

    }
}
