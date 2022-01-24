package cope.cosmos.client.ui.clickgui.window.windows.configuration;

import cope.cosmos.client.ui.util.GUIUtil;
import cope.cosmos.client.ui.clickgui.window.windows.ConfigurationWindow;
import cope.cosmos.client.ui.clickgui.window.windows.ConfigurationWindow.Page;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.util.Wrapper;
import cope.cosmos.util.client.ColorUtil;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import net.minecraft.util.math.Vec2f;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class ModuleComponent extends Component implements GUIUtil, Wrapper {

    private final ConfigurationWindow window;
    private final Module module;

    private final List<SettingComponent> settingComponents = new ArrayList<>();

    private boolean binding;
    private int hoverAnimation = 0;

    public ModuleComponent(ConfigurationWindow window, Module module) {
        this.window = window;
        this.module = module;

        module.getSettings().forEach(setting -> {
            if (!setting.hasParent()) {
                settingComponents.add(new SettingComponent(window, setting));
            }
        });
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
        String[] words = module.getDescription().split(" ");

        boolean lower = false;
        for (String word : words) {
            if ((FontUtil.getStringWidth(upperLine + word) * 0.6) > width) {
                lowerLine.append(" ").append(word);
                lower = true;
            }

            else if (!lower) {
                upperLine.append(" ").append(word);
            }
        }

        // set the height to equal the component's height
        setHeight(FontUtil.getFontHeight() + (FontUtil.getFontHeight() * 0.6F) + (!lowerLine.toString().equals("") ? (FontUtil.getFontHeight() * 0.6F + 2) : 0) + 7);

        // module background
        RenderUtil.drawRect(position.x, position.y, width, getHeight(), new Color(hoverAnimation, hoverAnimation, hoverAnimation, 40));

        // module name & description
        FontUtil.drawStringWithShadow(module.getName(), position.x + 3, position.y + 3, module.isEnabled() ? ColorUtil.getPrimaryColor().getRGB() : -1);

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
        if (mouseOver(getPosition().x, getPosition().y, getWidth(), getHeight())) {
            module.toggle();
            getCosmos().getSoundManager().playSound("click");
        }
    }

    @Override
    public void handleRightClick() {
        if (mouseOver(getPosition().x, getPosition().y, getWidth(), getHeight())) {
            window.setModuleComponent(this);
            window.setPage(Page.SETTING);
            window.updateColumns();
            getCosmos().getSoundManager().playSound("click");
        }
    }

    @Override
    public void handleKeyPress(char typedCharacter, int key) {
        if (binding) {
            if (key != -1 && key != Keyboard.KEY_ESCAPE) {
                module.setKey(key == Keyboard.KEY_DELETE ? Keyboard.KEY_NONE : key);
                binding = false;
            }
        }
    }

    public Module getModule() {
        return module;
    }

    public List<SettingComponent> getSettingComponents() {
        return settingComponents;
    }
}
