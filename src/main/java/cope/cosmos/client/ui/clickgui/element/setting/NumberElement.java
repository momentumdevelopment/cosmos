package cope.cosmos.client.ui.clickgui.element.setting;

import cope.cosmos.client.ui.clickgui.element.SimpleElement;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.client.ColorUtil;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.system.MathUtil;

@SuppressWarnings("unchecked")
public class NumberElement<T extends Number> extends SimpleElement {
    public NumberElement(Setting<T> s) {
        super();
        this.tooltip = s.description;
        this.withText(s::getName);
        this.withBackground(() -> 0xff17171D);
        this.withTextAlignment(TextAlignment.LEFT);
        this.requestedHeight = 24f;
        this.withAdditionalRendering((e, mouse) -> {
            final float percent = (s.getValue().floatValue() - s.getMin().floatValue()) / (s.getMax().floatValue() - s.getMin().floatValue());
            final float valueWidth = FontUtil.getStringWidth(s.getValue().floatValue() + "");
            final float barWidth = e.getWidth() - e.getTextOffset() * 2f;
            final float barHeight = 1;
            FontUtil.drawStringWithShadow(s.getValue().floatValue() + "", e.getX() + e.getWidth() - valueWidth - e.getTextOffset(), e.getY() + getVOffset(TextAlignment.RIGHT), 0xffffffff);
            RenderUtil.drawRect(e.getX() + e.getTextOffset(), e.getY() + e.getHeight() - 4.5f, (barWidth - 6) * percent + 0.25f, barHeight, ColorUtil.getPrimaryColor().brighter());
            RenderUtil.drawPolygon((barWidth - 6) * percent + 3.75f + e.getX() + e.getTextOffset(), e.getY() + e.getHeight() - 4f, 3, 360, ColorUtil.getPrimaryColor());
            if (e.dataContains("Dragging")) {
                if (e.getData("Dragging")) {
                    final float mouseX = mouse[0];
                    final float mPercent = (mouseX - e.getTextOffset() - e.getX()) / (barWidth - 6);
                    final double f = MathUtil.roundOnSet(s.getMin().floatValue() + (s.getMax().floatValue() - s.getMin().floatValue()) * mPercent, s.getRoundingScale());
                    if (mPercent >= 0 && mPercent <= 1) {
                        if (s.getValue() instanceof Integer) {
                            ((Setting<Integer>) s).setValue((int) f);
                        } else if (s.getValue() instanceof Double) {
                            ((Setting<Double>) s).setValue(f);
                        } else if (s.getValue() instanceof Float) {
                            ((Setting<Float>) s).setValue((float) f);
                        }
                    }
                }
            }
        });
        this.withOnClick((e, f) -> {
            final float percent = (s.getValue().floatValue() - s.getMin().floatValue()) / (s.getMax().floatValue() - s.getMin().floatValue());
            final float barWidth = e.getWidth() - e.getTextOffset() * 2f;
            final float x = f[0];
            final float y = f[1];
            final float button = f[2];
            if (button == 0) {
                final float cx = (barWidth - 6) * percent + 3.75f + e.getX() + e.getTextOffset();
                final float cy = e.getY() + e.getHeight() - 4f;
                if (x > cx - 3 && x < cx + 3 && y > cy - 3 && y < cy + 3) {
                    e.pushData("Dragging", true);
                }
            }
        });
        this.withOnRelease((e, f) -> e.pushData("Dragging", false));
    }

}
