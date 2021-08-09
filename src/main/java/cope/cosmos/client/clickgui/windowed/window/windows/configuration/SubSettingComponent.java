package cope.cosmos.client.clickgui.windowed.window.windows.configuration;

import cope.cosmos.client.clickgui.windowed.window.windows.ConfigurationWindow;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import net.minecraft.util.math.Vec2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class SubSettingComponent extends Component {

    private final ConfigurationWindow window;
    private final Setting<?> setting;

    private final List<SettingComponent> settingComponents = new ArrayList<>();

    private int hoverAnimation = 0;

    private boolean lower;

    public SubSettingComponent(ConfigurationWindow window, Setting<?> setting) {
        this.window = window;
        this.setting = setting;
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
        setHeight(FontUtil.getFontHeight() + (FontUtil.getFontHeight() * 0.6F) + (!lowerLine.toString().equals("") ? (FontUtil.getFontHeight() * 0.6F + 2) : 0) + 20);

        // module background
        RenderUtil.drawRect(position.x, position.y, width, getHeight(), new Color(hoverAnimation, hoverAnimation, hoverAnimation, 40));

        // module name & description
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

        glPopMatrix();
    }

    @Override
    public void handleLeftClick() {

    }

    @Override
    public void handleRightClick() {

    }

    @Override
    public void handleKeyPress(char typedCharacter, int key) {

    }

    public Setting<?> getSetting() {
        return setting;
    }

    public List<SettingComponent> getSettingComponents() {
        return settingComponents;
    }
}
