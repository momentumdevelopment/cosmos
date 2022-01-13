package cope.cosmos.client.ui.clickgui.feature;

import cope.cosmos.client.ui.clickgui.element.SimpleElement;
import cope.cosmos.client.ui.clickgui.element.setting.BooleanElement;
import cope.cosmos.client.ui.clickgui.element.setting.EnumElement;
import cope.cosmos.client.ui.clickgui.element.setting.KeybindElement;
import cope.cosmos.client.ui.clickgui.element.setting.NumberElement;
import cope.cosmos.client.ui.clickgui.window.Window;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.render.RenderUtil;

@SuppressWarnings({"unchecked", "rawtypes"})
public class SettingWindowFeature extends WindowFeature {

    private final SimpleElement[] elements;

    public SettingWindowFeature(int width, int height, Setting<?> setting) {
        super(setting.name, width, height);
        this.tooltip = setting.description;
        elements = new SimpleElement[setting.getSubSettings().size() + 1];
        populateIdx(0, -1, setting);
        if (!setting.getSubSettings().isEmpty()) {
            for (int i = 0; i < setting.getSubSettings().size(); i++) {
                populateIdx(i + 1, i, setting);
            }
        }
    }

    private void populateIdx(int idx, int sidx, Setting<?> setting) {
        final Setting<?> s = sidx == -1 ? setting : setting.getSubSettings().get(sidx);
        if (s.getValue() instanceof Boolean) {
            elements[idx] = new BooleanElement((Setting<Boolean>) s);
        } else if (s.getValue() instanceof Integer || s.getValue() instanceof Double || s.getValue() instanceof Float) {
            elements[idx] = new NumberElement(s);
        } else if (s.getValue() instanceof Enum<?>) {
            elements[idx] = new EnumElement((Setting<Enum<?>>) s);
        } else if (s.getValue() instanceof Character) {
            elements[idx] = new KeybindElement((Setting<Character>) s);
        }
    }

    @Override public void keyTyped(char typedChar, int keyCode) {
        for (SimpleElement e: elements) {
            if (e != null) {
                e.keyTyped(typedChar, keyCode);
            }
        }
    }

    @Override public void draw(Window window, int mouseX, int mouseY) {
        float yOffset = 4;
        SimpleElement hovered = null;
        RenderUtil.drawRect(x, y, x + width, y + height, 0xFF17171D);
        for (SimpleElement e: elements) {
            if (e != null) {
                e.setX(x);
                e.setY(y + yOffset);
                e.setWidth(width);
                e.setHeight(Math.max(17, e.requestedHeight));
                e.draw(mouseX, mouseY);
                if (e.isWithin(mouseX, mouseY) && hovered == null) {
                    hovered = e;
                }
                yOffset += e.getHeight();
            }
        }
        if (hovered != null) {
            hovered.renderTooltip(window, mouseX, mouseY);
        }
    }

    @Override public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        for (SimpleElement e: elements) {
            if (e != null) {
                if (e.mouseClicked(mouseX, mouseY, mouseButton)) {
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override public boolean mouseReleased(int mouseX, int mouseY, int mouseButton) {
        for (SimpleElement e: elements) {
            if (e != null) {
                if (e.mouseReleased(mouseX, mouseY, mouseButton)) {
                    return true;
                }
            }
        }
        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }
}
