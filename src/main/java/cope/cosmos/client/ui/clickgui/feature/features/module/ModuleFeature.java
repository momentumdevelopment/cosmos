package cope.cosmos.client.ui.clickgui.feature.features.module;

import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.ui.clickgui.feature.ClickType;
import cope.cosmos.client.ui.clickgui.feature.DrawableFeature;
import cope.cosmos.client.ui.clickgui.feature.features.category.CategoryFrameFeature;
import cope.cosmos.client.ui.clickgui.feature.features.setting.BooleanFeature;
import cope.cosmos.client.ui.clickgui.feature.features.setting.SettingFeature;
import cope.cosmos.client.ui.util.Animation;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.string.ColorUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.glScaled;

/**
 * @author linustouchtips
 * @since 01/29/2022
 */
public class ModuleFeature extends DrawableFeature {

    // immutable module feature traits
    public static final int HEIGHT = 14;

    // parent frame
    private final CategoryFrameFeature categoryFrameFeature;
    private float featureHeight;

    // module associated with this feature
    private final Module module;

    // setting features under this module feature
    private final List<SettingFeature<?>> settingFeatures = new ArrayList<>();

    // offset for setting features (don't have the animation applied to them)
    private int settingFeatureOffset;

    // hover animation
    private final Animation animation = new Animation(200, false);
    private int hoverAnimation;

    // open/close state
    private boolean open;

    @SuppressWarnings("unchecked")
    public ModuleFeature(CategoryFrameFeature categoryFrameFeature, Module module) {
        this.categoryFrameFeature = categoryFrameFeature;
        this.module = module;

        if (module != null) {

            // add all module's settings
            module.getSettings().forEach(setting -> {
                if (setting.getValue() instanceof Boolean) {
                    settingFeatures.add(new BooleanFeature(this, (Setting<Boolean>) setting));
                }
            });
        }
    }

    @Override
    public void drawFeature() {
        // feature height
        featureHeight = (float) (categoryFrameFeature.getPosition().y + categoryFrameFeature.getTitle() + categoryFrameFeature.getFeatureOffset() + 2);

        if (module != null) {

            // hover alpha animation
            if (isMouseOver(categoryFrameFeature.getPosition().x, featureHeight, categoryFrameFeature.getWidth(), HEIGHT) && hoverAnimation < 25 && !categoryFrameFeature.isExpanding()) {
                hoverAnimation += 5;
            }

            else if (!isMouseOver(categoryFrameFeature.getPosition().x, featureHeight, categoryFrameFeature.getWidth(), HEIGHT) && hoverAnimation > 0) {
                hoverAnimation -= 5;
            }
        }

        // module background
        RenderUtil.drawRect(categoryFrameFeature.getPosition().x, featureHeight, categoryFrameFeature.getWidth(), HEIGHT, new Color(23 + hoverAnimation, 23 + hoverAnimation, 29 + hoverAnimation, 255));

        if (module != null) {

            // module name
            glScaled(0.8, 0.8, 0.8); {

                // scaled position
                float scaledX = (categoryFrameFeature.getPosition().x + 4) * 1.25F;
                float scaledY = (float) ((categoryFrameFeature.getPosition().y + categoryFrameFeature.getTitle() + categoryFrameFeature.getFeatureOffset() + 6.5F) * 1.25F);
                float scaledWidth = (categoryFrameFeature.getPosition().x + categoryFrameFeature.getWidth() - (FontUtil.getStringWidth("...") * 0.8F) - 3) * 1.25F;

                FontUtil.drawStringWithShadow(getModule().getName(), scaledX, scaledY, getModule().isEnabled() ? ColorUtil.getPrimaryColor().getRGB() : Color.WHITE.getRGB());
                FontUtil.drawStringWithShadow("...", scaledWidth, scaledY, new Color(255, 255, 255).getRGB());
            }

            glScaled(1.25, 1.25, 1.25);

            // offset to account for this feature
            categoryFrameFeature.addFeatureOffset(HEIGHT);

            // draw all setting features
            if (animation.getAnimationFactor() > 0) {
                settingFeatureOffset = (int) categoryFrameFeature.getFeatureOffset();
                settingFeatures.forEach(settingFeature -> {
                    if (settingFeature.getSetting().isVisible()) {
                        settingFeature.drawFeature();

                        // add offset with animation factor accounted for
                        settingFeatureOffset += settingFeature.getHeight();
                        categoryFrameFeature.addFeatureOffset(settingFeature.getHeight() * animation.getAnimationFactor());
                    }
                });
            }
        }
    }

    @Override
    public void onClick(ClickType in) {
        if (module != null) {

            // toggle the module if clicked
            if (isMouseOver(categoryFrameFeature.getPosition().x, featureHeight, categoryFrameFeature.getWidth(), HEIGHT)) {

                // module feature bounds
                float highestPoint = featureHeight;
                float lowestPoint = highestPoint + HEIGHT;

                // check if it's able to be interacted with
                if (highestPoint >= categoryFrameFeature.getPosition().y + categoryFrameFeature.getTitle() + 2 && lowestPoint <= categoryFrameFeature.getPosition().y + categoryFrameFeature.getTitle() + categoryFrameFeature.getHeight() + 2) {
                    if (in.equals(ClickType.LEFT)) {
                        module.toggle();
                    }

                    else if (in.equals(ClickType.RIGHT)) {
                        open = !open;
                        animation.setState(open);
                    }

                    // play a sound to make the user happy :)
                    getCosmos().getSoundManager().playSound("click");
                }
            }

            if (open) {
                settingFeatures.forEach(settingFeature -> {
                    settingFeature.onClick(in);
                });
            }
        }
    }

    @Override
    public void onType(char in) {
        if (module != null) {
            if (open) {
                settingFeatures.forEach(settingFeature -> {
                    settingFeature.onType(in);
                });
            }
        }
    }

    /**
     * Gets the parent category frame feature
     * @return The parent category frame feature
     */
    public CategoryFrameFeature getCategoryFrameFeature() {
        return categoryFrameFeature;
    }

    /**
     * Gets the module
     * @return The module
     */
    public Module getModule() {
        return module;
    }

    /**
     * Gets the offset for setting features
     * @return The offset for setting features
     */
    public int getSettingFeatureOffset() {
        return settingFeatureOffset;
    }
}
