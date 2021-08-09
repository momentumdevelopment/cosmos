package cope.cosmos.client.clickgui.windowed.window.windows;

import cope.cosmos.client.clickgui.windowed.window.TabbedWindow;
import cope.cosmos.client.clickgui.windowed.window.windows.configuration.Component;
import cope.cosmos.client.clickgui.windowed.window.windows.configuration.ModuleComponent;
import cope.cosmos.client.clickgui.windowed.window.windows.configuration.SettingComponent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.ModuleManager;
import cope.cosmos.util.render.RenderUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec2f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.lwjgl.opengl.GL11.*;

@SuppressWarnings("unused")
public class ConfigurationWindow extends TabbedWindow {

    private Page page = Page.MODULE;

    private final List<Component> leftColumn = new ArrayList<>();
    private final List<Component> rightColumn = new ArrayList<>();

    private float leftOffset;
    private float rightOffset;

    private ModuleComponent moduleComponent;
    private SettingComponent settingComponent;

    private int backAnimation = 0;

    public ConfigurationWindow(String name, Vec2f position) {
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

        glPushAttrib(GL_SCISSOR_BIT); {
            RenderUtil.scissor((int) (getPosition().x + 3), (int) (getPosition().y + getBar() + 3), (int) (getPosition().x + getWidth() - 15), (int) (getPosition().y + getHeight() - 3));
            glEnable(GL_SCISSOR_TEST);
        }

        // module background
        RenderUtil.drawBorderRect(getPosition().x + 4, getPosition().y + getBar() + getTab().getHeight() + 6, getWidth() - 20, getHeight() - getBar() - getTab().getHeight() - 10, new Color(0, 0, 0, 40), new Color(0, 0, 0, 70));

        float halfWidth = (getWidth() - 28) / 2;

        leftOffset = 0;
        leftColumn.forEach(component -> {
            component.drawComponent(new Vec2f(getPosition().x + 6, getPosition().y + getBar() + getTab().getHeight() + 8 + leftOffset + getScroll()), halfWidth);
            leftOffset += component.getHeight() + 4;
        });

        rightOffset = 0;
        rightColumn.forEach(component -> {
            component.drawComponent(new Vec2f(getPosition().x + 10 + halfWidth, getPosition().y + getBar() + getTab().getHeight() + 8 + rightOffset + getScroll()), halfWidth);
            rightOffset += component.getHeight() + 4;
        });

        glDisable(GL_SCISSOR_TEST);
        glPopAttrib();
    }

    @Override
    public void handleLeftClick() {
        super.handleLeftClick();

        if (mouseOver(getPosition().x + 2, getPosition().y + getBar() + 3, 9, getTab().getHeight())) {
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
    }

    @Override
    public void handleRightClick() {
        super.handleRightClick();

        try {
            leftColumn.forEach(component -> {
                component.handleRightClick();
            });

            rightColumn.forEach(component -> {
                component.handleRightClick();
            });
        } catch (Exception ignored) {

        }
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
                } else {
                    rightColumn.add(new ModuleComponent(this, module));
                }

                left.set(!left.get());
            });
        }

        else if (page.equals(Page.SETTING)) {
            moduleComponent.getSettingComponents().forEach(settingComponent -> {
                if (left.get()) {
                    leftColumn.add(settingComponent);
                } else {
                    rightColumn.add(settingComponent);
                }

                left.set(!left.get());
            });
        }

        else if (page.equals(Page.SUBSETTING)) {
            settingComponent.getSettingComponents().forEach(subSettingComponent -> {
                if (left.get()) {
                    leftColumn.add(subSettingComponent);
                } else {
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