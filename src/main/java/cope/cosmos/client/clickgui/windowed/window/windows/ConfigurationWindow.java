package cope.cosmos.client.clickgui.windowed.window.windows;

import cope.cosmos.client.clickgui.windowed.window.TabbedWindow;
import cope.cosmos.client.clickgui.windowed.window.windows.configuration.Component;
import cope.cosmos.client.clickgui.windowed.window.windows.configuration.ModuleComponent;
import cope.cosmos.client.clickgui.windowed.window.windows.configuration.SettingComponent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.ModuleManager;
import cope.cosmos.util.client.ColorUtil;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec2f;
import org.lwjgl.input.Keyboard;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.lwjgl.opengl.GL11.*;

@SuppressWarnings("unused")
public class ConfigurationWindow extends TabbedWindow {

    private Page page = Page.MODULE;

    private final List<Component> leftColumn = new CopyOnWriteArrayList<>();
    private final List<Component> rightColumn = new CopyOnWriteArrayList<>();

    private float leftOffset;
    private float rightOffset;

    private ModuleComponent moduleComponent;
    private SettingComponent settingComponent;

    private int bindAnimation;
    private int drawnAnimation;
    private int backAnimation;

    private boolean binding;

    private float buttonHeight;
    private float buttonOffset;

    private float halfWidth;
    private float quarterWidth;

