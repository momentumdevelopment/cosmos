package cope.cosmos.client.clickgui.windowed.window.windows;

import cope.cosmos.client.clickgui.util.Util;
import cope.cosmos.client.clickgui.windowed.window.TabbedWindow;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.ModuleManager;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import net.minecraft.util.math.Vec2f;
import org.lwjgl.input.Keyboard;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;

@SuppressWarnings("unused")
public class ModulesWindow extends TabbedWindow {

    private final List<ModuleComponent> leftColumn = new ArrayList<>();
    private final List<ModuleComponent> rightColumn = new ArrayList<>();

    private float leftOffset;
    private float rightOffset;

    public ModulesWindow(String name, Vec2f position) {
        super(name, position);

        // add each of the categories as a tab
        for (Category category : Category.values()) {
            getTabs().add(new Tab<>(Setting.formatEnum(category), category));
        }

        // set our current tab as the first category
        setTab(getTabs().get(0));

        // update our columns
        updateColumns();
    }

    @Override
    public void drawWindow() {
        super.drawWindow();

        glPushAttrib(GL_SCISSOR_BIT); {
            RenderUtil.scissor((int) (getPosition().x + 3), (int) (getPosition().y + getBar() + 3), (int) (getPosition().x + getWidth() - 15), (int) (getPosition().y + getHeight() - 3));
            glEnable(GL_SCISSOR_TEST);
        }

        // module background
        RenderUtil.drawBorderRect(getPosition().x + 4, getPosition().y + getBar() + getTab().getHeight() + 6, getWidth() - 20, getHeight() - getBar() - getTab().getHeight() - 10, new Color(0, 0, 0, 40), new Color(0, 0, 0, 70));

        float halfWidth = (getWidth() - 28) / 2;

        leftOffset = 0;
        leftColumn.forEach(moduleComponent -> {
            moduleComponent.drawComponent(new Vec2f(getPosition().x + 6, getPosition().y + getBar() + getTab().getHeight() + 8 + leftOffset + getScroll()), halfWidth);
            leftOffset += moduleComponent.getHeight() + 4;
        });

        rightOffset = 0;
        rightColumn.forEach(moduleComponent -> {
            moduleComponent.drawComponent(new Vec2f(getPosition().x + 10 + halfWidth, getPosition().y + getBar() + getTab().getHeight() + 8 + rightOffset + getScroll()), halfWidth);
            rightOffset += moduleComponent.getHeight() + 4;
        });

        glDisable(GL_SCISSOR_TEST);
        glPopAttrib();
    }

    @Override
    public void handleLeftClick() {
        super.handleLeftClick();

        leftColumn.forEach(moduleComponent -> {
            moduleComponent.handleLeftClick();
        });

        rightColumn.forEach(moduleComponent -> {
            moduleComponent.handleLeftClick();
        });
    }

    @Override
    public void handleTabChange() {
        super.handleTabChange();

        // update our columns when the tab changes
        updateColumns();
    }

    @Override
    public void handleKeyPress(char typedCharacter, int key) {
        super.handleKeyPress(typedCharacter, key);

        leftColumn.forEach(moduleComponent -> {
            moduleComponent.handleKeyPress(typedCharacter, key);
        });

        rightColumn.forEach(moduleComponent -> {
            moduleComponent.handleKeyPress(typedCharacter, key);
        });
    }

    public void updateColumns() {
        // clear our old columns
        leftColumn.clear();
        rightColumn.clear();

        // add all the modules to the columns
        AtomicBoolean left = new AtomicBoolean(true);
        ModuleManager.getModules(module -> module.getCategory().equals(getTab().getObject())).forEach(module -> {
            if (left.get()) {
                leftColumn.add(new ModuleComponent(module));
            }

            else {
                rightColumn.add(new ModuleComponent(module));
            }

            left.set(!left.get());
        });
    }

    public static class ModuleComponent implements Util {

        private final Module module;

        private Vec2f position;
        private float width;
        private float height;

        private boolean binding;
        private boolean lower;

        private int bindAnimation = 0;
        private int drawnAnimation = 0;
        private int hoverAnimation = 0;

        public ModuleComponent(Module module) {
            this.module = module;
        }

        public void drawComponent(Vec2f position, float width) {
            setPosition(position);
            setWidth(width);

            glPushMatrix();

            // hover animation
            if (mouseOver(position.x, position.y, width, height) && hoverAnimation < 25)
                hoverAnimation += 5;

            else if (!mouseOver(position.x, position.y, width, height) && hoverAnimation > 0)
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
            RenderUtil.drawRect(position.x, position.y, width, height, new Color(hoverAnimation, hoverAnimation, hoverAnimation, 40));

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
            buttonOffset += (FontUtil.getStringWidth("Bind: " + (binding ? "Listening..." : Keyboard.getKeyName(module.getKey()))) * 0.8F) + 2;

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

        public void handleLeftClick() {
            try {
                if (mouseOver(position.x, position.y, width, height - 19))
                    module.toggle();

                if (mouseOver(position.x + FontUtil.getStringWidth("Bind: " + (binding ? "Listening..." : Keyboard.getKeyName(module.getKey()))) + 10, position.y + height - 14, (FontUtil.getStringWidth("Drawn: " + module.isDrawn()) * 0.8F) + 4, (FontUtil.getFontHeight() * 0.8F) + 3)) {
                    boolean previousDrawn = module.isDrawn();
                    module.setDrawn(!previousDrawn);
                }

                if (mouseOver(position.x + 3, position.y + height - 14, (FontUtil.getStringWidth("Bind: " + (binding ? "Listening..." : Keyboard.getKeyName(module.getKey()))) * 0.8F) + 4, (FontUtil.getFontHeight() * 0.8F) + 3))
                    binding = !binding;
            } catch (Exception ignored) {

            }
        }

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

        public void setPosition(Vec2f in) {
            position = in;
        }

        public Vec2f getPosition() {
            return position;
        }

        public void setWidth(float in) {
            width = in;
        }

        public float getWidth() {
            return width;
        }

        public void setHeight(float in) {
            height = in;
        }

        public float getHeight() {
            return height;
        }
    }
}