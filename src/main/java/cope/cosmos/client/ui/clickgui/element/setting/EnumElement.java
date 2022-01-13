package cope.cosmos.client.ui.clickgui.element.setting;

import cope.cosmos.client.ui.clickgui.element.DropDownMenu;
import cope.cosmos.client.ui.clickgui.element.SimpleElement;
import cope.cosmos.client.ui.clickgui.window.Window;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.client.StringFormatter;
import cope.cosmos.util.render.FontUtil;

import java.util.Arrays;
import java.util.stream.Collectors;

public class EnumElement extends SimpleElement {
    private final DropDownMenu menu;
    private boolean menuOpen;

    public EnumElement(Setting<Enum<?>> s) {
        super();
        this.tooltip = s.description;
        menu = new DropDownMenu(Arrays.stream(s.getValue().getDeclaringClass().getEnumConstants()).map(e -> new SimpleElement().withOnClick((el, f) -> s.setValue(e)).withText(() -> StringFormatter.formatEnum(e)).withTooltip("Set " + s.name + " to " + e.toString()).autoWidth(2f).withBackground(() -> e.equals(s.getValue()) ? 0xff232329 : 0xff17171D).withTextAlignment(TextAlignment.LEFT)).collect(Collectors.toList()));
        this.withText(s::getName);
        this.withBackground(() -> 0xff17171D);
        this.withAdditionalRendering((e, f) -> {
            final String str = "Selected: " + s.getValue().toString();
            FontUtil.drawStringWithShadow(str, this.x + this.width - FontUtil.getStringWidth(str) - e.getTextOffset(), this.y + this.height / 2f - getVOffset(e.getTextAlignment()), 0xffffffff);
        });
        this.withOnClick((e, f) -> {
            final String str = "Selected: " + s.getValue().toString();
            final float x = f[0], y = f[1];
            final float txtStart = this.x + this.width - FontUtil.getStringWidth(str) - e.getTextOffset();
            if (x > txtStart && x < txtStart + FontUtil.getStringWidth(str) && y > this.y + this.height / 2f - FontUtil.getFontHeight() / 2f && y < this.y + this.height / 2f + getVOffset(TextAlignment.RIGHT)) {
                menuOpen = !menuOpen;
            }
        });
        this.withTextAlignment(TextAlignment.LEFT);
    }

    @Override public void renderTooltip(Window window, int x, int y) {
        super.renderTooltip(window, x, y);
        if (menuOpen) {
            menu.withX(this.x + this.width - menu.getWidth() - this.getTextOffset()).withY(this.y + this.height / 2f + FontUtil.getFontHeight() / 2f + 2f).draw(x, y);
        }
    }

    @Override public boolean isWithin(float x, float y) {
        return (menuOpen && menu.isWithin(x, y)) || super.isWithin(x, y);
    }

    @Override public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (menuOpen) {
            if (menu.mouseClicked(mouseX, mouseY, mouseButton)) {
                return true;
            } else {
                menuOpen = false;
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
