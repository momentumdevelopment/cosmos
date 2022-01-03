package cope.cosmos.client.clickgui.ethius.feature;

import cope.cosmos.client.clickgui.ethius.element.SimpleElement;
import cope.cosmos.client.clickgui.ethius.window.Window;
import cope.cosmos.util.render.RenderUtil;
import net.minecraft.util.math.MathHelper;

public class TabbedWindowFeature extends WindowFeature {
    private final WindowFeature[] tabs;
    private int currentTabIdx;
    private final SimpleElement[] tabElements;
    public TabbedWindowFeatureType type = TabbedWindowFeatureType.TOP;
    public int backgroundColor = 0xff35353F;
    public int selectColor = 0xff232329;
    public int scrollOffset = 0;

    public TabbedWindowFeature(String name, int width, int height, WindowFeature... tabs) {
        super(name, width, height);
        this.tabs = tabs;
        tabElements = new SimpleElement[tabs.length];
        for (int i = 0; i < tabs.length; i++) {
            int finalI = i;
            tabElements[i] = new SimpleElement(0, 0, width, height).withText(() -> tabs[finalI].getName()).withOnClick((e, f) -> {
                if (f[2] == 0) this.currentTabIdx = finalI;
            }).withBackground(() -> 0xff1b1b20).withTooltip(tabs[finalI].tooltip);
        }
    }

    @Override public void draw(Window window, int mouseX, int mouseY) {
        switch (type) {
            case TOP: {
                float xOffset = 3;
                SimpleElement hovered = null;
                for (int i = 0; i < tabs.length; i++) {
                    if (i == currentTabIdx) {
                        this.tabElements[i].withBackground(() -> 0xff35353f);
                    } else {
                        this.tabElements[i].withBackground(() -> 0xff1b1b20);
                    }
                    this.tabElements[i].withX(this.x + xOffset).withY(this.y).withBackgroundType(SimpleElement.BackgroundType.TOP_ROUNDED).autoWidth(18f).withHeight(16).draw(mouseX, mouseY);
                    if (this.tabElements[i].isWithin(mouseX, mouseY) && hovered == null) {
                        hovered = this.tabElements[i];
                    }
                    xOffset += this.tabElements[i].getWidth() + 3;
                }
                if (this.tabs.length > 0) {
                    this.currentTabIdx = MathHelper.clamp(this.currentTabIdx, 0, this.tabs.length - 1);
                    final WindowFeature currentTab = this.tabs[this.currentTabIdx];
                    currentTab.x = this.x;
                    currentTab.y = this.y + 16;
                    currentTab.width = this.width;
                    currentTab.height = this.height - 33;
                    currentTab.draw(window, mouseX, mouseY);
                }
                if (hovered != null) {
                    hovered.renderTooltip(window, mouseX, mouseY);
                }
            }
            break;
            case LEFT: {
                float yOffset = 4;
                SimpleElement hovered = null;
                RenderUtil.drawRect(this.x, this.y, 112, this.height + 17, backgroundColor);
                for (int i = scrollOffset; i < MathHelper.clamp(scrollOffset + tabsViewable(), 0, tabElements.length); i++) {
                    if (i == currentTabIdx) {
                        this.tabElements[i].withBackground(() -> selectColor);
                    } else {
                        this.tabElements[i].withBackground(() -> backgroundColor);
                    }
                    this.tabElements[i].withX(this.x).withY(this.y + yOffset).withWidth(112).withTextAlignment(SimpleElement.TextAlignment.LEFT).withHeight(17).draw(mouseX, mouseY - scrollOffset * 17);
                    if (this.tabElements[i].isWithin(mouseX, mouseY) && hovered == null) {
                        hovered = this.tabElements[i];
                    }
                    yOffset += this.tabElements[i].getHeight();
                }
                if (this.tabs.length > 0) {
                    this.currentTabIdx = MathHelper.clamp(this.currentTabIdx, 0, this.tabs.length - 1);
                    final WindowFeature currentTab = this.tabs[this.currentTabIdx];
                    currentTab.x = this.x + 112;
                    currentTab.y = this.y;
                    currentTab.width = this.width - 114;
                    currentTab.height = this.height - 2;
                    currentTab.draw(window, mouseX, mouseY);
                }
                if (hovered != null) {
                    hovered.renderTooltip(window, mouseX, mouseY);
                }
            }
            break;
        }
    }

    @Override public void keyTyped(char typedChar, int keyCode) {
        if (this.tabs.length > 0) {
            this.tabs[this.currentTabIdx].keyTyped(typedChar, keyCode);
        }
    }

    @Override public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isWithin(mouseX, mouseY)) {
            for (int i = scrollOffset; i < MathHelper.clamp(scrollOffset + tabsViewable(), 0, tabElements.length); i++) {
                if (tabElements[i].mouseClicked(mouseX, mouseY, mouseButton)) {
                    break;
                }
            }
        }
        if (this.tabs.length > 0) {
            this.tabs[this.currentTabIdx].mouseClicked(mouseX, mouseY, mouseButton);
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (this.tabs.length > 0) {
            this.tabs[this.currentTabIdx].mouseReleased(mouseX, mouseY, mouseButton);
        }
        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override public void mouseScrolled(float mouseX, float mouseY, int scrollAmount) {
        if (this.type == TabbedWindowFeatureType.LEFT && this.tabs.length > 0) {
            if (this.isInTabsSection(mouseX, mouseY)) {
                this.scrollOffset -= Math.signum(scrollAmount);
                if (this.scrollOffset < 0) {
                    this.scrollOffset = 0;
                }
                if (scrollOffset + tabsViewable() > tabs.length) {
                    this.scrollOffset = tabs.length - tabsViewable();
                }
                scrollOffset = MathHelper.clamp(scrollOffset, 0, MathHelper.clamp(tabs.length - tabsViewable(), 0, Integer.MAX_VALUE));
            }
        }
        if (this.tabs.length > 0) {
            this.tabs[this.currentTabIdx].mouseScrolled(mouseX, mouseY, scrollAmount);
        }
    }

    private boolean isInTabsSection(float mouseX, float mouseY) {
        return this.type == TabbedWindowFeatureType.LEFT && mouseX > this.x && mouseX < this.x + 102 && mouseY > this.y && mouseY < this.y + this.height;
    }

    private int tabsViewable() {
        return (int) ((this.height + 13) / 17);
    }

    public enum TabbedWindowFeatureType {
        LEFT, TOP
    }
}
