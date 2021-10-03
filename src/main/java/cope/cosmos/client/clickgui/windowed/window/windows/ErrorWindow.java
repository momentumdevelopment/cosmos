package cope.cosmos.client.clickgui.windowed.window.windows;

import cope.cosmos.client.clickgui.windowed.window.Window;
import cope.cosmos.util.client.ColorUtil;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import net.minecraft.util.math.Vec2f;

import java.awt.*;

public class ErrorWindow extends Window {

    private final String error;

    private int hoverAnimation = 0;

    public ErrorWindow(String name, String error, Vec2f position) {
        super(name, position, FontUtil.getStringWidth(error) + 6, 48);
        this.error = error;

        setExpandable(false);
    }

    @Override
    public void drawWindow() {
        super.drawWindow();

        // hover animation
        if (mouseOver(getPosition().x + (getWidth() / 2) - 15, getPosition().y + getBar() + FontUtil.getFontHeight() + 6, 30, 14) && hoverAnimation < 25)
            hoverAnimation += 5;

        else if (!mouseOver(getPosition().x + (getWidth() / 2) - 15, getPosition().y + getBar() + FontUtil.getFontHeight() + 6, 30, 14) && hoverAnimation > 0)
            hoverAnimation -= 5;

        FontUtil.drawStringWithShadow(error, getPosition().x + 3, getPosition().y + getBar() + 3, -1);

        RenderUtil.drawRect(getPosition().x + (getWidth() / 2) - 15, getPosition().y + getBar() + FontUtil.getFontHeight() + 6, 30, 14, new Color(ColorUtil.getPrimaryColor().getRed(), ColorUtil.getPrimaryColor().getGreen(), ColorUtil.getPrimaryColor().getBlue(), 110 + hoverAnimation));
        FontUtil.drawStringWithShadow("OK", getPosition().x + (getWidth() / 2) - (FontUtil.getStringWidth("OK") / 2), getPosition().y + getBar() + FontUtil.getFontHeight() + 9, -1);
    }

    @Override
    public void handleLeftClick() {
        super.handleLeftClick();

        if (mouseOver(getPosition().x + (getWidth() / 2) - 15, getPosition().y + getBar() + FontUtil.getFontHeight() + 6, 30, 14)) {
            getManager().purgeWindow(this);
        }
    }
}
