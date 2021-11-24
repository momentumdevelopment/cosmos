package cope.cosmos.client.clickgui.windowed.window.windows.configuration.types;

import cope.cosmos.client.clickgui.windowed.window.windows.configuration.SettingComponent;
import cope.cosmos.client.clickgui.windowed.window.windows.configuration.TypeComponent;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.client.ColorUtil;
import cope.cosmos.util.render.RenderUtil;
import net.minecraft.util.math.Vec2f;

import java.awt.*;

public class BooleanComponent extends TypeComponent<Boolean> {
    public BooleanComponent(SettingComponent settingComponent, Setting<Boolean> setting) {
        super(settingComponent, setting);
    }

    @Override
    public void drawType(Vec2f position, float width, float height, float boundHeight) {
        setPosition(position);
        setWidth(width);
        setHeight(height);
        setBoundHeight(boundHeight);

        RenderUtil.drawRoundedRect(position.x + width - 18.5F, position.y + 2.5F, 16, 16, 5, getSetting().getValue() ? new Color(ColorUtil.getPrimaryColor().getRed(), ColorUtil.getPrimaryColor().getGreen(), ColorUtil.getPrimaryColor().getBlue(), 140) : new Color(0, 0, 0, 80));
    }

    @Override
    public void handleLeftClick() {
        if (mouseOver(getPosition().x + getWidth() - 19, getPosition().y + 2, 17, 17)) {
            boolean previousValue = getSetting().getValue();
            getSetting().setValue(!previousValue);
        }
    }

    @Override
    public void handleRightClick() {

    }

    @Override
    public void handleKeyPress(char typedCharacter, int key) {

    }
}
