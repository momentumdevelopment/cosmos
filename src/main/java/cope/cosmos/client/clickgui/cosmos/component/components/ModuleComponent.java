package cope.cosmos.client.clickgui.cosmos.component.components;

import cope.cosmos.client.clickgui.cosmos.component.Component;
import cope.cosmos.client.clickgui.util.GUIUtil;
import cope.cosmos.client.clickgui.cosmos.window.windows.CategoryWindow;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.client.ClickGUI;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.Wrapper;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.world.SoundUtil;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

import java.awt.Color;

import static org.lwjgl.opengl.GL11.*;

@SuppressWarnings({"unchecked", "cast", "rawtypes"})
public class ModuleComponent extends Component implements Wrapper, GUIUtil {

    private Vec2f position = Vec2f.ZERO;

    private int hoverAnimation = 0;
    private float settingOffset = 0;
    private float settingAnimation = 0;

    private boolean open;

    public ModuleComponent(Module module, CategoryWindow categoryWindow) {
        super(module, categoryWindow);

        if (module != null) {
            module.getSettings().forEach(setting -> {
                if (setting.getValue() instanceof Boolean)
                    getSettingComponents().add(new BooleanComponent((Setting<Boolean>) setting, this));

                else if (setting.getValue() instanceof Enum<?>)
                    getSettingComponents().add(new EnumComponent((Setting<Enum<?>>) setting, this));

                else if (setting.getValue() instanceof Color)
                    getSettingComponents().add(new ColorComponent((Setting<Color>) setting, this));

                else if (setting.getValue() instanceof Double)
                    getSettingComponents().add(new NumberComponent(setting, this));

                else if (setting.getValue() instanceof Float)
                    getSettingComponents().add(new NumberComponent(setting, this));
            });

            // drawn and bind component, every module has this
            getSettingComponents().add(new DrawnComponent(this));
            getSettingComponents().add(new BindComponent(this));
        }

        open = false;
    }

    @Override
    public void drawComponent(Vec2f position) {
        setPosition(position);

        // module enabled animation
        settingAnimation = getModule() != null ? (float) MathHelper.clamp(getAnimation().getAnimationFactor(), 0, 1) : 0;

        if (getModule() != null) {
            // hover animation
            if (mouseOver(position.x, position.y, WIDTH, HEIGHT) && hoverAnimation < 25)
                hoverAnimation += 5;

            else if (!mouseOver(position.x, position.y, WIDTH, HEIGHT) && hoverAnimation > 0)
                hoverAnimation -= 5;
        }

        // module component
        RenderUtil.drawRect(position.x, position.y, WIDTH, HEIGHT, new Color(ClickGUI.INSTANCE.getBackgroundColor().getRed() + hoverAnimation, ClickGUI.INSTANCE.getBackgroundColor().getGreen() + hoverAnimation, ClickGUI.INSTANCE.getBackgroundColor().getBlue() + hoverAnimation));

        if (getModule() != null) {
            // module name
            glScaled(0.8, 0.8, 0.8); {
                float scaledX = (position.x + 4) * 1.25F;
                float scaledY = (position.y + 4.5F) * 1.25F;
                float scaledWidth = (position.x + WIDTH - (FontUtil.getStringWidth("...") * 0.8F) - 3) * 1.25F;

                FontUtil.drawStringWithShadow(getModule().getName(), scaledX, scaledY, getModule().isEnabled() ? new Color(ClickGUI.INSTANCE.getPrimaryColor().getRed(), ClickGUI.INSTANCE.getPrimaryColor().getGreen(), ClickGUI.INSTANCE.getPrimaryColor().getBlue()).getRGB() : new Color(255, 255, 255).getRGB());
                FontUtil.drawStringWithShadow("...", scaledWidth, scaledY, new Color(255, 255, 255).getRGB());
            }

            glScaled(1.25, 1.25, 1.25);

            /*
            if (getModule().getSettings().size() > 0) {
                glTranslated(position.x + WIDTH - 10, position.y + (HEIGHT / 2), 0);
                glRotated(90 * settingAnimation, 0, 0, 1);

                mc.getTextureManager().bindTexture(new ResourceLocation("cosmos", "textures/icons/dots.png"));
                Gui.drawModalRectWithCustomSizedTexture((int) (position.x + WIDTH - 10), (int) (position.y + 3), 0, 0, 8, 8, 8, 8);

                glRotated(-(90 * settingAnimation), 0, 0, 1);
                glTranslated(-position.x - WIDTH + 10, -position.y - (HEIGHT / 2), 0);
            }
             */

            if (settingAnimation > 0) {
                settingOffset = 0;
                getSettingComponents().forEach(settingComponent -> {
                    if (settingComponent.getSetting().isVisible() && !settingComponent.getSetting().hasParent()) {
                        float visibleY = position.y + HEIGHT + (settingOffset * settingComponent.getHeight());
                        settingComponent.drawSettingComponent(new Vec2f(position.x + BAR, visibleY));

                        if (settingComponent instanceof NumberComponent) {
                            settingOffset += 1.42857F;
                            getParentWindow().setModuleOffset(settingAnimation * 1.42857F);
                        }

                        else {
                            settingOffset += 1;
                            getParentWindow().setModuleOffset(settingAnimation);
                        }
                    }
                });
            }

            // bar
            RenderUtil.drawRect(position.x, position.y + HEIGHT, BAR, (settingOffset * HEIGHT), new Color(ClickGUI.INSTANCE.getPrimaryColor().getRed(), ClickGUI.INSTANCE.getPrimaryColor().getGreen(), ClickGUI.INSTANCE.getPrimaryColor().getBlue()));
        }
    }

