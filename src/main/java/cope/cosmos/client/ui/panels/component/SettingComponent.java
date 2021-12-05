package cope.cosmos.client.ui.panels.component;

import cope.cosmos.client.ui.panels.component.components.*;
import cope.cosmos.client.ui.utility.GUIUtil;
import cope.cosmos.client.features.modules.client.ClickGUI;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.managment.managers.AnimationManager;
import cope.cosmos.utility.render.RenderUtil;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unchecked", "cast", "rawtypes"})
public abstract class SettingComponent<T> implements GUIUtil {

    public float WIDTH = 98;
    public final float HEIGHT = 14;
    public final float BAR = 2;

    private Vec2f position = Vec2f.ZERO;

    private final List<SettingComponent<?>> settingComponents = new ArrayList<>();
    private final ModuleComponent moduleComponent;
    private SettingComponent<?> settingComponent;

    private final Setting<T> setting;

    private final AnimationManager animationManager;
    private boolean open;

    private float settingOffset = 0;

    public SettingComponent(Setting<T> setting, ModuleComponent moduleComponent) {
        this.setting = setting;
        this.moduleComponent = moduleComponent;

        setting.getSubSettings().forEach(subSetting -> {
            SettingComponent<?> subSettingComponent = null;

            if (subSetting.getValue() instanceof Boolean)
                subSettingComponent = new BooleanComponent((Setting<Boolean>) subSetting, moduleComponent);

            else if (subSetting.getValue() instanceof Enum<?>)
                subSettingComponent = new EnumComponent((Setting<Enum<?>>) subSetting, moduleComponent);

            else if (subSetting.getValue() instanceof Color)
                subSettingComponent = new ColorComponent((Setting<Color>) subSetting, moduleComponent);

            else if (subSetting.getValue() instanceof Double)
                subSettingComponent = new NumberComponent(subSetting, moduleComponent);

            else if (subSetting.getValue() instanceof Float)
                subSettingComponent = new NumberComponent(subSetting, moduleComponent);

            if (subSettingComponent != null) {
                subSettingComponent.setSettingComponent(this);
                settingComponents.add(subSettingComponent);
            }
        });

        if (isSubSetting())
            WIDTH = 96;

        open = false;
        animationManager = new AnimationManager(200, false);
    }

    public void drawSettingComponent(Vec2f position) {
        float settingAnimation = (float) MathHelper.clamp(animationManager.getAnimationFactor(),0, 1);

        if (settingAnimation > 0) {
            settingOffset = 0;
            settingComponents.forEach(settingComponent -> {
                if (settingComponent.getSetting().isVisible()) {
                    settingComponent.drawSettingComponent(new Vec2f(position.x + BAR, position.y + HEIGHT + (settingOffset * HEIGHT)));

                    if (settingComponent instanceof NumberComponent) {
                        settingOffset += 1.42857F;
                        moduleComponent.setSettingOffset(settingAnimation * 1.42857F);
                        moduleComponent.getParentWindow().setModuleOffset(settingAnimation * 1.42857F);
                    }

                    else {
                        settingOffset += 1;
                        moduleComponent.setSettingOffset(settingAnimation);
                        moduleComponent.getParentWindow().setModuleOffset(settingAnimation);
                    }
                }
            });
        }

        RenderUtil.drawRect(position.x, position.y + HEIGHT, BAR, (settingOffset * HEIGHT), new Color(ClickGUI.INSTANCE.getPrimaryColor().getRed(), ClickGUI.INSTANCE.getPrimaryColor().getGreen(), ClickGUI.INSTANCE.getPrimaryColor().getBlue()));
    }

    public void handleLeftClick(int mouseX, int mouseY) {
        if (open) {
            settingComponents.forEach(settingComponent -> {
                if (settingComponent.getSetting().isVisible()) {
                    settingComponent.handleLeftClick(mouseX, mouseY);
                }
            });
        }
    }

    public void handleLeftDrag(int mouseX, int mouseY) {
        if (open) {
            settingComponents.forEach(settingComponent -> {
                if (settingComponent.getSetting().isVisible()) {
                    settingComponent.handleLeftDrag(mouseX, mouseY);
                }
            });
        }
    }

    public void handleRightClick(int mouseX, int mouseY) {
        if (mouseOver(getPosition().x, getPosition().y, WIDTH, HEIGHT)) {
            // SoundUtil.clickSound();

            open = !open;
            animationManager.setState(open);
        }

        if (open) {
            settingComponents.forEach(settingComponent -> {
                if (settingComponent.getSetting().isVisible()) {
                    settingComponent.handleRightClick(mouseX, mouseY);
                }
            });
        }
    }

    public void handleKeyPress(char typedCharacter, int key) {
        if (open) {
            settingComponents.forEach(settingComponent -> {
                if (settingComponent.getSetting().isVisible()) {
                    settingComponent.handleKeyPress(typedCharacter, key);
                }
            });
        }
    }

    public void handleScroll(int scroll) {
        if (open) {
            settingComponents.forEach(settingComponent -> {
                if (settingComponent.getSetting().isVisible()) {
                    settingComponent.handleScroll(scroll);
                }
            });
        }
    }

    public void setPosition(Vec2f in) {
        position = in;
    }

    public Vec2f getPosition() {
        return position;
    }

    public Setting<T> getSetting() {
        return setting;
    }

    public boolean isSubSetting() {
        return setting.hasParent();
    }

    public ModuleComponent getModuleComponent() {
        return moduleComponent;
    }

    public float getWidth() {
        return WIDTH;
    }

    public float getHeight() {
        return HEIGHT;
    }

    public void setSettingOffset(float in) {
        settingOffset += in;
    }

    public SettingComponent<?> getSettingComponent() {
        return settingComponent;
    }

    public void setSettingComponent(SettingComponent<?> in) {
        settingComponent = in;
    }
}
