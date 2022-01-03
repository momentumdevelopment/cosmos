package cope.cosmos.client.clickgui.ethius.element.setting;

import cope.cosmos.client.clickgui.ethius.Animations;
import cope.cosmos.client.clickgui.ethius.element.SimpleElement;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.client.ColorUtil;
import cope.cosmos.util.render.RenderUtil;
import net.minecraft.util.math.MathHelper;

public class BooleanElement extends SimpleElement {
    public BooleanElement(Setting<Boolean> s) {
        super();
        this.tooltip = s.description;
        this.withTextAlignment(TextAlignment.LEFT);
        this.withText(s::getName);
        this.withBackground(() -> 0xff17171D);
        this.withAdditionalRendering((e, f) -> {
            final float width_height = e.getHeight() - 4;
            final float x = e.getX() + e.getWidth() - e.getTextOffset() - width_height;
            final float y = e.getY() + 2;
            RenderUtil.drawRect(x, y, width_height, width_height, 0xff35353f);
            final long lastActionTime = e.dataContains("LastActionTime") ? e.getData("LastActionTime") : -1;
            if (s.getValue() || System.currentTimeMillis() - lastActionTime < 200) {
                float animation = Animations.getDecelerateAnimation(200, (long) MathHelper.clamp(System.currentTimeMillis() - lastActionTime, 0, 200));
                if (!s.getValue()) {
                    animation = 1 - animation;
                }
                if (lastActionTime == -1) {
                    animation = 1;
                }
                final float xv1 = x - (width_height / 2f - 1) * animation + (width_height / 2f);
                final float yv1 = y - (width_height / 2f - 1) * animation + (width_height / 2f);
                final float xv2 = x + (width_height / 2f - 1) * animation + (width_height / 2f);
                final float yv2 = y + (width_height / 2f - 1) * animation + (width_height / 2f);
                RenderUtil.drawLine2d(xv1, yv1, xv2, yv2, 1f, ColorUtil.getPrimaryColor().brighter());
                RenderUtil.drawLine2d(xv2, yv1, xv1, yv2, 1f, ColorUtil.getPrimaryColor().brighter());
            }
        });
        this.withOnClick((e, f) -> {
            e.pushData("LastActionTime", System.currentTimeMillis());
            s.setValue(!s.getValue());
        });
    }
}