    @Override
    public void handleLeftClick(int mouseX, int mouseY) {
        if (getModule() != null && mouseOver(position.x, position.y, WIDTH, HEIGHT)) {
            SoundUtil.clickSound();

            getModule().toggle();
        }

        if (open) {
            getSettingComponents().forEach(settingComponent -> {
                if (settingComponent.getSetting().isVisible()) {
                    settingComponent.handleLeftClick(mouseX, mouseY);
                }
            });
        }
    }

    @Override
    public void handleLeftDrag(int mouseX, int mouseY) {
        if (open) {
            getSettingComponents().forEach(settingComponent -> {
                if (settingComponent.getSetting().isVisible()) {
                    settingComponent.handleLeftDrag(mouseX, mouseY);
                }
            });
        }
    }

    @Override
    public void handleRightClick(int mouseX, int mouseY) {
        if (mouseOver(position.x, position.y, WIDTH, HEIGHT)) {
            SoundUtil.clickSound();

            open = !open;

            if (getModule() != null)
                getAnimation().setState(open);
        }

        if (open) {
            getSettingComponents().forEach(settingComponent -> {
                if (settingComponent.getSetting().isVisible()) {
                    settingComponent.handleRightClick(mouseX, mouseY);
                }
            });
        }
    }

    @Override
    public void handleKeyPress(char typedCharacter, int key) {
        if (open) {
            getSettingComponents().forEach(settingComponent -> {
                if (settingComponent.getSetting().isVisible()) {
                    settingComponent.handleKeyPress(typedCharacter, key);
                }
            });
        }
    }

    @Override
    public void handleScroll(int scroll) {
        if (open) {
            getSettingComponents().forEach(settingComponent -> {
                if (settingComponent.getSetting().isVisible()) {
                    settingComponent.handleScroll(scroll);
                }
            });
        }
    }

    public float getHeight() {
        return HEIGHT;
    }

    public void setPosition(Vec2f in) {
        position = in;
    }

    public Vec2f getPosition() {
        return position;
    }

    public void setSettingOffset(float in) {
        settingOffset += in;
    }
}
