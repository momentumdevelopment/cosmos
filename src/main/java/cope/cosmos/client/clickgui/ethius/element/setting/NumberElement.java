package cope.cosmos.client.clickgui.ethius.element.setting;

import cope.cosmos.client.clickgui.ethius.element.SimpleElement;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.client.ColorUtil;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.system.MathUtil;

public class NumberElement<T extends Number> extends SimpleElement {
    public NumberElement(Setting<T> s) {
        super();
        this.tooltip = s.description;
        this.withText(s::getName);
        this.withBackground(() -> 0xff17171D);
        this.withTextAlignment(TextAlignment.LEFT);
        this.requestedHeight = 30f;
        this.withAdditionalRendering((e, mouse) -> {
            final float percent = (s.getValue().floatValue() - s.getMin().floatValue()) / (s.getMax().floatValue() - s.getMin().floatValue());
            final float nameWidth = FontUtil.getStringWidth(this.text.get()) + 4;
            final float valueWidth = FontUtil.getStringWidth(s.getValue().floatValue() + "");
            final float barWidth = e.getWidth() - 4f;
            final float barHeight = 1;
            FontUtil.drawStringWithShadow(s.getValue().floatValue() + "", e.getX() + e.getWidth() - valueWidth - e.getTextOffset(), e.getY() + e.getHeight() / 2f - FontUtil.getFontHeight() / 2f + 2f, 0xffffffff);
            RenderUtil.drawRect(e.getX() + 2f, e.getY() + e.getHeight() - 10.5f, (barWidth - 6) * percent + 0.25f, barHeight, ColorUtil.getPrimaryColor().brighter());
            RenderUtil.drawPolygon((barWidth - 6) * percent + 3.75f + e.getX() + 2f, e.getY() + e.getHeight() - 10f, 3, 360, ColorUtil.getPrimaryColor());
            if (e.dataContains("Dragging")) {
                if (e.getData("Dragging")) {
                    final float mouseX = mouse[0];
                    final float mPercent = (mouseX - 2f - e.getX()) / (barWidth - 6);
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
            final float barWidth = e.getWidth() - 4f;
            final float x = f[0];
            final float y = f[1];
            final float button = f[2];
            if (button == 0) {
                final float cx = (barWidth - 6) * percent + 3.75f + e.getX() + 2f;
                final float cy = e.getY() + e.getHeight() / 2f;
                if (x > cx - 3 && x < cx + 3 && y > cy - 3 && y < cy + 3) {
                    e.pushData("Dragging", true);
                }
            }
        });
        this.withOnRelease((e, f) -> e.pushData("Dragging", false));
    }

}
