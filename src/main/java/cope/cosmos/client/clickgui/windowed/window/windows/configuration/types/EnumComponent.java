package cope.cosmos.client.clickgui.windowed.window.windows.configuration.types;

import cope.cosmos.client.clickgui.util.GUIUtil;
import cope.cosmos.client.clickgui.windowed.window.windows.configuration.SettingComponent;
import cope.cosmos.client.clickgui.windowed.window.windows.configuration.TypeComponent;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.Wrapper;
import cope.cosmos.util.client.ColorUtil;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.opengl.GL11.glScaled;

public class EnumComponent extends TypeComponent<Enum<?>> implements Wrapper {

    private boolean open;

    private float buttonOffset;

    private final List<ModeComponent> modeComponents = new ArrayList<>();

    private int hoverAnimation = 0;

    public EnumComponent(SettingComponent settingComponent, Setting<Enum<?>> setting) {
        super(settingComponent, setting);

        Enum<?> modeValue = setting.getValue();
        String[] modes = Arrays.stream(modeValue.getClass().getEnumConstants()).map(Enum::name).toArray(String[]::new);

        for (String mode : modes) {
            modeComponents.add(new ModeComponent(this, getSetting(), mode));
        }
    }

    @Override
    public void drawType(Vec2f position, float width, float height, float boundHeight) {
        setPosition(position);
        setWidth(width);
        setHeight(height);
        setBoundHeight(boundHeight);

        // hover animation
        if (mouseOver(position.x + 3, position.y + boundHeight - 17, width - 6, 13) && hoverAnimation < 25)
            hoverAnimation += 5;

        else if (!mouseOver(position.x + 3, position.y + boundHeight - 17, width - 6, 13) && hoverAnimation > 0)
            hoverAnimation -= 5;

        RenderUtil.drawBorderRect(position.x + 3, position.y + boundHeight - 17, width - 6, 13, new Color(hoverAnimation, hoverAnimation, hoverAnimation, 65), new Color(0, 0, 0, 100));

        glScaled(0.8, 0.8, 0.8); {
            float scaledX = (position.x + 6) * 1.25F;
            float scaledY = (position.y + boundHeight - 14) * 1.25F;
            FontUtil.drawStringWithShadow(Setting.formatEnum(getSetting().getValue()), scaledX, scaledY, -1);
        }

        glScaled(1.25, 1.25, 1.25);

        mc.getTextureManager().bindTexture(new ResourceLocation("cosmos", "textures/icons/dropdown.png"));
        Gui.drawModalRectWithCustomSizedTexture((int) (position.x + width - 19), (int) (position.y + boundHeight - 15), 0, 0, 13, 13, 13, 13);

        if (open) {
            buttonOffset = 1;
            modeComponents.forEach(modeComponent -> {
                modeComponent.drawComponent(new Vec2f(position.x + 3, position.y + boundHeight + buttonOffset - 4), width - 6);
                buttonOffset += 13;
            });
        }
    }

    @Override
    public void handleLeftClick() {
        if (mouseOver(getPosition().x + 3, getPosition().y + getBoundHeight() - 17, getWidth() - 6, 13)) {
            open = !open;
        }

        if (open) {
            modeComponents.forEach(modeComponent -> {
                modeComponent.handleLeftClick();
            });
        }
    }

    @Override
    public void handleRightClick() {

    }

    @Override
    public void handleKeyPress(char typedCharacter, int key) {

    }

    public int getMaxHeight() {
        return modeComponents.size() * 13;
    }

    public void setOpen(boolean in) {
        open = in;
    }

    public boolean isOpen() {
        return open;
    }

    public static class ModeComponent implements GUIUtil {

        private final EnumComponent component;
        private final Setting<Enum<?>> setting;
        private final String mode;

        private Vec2f position;
        private float width;

        private int hoverAnimation = 0;

        public ModeComponent(EnumComponent component, Setting<Enum<?>> setting, String mode) {
            this.component = component;
            this.setting = setting;
            this.mode = mode;
        }

        @SuppressWarnings("unchecked")
        public void drawComponent(Vec2f position, float width) {
            setPosition(position);
            setWidth(width);

            // hover animation
            if (mouseOver(position.x, position.y, width, 13) && hoverAnimation < 25)
                hoverAnimation += 5;

            else if (!mouseOver(position.x, position.y, width, 13) && hoverAnimation > 0)
                hoverAnimation -= 5;

            RenderUtil.drawRect(position.x, position.y, width, 13, setting.getValue().equals(Enum.valueOf(setting.getValue().getClass(), mode)) ? new Color(ColorUtil.getPrimaryColor().getRed(), ColorUtil.getPrimaryColor().getGreen(), ColorUtil.getPrimaryColor().getBlue(), 90) : new Color(hoverAnimation, hoverAnimation, hoverAnimation, 90));

            glScaled(0.8, 0.8, 0.8); {
                float scaledX = (position.x + 3) * 1.25F;
                float scaledY = (position.y + 3) * 1.25F;

                char firstChar = mode.charAt(0);
                String suffixChars = mode.split(String.valueOf(firstChar), 2)[1];
                String modeFormatted = String.valueOf(firstChar).toUpperCase() + suffixChars.toLowerCase();

                FontUtil.drawStringWithShadow(modeFormatted, scaledX, scaledY, -1);
            }

            glScaled(1.25, 1.25, 1.25);
        }

        @SuppressWarnings("unchecked")
        public void handleLeftClick() {
            if (mouseOver(position.x, position.y, width, 13)) {
                setting.setValue(Enum.valueOf(setting.getValue().getClass(), mode));
                component.setOpen(false);
            }
        }

        public void setPosition(Vec2f in) {
            position = in;
        }

        public void setWidth(float in) {
            width = in;
        }
    }
}
