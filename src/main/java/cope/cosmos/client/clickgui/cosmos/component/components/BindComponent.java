package cope.cosmos.client.clickgui.cosmos.component.components;

import cope.cosmos.client.clickgui.cosmos.component.SettingComponent;
import cope.cosmos.client.features.modules.client.ClickGUI;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import net.minecraft.util.math.Vec2f;
import org.lwjgl.input.Keyboard;

import java.awt.*;

import static org.lwjgl.opengl.GL11.glScaled;

public class BindComponent extends SettingComponent<String> {

    private int hoverAnimation = 0;

    private boolean binding;

    public BindComponent(ModuleComponent moduleComponent) {
        super(new Setting<>("Bind", "Dummy Setting", "Dummy"), moduleComponent);

        // binding is always false when opening gui
        binding = false;
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
            float scaledWidth = (position.x + WIDTH - (FontUtil.getStringWidth(binding ? "Listening ..." : Keyboard.getKeyName(getModuleComponent().getModule().getKey())) * 0.55F) - 3) * 1.81818181F;
            float scaledY = (position.y + 5) * 1.81818181F;

            FontUtil.drawStringWithShadow("Bind", scaledX, scaledY, -1);
            FontUtil.drawStringWithShadow(binding ? "Listening ..." : Keyboard.getKeyName(getModuleComponent().getModule().getKey()), scaledWidth, scaledY, -1);
        }

        glScaled(1.81818181, 1.81818181, 1.81818181);
    }

    @Override
    public void handleLeftClick(int mouseX, int mouseY) {
        if (mouseOver(getPosition().x, getPosition().y, WIDTH, HEIGHT)) {
            // SoundUtil.clickSound();

            binding = !binding;
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
        if (binding) {
            if (key != -1 && key != Keyboard.KEY_ESCAPE) {
                getModuleComponent().getModule().setKey(key == Keyboard.KEY_DELETE ? Keyboard.KEY_NONE : key);
                binding = false;
            }
        }
    }

    @Override
    public void handleScroll(int scroll) {

    }
}
