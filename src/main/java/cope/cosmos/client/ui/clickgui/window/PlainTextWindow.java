package cope.cosmos.client.ui.clickgui.window;

import cope.cosmos.client.ui.clickgui.ClickGuiScreen;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;

import java.util.List;

public class PlainTextWindow extends Window {

    private final String text;
    private List<String> wrapped;
    private float lastWidth;
    private float scrollY;

    public PlainTextWindow(String text, String name, ClickGuiScreen mainScr) {
        super(0, 0, 0, 0, name, mainScr);
        this.text = text;
    }

    @Override public void draw(int mouseX, int mouseY) {
        super.draw(mouseX, mouseY);
        if (wrapped == null || lastWidth != width) {
            wrapped = FontUtil.wrapWords(text, this.width - 10);
        }
        RenderUtil.scissor(x, y, width, height);
        float yOffset = scrollY + FontUtil.getFontHeight() + 4f;
        for (String line : wrapped) {
            FontUtil.drawStringWithShadow(line, x + 5, y + yOffset, 0xFFFFFF);
        }
        RenderUtil.endScissor();
        lastWidth = this.width;
    }

    @Override
    public void mouseScrolled(float mouseX, float mouseY, int amount) {
        if (isWithin(mouseX, mouseY)) {
            scrollY -= Math.signum(amount) * 10;
            if (scrollY < 0) {
                scrollY = 0;
            }
            if (wrapped != null) {
                if (scrollY > FontUtil.getFontHeight() * wrapped.size() - height + 10) {
                    scrollY = FontUtil.getFontHeight() * wrapped.size() - height;
                }
            }
        }
    }
}
