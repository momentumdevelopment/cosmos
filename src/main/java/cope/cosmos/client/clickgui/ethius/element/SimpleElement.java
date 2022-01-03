package cope.cosmos.client.clickgui.ethius.element;

import cope.cosmos.client.clickgui.ethius.window.Window;
import cope.cosmos.util.Wrapper;
import cope.cosmos.util.client.ColorUtil;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import it.unimi.dsi.fastutil.objects.Object2FloatFunction;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class SimpleElement extends Element implements Wrapper {

    public static final Object2FloatFunction<SimpleElement> ELEMENT_WIDTH_GETTER = new Object2FloatFunction<SimpleElement>() {
        @Override
        public float getFloat(Object key) {
            if (!(key instanceof SimpleElement)) return 0;
            return ((SimpleElement) key).width;
        }
        @Override public float put(SimpleElement key, float value) {return 0;}@Override public float removeFloat(Object key) {return 0;}@Override public void defaultReturnValue(float rv) {}@Override public float defaultReturnValue() {return 0;}@Override public Float put(SimpleElement key, Float value) {return null;}@Override public Float get(Object key) {return null;}@Override public boolean containsKey(Object key) {return false;}@Override public Float remove(Object key) {return null;}@Override public int size() {return 0;}@Override public void clear() {}
    };
    public static final Object2FloatFunction<SimpleElement> ELEMENT_HEIGHT_GETTER = new Object2FloatFunction<SimpleElement>() {
        @Override
        public float getFloat(Object key) {
            if (!(key instanceof SimpleElement)) return 0;
            return ((SimpleElement) key).height;
        }
        @Override public float put(SimpleElement key, float value) {return 0;}@Override public float removeFloat(Object key) {return 0;}@Override public void defaultReturnValue(float rv) {}@Override public float defaultReturnValue() {return 0;}@Override public Float put(SimpleElement key, Float value) {return null;}@Override public Float get(Object key) {return null;}@Override public boolean containsKey(Object key) {return false;}@Override public Float remove(Object key) {return null;}@Override public int size() {return 0;}@Override public void clear() {}
    };

    private BiConsumer<SimpleElement, float[]> onClick = (e, m) -> {};
    private BiConsumer<SimpleElement, float[]> onRelease = (e, m) -> {};
    private BiConsumer<SimpleElement, int[]> onKey = (e, k) -> {};
    private BiConsumer<SimpleElement, float[]> additionalRendering = (b, m) -> {};
    private Supplier<Integer> textColor = () -> 0xffffffff;
    private TextAlignment textAlignment = TextAlignment.CENTER;
    private BackgroundType backgroundType = BackgroundType.RECTANGLE;
    protected Supplier<String> text = () -> "";
    private Supplier<Integer> background = () -> ColorUtil.getPrimaryAlphaColor(0xaa).darker().getRGB();
    public float requestedWidth;
    public float requestedHeight;
    private final WeakHashMap<String, Object> data = new WeakHashMap<>();
    public String tooltip;

    public SimpleElement(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.requestedWidth = -1;
        this.requestedHeight = -1;
    }

    public SimpleElement() {
        // init to -10000 to prevent artifacts
        this.x = -10000;
        this.y = -10000;
        this.width = 0;
        this.height = 0;
        this.requestedWidth = -1;
        this.requestedHeight = -1;
    }

    public void pushData(String key, Object value) {
        if (dataContains(key)) {
            data.replace(key, value);
            return;
        }
        data.put(key, value);
    }

    public <T> T getData(String key) {
        return (T) data.get(key);
    }

    public boolean dataContains(String key) {
        return data.containsKey(key);
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        switch (this.backgroundType) {
            case RECTANGLE: RenderUtil.drawRect(x, y, width, height, background.get()); break;
            case ALL_ROUNDED: RenderUtil.drawRoundedRect(x, y, width, height, 6f, new Color(background.get())); break;
            case TOP_ROUNDED: {
                RenderUtil.drawRoundedRect(x, y, width, height, 6f, new Color(background.get()));
                RenderUtil.drawRect(x, y + height * 0.5f, width, height * 0.5f, background.get());
            }
            break;
        }
        switch (textAlignment) {
            case LEFT: {
                final float vOffset = this.requestedHeight == -1 ? 0f : height / 2f - FontUtil.getFontHeight() / 2f + 2f;
                FontUtil.drawStringWithShadow(text.get(), x + getTextOffset(), y + vOffset, textColor.get());
            }
            break;
            case CENTER: {
                final float vOffset = this.requestedHeight == -1 ? FontUtil.getFontHeight() / 2f : height / 2f;
                FontUtil.drawCenteredStringWithShadow(text.get(), x + width / 2f, y + vOffset, textColor.get());
            }
            break;
            case RIGHT: {
                final float vOffset = this.requestedHeight == -1 ? 0f : height / 2f - FontUtil.getFontHeight() / 2f + 2f;
                final String e = text.get();
                FontUtil.drawStringWithShadow(e, x + width - getTextOffset() - FontUtil.getStringWidth(e), y + vOffset, textColor.get());
            }
            break;
        }
        additionalRendering.accept(this, new float[]{mouseX, mouseY});
    }

    @Override
    public void renderTooltip(Window window, int mouseX, int mouseY) {
        if (isWithin(mouseX, mouseY)) {
            window.info = tooltip;
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        final boolean bl = isWithin(mouseX, mouseY);
        if (bl) {
            onClick.accept(this, new float[]{mouseX, mouseY, mouseButton});
        }
        return bl;
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int mouseButton) {
        onRelease.accept(this, new float[]{mouseX, mouseY, mouseButton});
        return isWithin(mouseX, mouseY);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        this.onKey.accept(this, new int[]{typedChar, keyCode});
    }

    public float getTextOffset() {
        return MathHelper.clamp(width * 0.05357f, 2, 10);
    }

    public SimpleElement withX(float x) {
        this.x = x;
        return this;
    }

    public SimpleElement withY(float y) {
        this.y = y;
        return this;
    }

    public SimpleElement withOnKey(BiConsumer<SimpleElement, int[]> onKey) {
        this.onKey = onKey;
        return this;
    }

    public SimpleElement withTooltip(String tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    public SimpleElement withTextColor(Supplier<Integer> textColor) {
        this.textColor = textColor;
        return this;
    }

    // not to be used while in initialization phase
    public SimpleElement autoWidth(float extra) {
        this.width = FontUtil.getStringWidth(this.text.get()) + extra * 2f;
        return this;
    }

    public SimpleElement withWidth(float width) {
        this.width = width;
        return this;
    }

    public SimpleElement withHeight(float height) {
        this.height = height;
        return this;
    }

    public SimpleElement withOnClick(BiConsumer<SimpleElement, float[]> onClick) {
        this.onClick = onClick;
        return this;
    }

    public SimpleElement withOnRelease(BiConsumer<SimpleElement, float[]> onRelease) {
        this.onRelease = onRelease;
        return this;
    }

    public SimpleElement withTextAlignment(TextAlignment textAlignment) {
        this.textAlignment = textAlignment;
        return this;
    }

    public SimpleElement withAdditionalRendering(BiConsumer<SimpleElement, float[]> additionalRendering) {
        this.additionalRendering = additionalRendering;
        return this;
    }

    public SimpleElement withText(Supplier<String> text) {
        this.text = text;
        return this;
    }

    public SimpleElement withBackground(Supplier<Integer> background) {
        this.background = background;
        return this;
    }

    public SimpleElement withBackgroundType(BackgroundType backgroundType) {
        this.backgroundType = backgroundType;
        return this;
    }

    public enum TextAlignment {
        LEFT,
        CENTER,
        RIGHT
    }

    public enum BackgroundType {
        TOP_ROUNDED, RECTANGLE, ALL_ROUNDED
    }

}
