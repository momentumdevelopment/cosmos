package cope.cosmos.client.clickgui.windowed.window.windows.configuration;

import cope.cosmos.client.clickgui.windowed.window.windows.ConfigurationWindow;
import cope.cosmos.client.clickgui.windowed.window.windows.ConfigurationWindow.Page;
import cope.cosmos.client.clickgui.windowed.window.windows.configuration.types.BooleanComponent;
import cope.cosmos.client.clickgui.windowed.window.windows.configuration.types.TypeComponent;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import net.minecraft.util.math.Vec2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class SettingComponent extends Component {

    private TypeComponent<?> typeComponent = null;

    private final ConfigurationWindow window;
    private final Setting<?> setting;

    private final List<SubSettingComponent> settingComponents = new ArrayList<>();

    private int hoverAnimation = 0;

    private boolean lower;

    @SuppressWarnings("unchecked")
    public SettingComponent(ConfigurationWindow window, Setting<?> setting) {
        this.window = window;
        this.setting = setting;

        setting.getSubSettings().forEach(subSetting -> {
            settingComponents.add(new SubSettingComponent(window, subSetting));
        });

        if (setting.getValue() instanceof Boolean) {
            typeComponent = new BooleanComponent((Setting<Boolean>) setting);
        }
    }

    @Override
    public void drawComponent(Vec2f position, float width) {
        setPosition(position);
        setWidth(width);

        glPushMatrix();

        // hover animation
        if (mouseOver(position.x, position.y, width, getHeight()) && hoverAnimation < 25)
            hoverAnimation += 5;

        else if (!mouseOver(position.x, position.y, width, getHeight()) && hoverAnimation > 0)
            hoverAnimation -= 5;

        // split the description into two lines
        StringBuilder upperLine = new StringBuilder();
        StringBuilder lowerLine = new StringBuilder();

        // split the description into individual words
        String[] words = setting.getDescription().split(" ");

        lower = false;
        for (String word : words) {
            if ((FontUtil.getStringWidth(upperLine.toString() + word) * 0.6) > width) {
                lowerLine.append(" ").append(word);
                lower = true;
            }

            else if (!lower) {
                upperLine.append(" ").append(word);
            }
        }

        // set the height to equal the component's height
        setHeight(FontUtil.getFontHeight() + (FontUtil.getFontHeight() * 0.6F) + (!lowerLine.toString().equals("") ? (FontUtil.getFontHeight() * 0.6F + 2) : 0) + 7);

        // setting background
        RenderUtil.drawRect(position.x, position.y, width, getHeight(), new Color(hoverAnimation, hoverAnimation, hoverAnimation, 40));

        // setting name & description
        FontUtil.drawStringWithShadow(setting.getName(), position.x + 3, position.y + 3, -1);

        glScaled(0.6, 0.6, 0.6); {
            float scaledUpperX = (position.x + 1) * 1.6666667F;
            float scaledUpperY = (position.y + FontUtil.getFontHeight() + 4.5F) * 1.6666667F;
            FontUtil.drawStringWithShadow(upperLine.toString(), scaledUpperX, scaledUpperY, -1);

            if (!lowerLine.toString().equals("")) {
                float scaledLowerX = (position.x + 1) * 1.6666667F;
                float scaledLowerY = (position.y + FontUtil.getFontHeight() + 4.5F + ((FontUtil.getFontHeight() + 1.5F) * 0.6F)) * 1.6666667F;
                FontUtil.drawStringWithShadow(lowerLine.toString(), scaledLowerX, scaledLowerY, -1);
            }
        }

        glScaled(1.6666667, 1.6666667, 1.6666667);

        if (typeComponent != null) {
            typeComponent.drawType(position, width);
        }

        glPopMatrix();
    }

    @Override
    public void handleLeftClick() {
        if (typeComponent != null) {
            typeComponent.handleLeftClick();
        }
    }

    @Override
    public void handleRightClick() {
        try {
            if (mouseOver(getPosition().x, getPosition().y, getWidth(), getHeight())) {
                window.setSettingComponent(this);
                window.setPage(Page.SUBSETTING);
                window.updateColumns();
            }
        } catch (Exception ignored) {

        }

        if (typeComponent != null) {
            typeComponent.handleRightClick();
        }
    }

    @Override
    public void handleKeyPress(char typedCharacter, int key) {
        if (typeComponent != null) {
            typeComponent.handleKeyPress(typedCharacter, key);
        }
    }

    public Setting<?> getSetting() {
        return setting;
    }

    public List<SubSettingComponent> getSettingComponents() {
        return settingComponents;
    }
}