    public ConfigurationWindow(String name, Vec2f position) {
        super(name, new ResourceLocation("cosmos", "textures/icons/client.png"), position, 400, 300, true);

        // add each of the categories as a tab
        for (Category category : Category.values()) {
            if (category.equals(Category.HIDDEN))
                continue;

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

        if (mouseOver(getPosition().x + 4, getPosition().y + getBar() + 4, 13, getTab().getHeight() - 2) && backAnimation < 25)
            backAnimation += 5;

        else if (!mouseOver(getPosition().x + 4, getPosition().y + getBar() + 4, 13, getTab().getHeight() - 2) && backAnimation > 0)
            backAnimation -= 5;

        // back button
        RenderUtil.drawBorderRect(getPosition().x + 4, getPosition().y + getBar() + 4, 13, getTab().getHeight() - 2, new Color(backAnimation, backAnimation, backAnimation, 70), new Color(0, 0, 0, 130));

        glPushMatrix();
        glColor4d(1, 1, 1, 1);
        mc.getTextureManager().bindTexture(new ResourceLocation("cosmos", "textures/icons/back.png"));
        GuiScreen.drawModalRectWithCustomSizedTexture((int) (getPosition().x + 4), (int) (getPosition().y + getBar() + 4), 0, 0, 13, 13, 13, 13);
        glPopMatrix();

        // module background
        RenderUtil.drawBorderRect(getPosition().x + 4, getPosition().y + getBar() + getTab().getHeight() + 6, getWidth() - 20, getHeight() - getBar() - getTab().getHeight() - 10, new Color(0, 0, 0, 40), new Color(0, 0, 0, 70));

        glPushAttrib(GL_SCISSOR_BIT); {
            RenderUtil.scissor((int) (getPosition().x + 3), (int) (getPosition().y + getBar() + 19), (int) (getPosition().x + getWidth() - 15), (int) (getPosition().y + getHeight() - 3));
            glEnable(GL_SCISSOR_TEST);
        }

        halfWidth = (getWidth() - 28) / 2;

        if (page.equals(Page.SETTING) && moduleComponent != null) {
            // split the description into two lines
            StringBuilder upperLine = new StringBuilder();
            StringBuilder lowerLine = new StringBuilder();

            // split the description into individual words
            String[] words = moduleComponent.getModule().getDescription().split(" ");

            boolean lower = false;
            for (String word : words) {
                if ((FontUtil.getStringWidth(upperLine.toString() + word) * 0.6) > halfWidth) {
                    lowerLine.append(" ").append(word);
                    lower = true;
                }

                else if (!lower) {
                    upperLine.append(" ").append(word);
                }
            }

            // module name & description
            FontUtil.drawStringWithShadow(moduleComponent.getModule().getName(), getPosition().x + 8, getPosition().y + getBar() + getTab().getHeight() + 11 - getScroll(), moduleComponent.getModule().isEnabled() ? ColorUtil.getPrimaryColor().getRGB() : -1);

            glScaled(0.6, 0.6, 0.6); {
                float scaledUpperX = (getPosition().x + 6) * 1.6666667F;
                float scaledUpperY = (getPosition().y + FontUtil.getFontHeight() + getBar() + getTab().getHeight() + 12.5F - getScroll()) * 1.6666667F;
                FontUtil.drawStringWithShadow(upperLine.toString(), scaledUpperX, scaledUpperY, -1);

                if (!lowerLine.toString().equals("")) {
                    float scaledLowerX = (getPosition().x + 6) * 1.6666667F;
                    float scaledLowerY = (getPosition().y + FontUtil.getFontHeight() + getBar() + getTab().getHeight() + 12.5F - getScroll() + ((FontUtil.getFontHeight() + 1.5F) * 0.6F)) * 1.6666667F;
                    FontUtil.drawStringWithShadow(lowerLine.toString(), scaledLowerX, scaledLowerY, -1);
                }
            }

            glScaled(1.6666667, 1.6666667, 1.6666667);

            buttonHeight = FontUtil.getFontHeight() + (FontUtil.getFontHeight() * 0.6F) + (!lowerLine.toString().equals("") ? (FontUtil.getFontHeight() * 0.6F + 2) : 0) + 15;
            quarterWidth = (halfWidth / 2) - 5;

            if (mouseOver(getPosition().x + 8, getPosition().y + getBar() + getTab().getHeight() - getScroll() + buttonHeight, quarterWidth, (FontUtil.getFontHeight() * 0.8F) + 3) && bindAnimation < 25)
                bindAnimation += 5;

            else if (!mouseOver(getPosition().x + 8, getPosition().y + getBar() + getTab().getHeight() - getScroll() + buttonHeight, quarterWidth, (FontUtil.getFontHeight() * 0.8F) + 3) && bindAnimation > 0)
                bindAnimation -= 5;

            // bind button
            RenderUtil.drawBorderRect(getPosition().x + 8, getPosition().y + getBar() + getTab().getHeight() - getScroll() + buttonHeight, quarterWidth, (FontUtil.getFontHeight() * 0.8F) + 3, new Color(20 + bindAnimation, 20 + bindAnimation, 20 + bindAnimation, 60), new Color(0, 0, 0, 60));

            glScaled(0.8, 0.8, 0.8); {
                float scaledX = (getPosition().x + (((getPosition().x + quarterWidth) - getPosition().x) / 2) - (FontUtil.getStringWidth("Bind: " + (binding ? "Listening..." : Keyboard.getKeyName(moduleComponent.getModule().getKey()))) / 2F) + 10) * 1.25F;
                float scaledY = (getPosition().y + getBar() + getTab().getHeight() - getScroll() + buttonHeight + 2) * 1.25F;
                FontUtil.drawStringWithShadow("Bind: " + (binding ? "Listening..." : Keyboard.getKeyName(moduleComponent.getModule().getKey())), scaledX, scaledY, -1);
            }

            glScaled(1.25, 1.25, 1.25);

            // offset the buttons
            buttonOffset = quarterWidth + 5;

            if (mouseOver(getPosition().x + buttonOffset + 8, getPosition().y + getBar() + getTab().getHeight() - getScroll() + buttonHeight, quarterWidth, (FontUtil.getFontHeight() * 0.8F) + 3) && drawnAnimation < 25)
                drawnAnimation += 5;

            else if (!mouseOver(getPosition().x + buttonOffset + 8, getPosition().y + getBar() + getTab().getHeight() - getScroll() + buttonHeight, quarterWidth, (FontUtil.getFontHeight() * 0.8F) + 3) && drawnAnimation > 0)
                drawnAnimation -= 5;

            // drawn button
            RenderUtil.drawBorderRect(getPosition().x + buttonOffset + 8, getPosition().y + getBar() + getTab().getHeight() - getScroll() + buttonHeight, quarterWidth, (FontUtil.getFontHeight() * 0.8F) + 3, new Color(20 + drawnAnimation, 20 + drawnAnimation, 20 + drawnAnimation, 60), new Color(0, 0, 0, 60));

            glScaled(0.8, 0.8, 0.8); {
                float scaledX = (getPosition().x + (((getPosition().x + quarterWidth) - getPosition().x) / 2) + buttonOffset - (FontUtil.getStringWidth("Drawn: " + moduleComponent.getModule().isDrawn()) / 2F) + 10) * 1.25F;
                float scaledY = (getPosition().y + getBar() + getTab().getHeight() - getScroll() + buttonHeight + 2) * 1.25F;
                FontUtil.drawStringWithShadow("Drawn: " + moduleComponent.getModule().isDrawn(), scaledX, scaledY, -1);
            }

            glScaled(1.25, 1.25, 1.25);

            // set the offset to account for the page title
            leftOffset = FontUtil.getFontHeight() + (FontUtil.getFontHeight() * 0.6F) + (!lowerLine.toString().equals("") ? (FontUtil.getFontHeight() * 0.6F + 2) : 0) + 21;
        }

        else {
            leftOffset = 0;
        }

        // render both of our columns
        leftColumn.forEach(component -> {
            if (component.isVisible()) {
                component.drawComponent(new Vec2f(getPosition().x + 6, getPosition().y + getBar() + getTab().getHeight() + 8 + leftOffset - getScroll()), halfWidth);
                leftOffset += component.getHeight() + 4;
            }
        });

        rightOffset = 0;
        rightColumn.forEach(component -> {
            if (component.isVisible()) {
                component.drawComponent(new Vec2f(getPosition().x + 10 + halfWidth, getPosition().y + getBar() + getTab().getHeight() + 8 + rightOffset - getScroll()), halfWidth);
                rightOffset += component.getHeight() + 4;
            }
        });

        // set our lower bound as our lowest component
        setLowerBound(Math.max(leftOffset, rightOffset));

        glDisable(GL_SCISSOR_TEST);
        glPopAttrib();
    }

    @Override
    public void handleLeftClick() {
        super.handleLeftClick();

        if (mouseOver(getPosition().x + 4, getPosition().y + getBar() + 4, 13, 13)) {
            switch (page) {
                case MODULE:
                    break;
                case SETTING:
                    setPage(Page.MODULE);
                    break;
                case SUBSETTING:
                    setPage(Page.SETTING);
                    break;
            }

            updateColumns();
        }

        leftColumn.forEach(component -> {
            component.handleLeftClick();
        });

        rightColumn.forEach(component -> {
            component.handleLeftClick();
        });

        if (page.equals(Page.SETTING) && moduleComponent != null) {
            if (mouseOver(getPosition().x + 8, getPosition().y + getBar() + getTab().getHeight() + buttonHeight, quarterWidth, (FontUtil.getFontHeight() * 0.8F) + 3)) {
                binding = !binding;
            }

            if (mouseOver(getPosition().x + buttonOffset + 8, getPosition().y + getBar() + getTab().getHeight() - getScroll() + buttonHeight, quarterWidth, (FontUtil.getFontHeight() * 0.8F) + 3)) {
                boolean previousDrawn = moduleComponent.getModule().isDrawn();
                moduleComponent.getModule().setDrawn(!previousDrawn);
            }
        }
    }

    @Override
    public void handleRightClick() {
        super.handleRightClick();

        leftColumn.forEach(component -> {
            component.handleRightClick();
        });

        rightColumn.forEach(component -> {
            component.handleRightClick();
        });
    }

    @Override
    public void handleTabChange() {
        super.handleTabChange();

        // update our columns when the tab changes
        setPage(Page.MODULE);
        updateColumns();
    }

    @Override
    public void handleKeyPress(char typedCharacter, int key) {
        super.handleKeyPress(typedCharacter, key);

        leftColumn.forEach(component -> {
            component.handleKeyPress(typedCharacter, key);
        });

        rightColumn.forEach(component -> {
            component.handleKeyPress(typedCharacter, key);
        });

        if (binding && key != -1 && key != Keyboard.KEY_ESCAPE) {
            if (key == Keyboard.KEY_DELETE || key == Keyboard.KEY_CLEAR || key == Keyboard.KEY_BACK) {
                moduleComponent.getModule().setKey(Keyboard.KEY_NONE);
            }

            else {
                moduleComponent.getModule().setKey(key);
            }

            binding = false;
        }
    }

    @Override
    public float getOffset() {
        return 17;
    }

    public void updateColumns() {
        // clear our old columns
        leftColumn.clear();
        rightColumn.clear();

        // add all the modules to the columns
        AtomicBoolean left = new AtomicBoolean(true);

        if (page.equals(Page.MODULE)) {
            ModuleManager.getModules(module -> module.getCategory().equals(getTab().getObject())).forEach(module -> {
                if (left.get()) {
                    leftColumn.add(new ModuleComponent(this, module));
                }

                else {
                    rightColumn.add(new ModuleComponent(this, module));
                }

                left.set(!left.get());
            });
        }

        else if (page.equals(Page.SETTING)) {
            moduleComponent.getSettingComponents().forEach(settingComponent -> {
                if (left.get()) {
                    leftColumn.add(settingComponent);
                }

                else {
                    rightColumn.add(settingComponent);
                }

                left.set(!left.get());
            });
        }

        else if (page.equals(Page.SUBSETTING)) {
            settingComponent.getSettingComponents().forEach(subSettingComponent -> {
                if (left.get()) {
                    leftColumn.add(subSettingComponent);
                }

                else {
                    rightColumn.add(subSettingComponent);
                }

                left.set(!left.get());
            });
        }
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page in) {
        page = in;
    }

    public void setModuleComponent(ModuleComponent in) {
        moduleComponent = in;
    }

    public void setSettingComponent(SettingComponent in) {
        settingComponent = in;
    }

    public enum Page {
        MODULE, SETTING, SUBSETTING
    }
}