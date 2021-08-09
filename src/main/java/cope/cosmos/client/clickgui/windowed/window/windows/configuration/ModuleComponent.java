package cope.cosmos.client.clickgui.windowed.window.windows.configuration;

import cope.cosmos.client.clickgui.util.Util;
import cope.cosmos.client.clickgui.windowed.window.windows.ConfigurationWindow;
import cope.cosmos.client.clickgui.windowed.window.windows.ConfigurationWindow.Page;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import net.minecraft.util.math.Vec2f;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class ModuleComponent extends Component implements Util {

    private final ConfigurationWindow window;
    private final Module module;

    private final List<SettingComponent> settingComponents = new ArrayList<>();

    private boolean binding;
    private boolean lower;

    private int bindAnimation = 0;
    private int drawnAnimation = 0;
    private int hoverAnimation = 0;

    public ModuleComponent(ConfigurationWindow window, Module module) {
        this.window = window;
        this.module = module;

        module.getSettings().forEach(setting -> {
            if (!setting.hasParent())
                settingComponents.add(new SettingComponent(window, setting));
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
        FontUtil.drawStringWithShadow(module.getName(), position.x + 3, position.y + 3, module.isEnabled() ? Color.RED.getRGB() : -1);

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

        float buttonHeight = FontUtil.getFontHeight() + (FontUtil.getFontHeight() * 0.6F) + (!lowerLine.toString().equals("") ? (FontUtil.getFontHeight() * 0.6F + 2) : 0) + 7;
        float buttonOffset = 0;

        if (mouseOver(position.x + 4, position.y + buttonHeight, (FontUtil.getStringWidth("Bind: " + (binding ? "Listening..." : Keyboard.getKeyName(module.getKey()))) * 0.8F) + 4, (FontUtil.getFontHeight() * 0.8F) + 3) && bindAnimation < 25)
            bindAnimation += 5;

        else if (!mouseOver(position.x + 4, position.y + buttonHeight, (FontUtil.getStringWidth("Bind: " + (binding ? "Listening..." : Keyboard.getKeyName(module.getKey()))) * 0.8F) + 4, (FontUtil.getFontHeight() * 0.8F) + 3) && bindAnimation > 0)
            bindAnimation -= 5;

        // bind button
        RenderUtil.drawBorderRect(position.x + 4, position.y + buttonHeight, (FontUtil.getStringWidth("Bind: " + (binding ? "Listening..." : Keyboard.getKeyName(module.getKey()))) * 0.8F) + 4, (FontUtil.getFontHeight() * 0.8F) + 3, new Color(20 + bindAnimation, 20 + bindAnimation, 20 + bindAnimation, 60), new Color(0, 0, 0, 60));

        glScaled(0.8, 0.8, 0.8); {
            float scaledX = (position.x + 6) * 1.25F;
            float scaledY = (position.y + buttonHeight + 2) * 1.25F;
            FontUtil.drawStringWithShadow("Bind: " + (binding ? "Listening..." : Keyboard.getKeyName(module.getKey())), scaledX, scaledY, -1);
        }

        glScaled(1.25, 1.25, 1.25);

        // offset the buttons
        buttonOffset += (FontUtil.getStringWidth("Bind: " + (binding ? "Listening..." : Keyboard.getKeyName(module.getKey()))) * 0.8F) + 11;

        if (mouseOver(position.x + buttonOffset + 4, position.y + buttonHeight, (FontUtil.getStringWidth("Drawn: " + module.isDrawn()) * 0.8F) + 4, (FontUtil.getFontHeight() * 0.8F) + 3) && drawnAnimation < 25)
            drawnAnimation += 5;

        else if (!mouseOver(position.x + buttonOffset, position.y + buttonHeight, (FontUtil.getStringWidth("Drawn: " + module.isDrawn()) * 0.8F) + 4, (FontUtil.getFontHeight() * 0.8F) + 3) && drawnAnimation > 0)
            drawnAnimation -= 5;

        // drawn button
        RenderUtil.drawBorderRect(position.x + buttonOffset, position.y + buttonHeight, (FontUtil.getStringWidth("Drawn: " + module.isDrawn()) * 0.8F) + 4, (FontUtil.getFontHeight() * 0.8F) + 3, new Color(20 + drawnAnimation, 20 + drawnAnimation, 20 + drawnAnimation, 60), new Color(0, 0, 0, 60));

        glScaled(0.8, 0.8, 0.8); {
            float scaledX = (position.x + 2 + buttonOffset) * 1.25F;
            float scaledY = (position.y + buttonHeight + 2) * 1.25F;
            FontUtil.drawStringWithShadow("Drawn: " + module.isDrawn(), scaledX, scaledY, -1);
        }

        glScaled(1.25, 1.25, 1.25);

        glPopMatrix();
    }

    @Override
    public void handleLeftClick() {
        try {
            if (mouseOver(getPosition().x, getPosition().y, getWidth(), getHeight() - 19))
                module.toggle();

            if (mouseOver(getPosition().x + FontUtil.getStringWidth("Bind: " + (binding ? "Listening..." : Keyboard.getKeyName(module.getKey()))) + 10, getPosition().y + getHeight() - 14, (FontUtil.getStringWidth("Drawn: " + module.isDrawn()) * 0.8F) + 4, (FontUtil.getFontHeight() * 0.8F) + 3)) {
                boolean previousDrawn = module.isDrawn();
                module.setDrawn(!previousDrawn);
            }

            if (mouseOver(getPosition().x + 3, getPosition().y + getHeight() - 14, (FontUtil.getStringWidth("Bind: " + (binding ? "Listening..." : Keyboard.getKeyName(module.getKey()))) * 0.8F) + 4, (FontUtil.getFontHeight() * 0.8F) + 3))
                binding = !binding;
        } catch (Exception ignored) {

        }
    }

    @Override
    public void handleRightClick() {
        try {
            if (mouseOver(getPosition().x, getPosition().y, getWidth(), getHeight())) {
                window.setModuleComponent(this);
                window.setPage(Page.SETTING);
                window.updateColumns();
            }
        } catch (Exception ignored) {

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
