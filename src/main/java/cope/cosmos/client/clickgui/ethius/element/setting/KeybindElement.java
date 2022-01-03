package cope.cosmos.client.clickgui.ethius.element.setting;

import cope.cosmos.client.clickgui.ethius.element.SimpleElement;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.render.FontUtil;
import org.lwjgl.input.Keyboard;

public class KeybindElement extends SimpleElement {
    public KeybindElement(Setting<Character> s) {
        super();
        this.tooltip = s.description;
        this.withText(s::getName);
        this.withTextAlignment(TextAlignment.LEFT);
        this.withBackground(() -> 0xff17171D);
        this.withOnKey((e, k) -> {
            if (e.dataContains("Polling")) {
                if ((boolean) e.getData("Polling")) {
                    s.getModule().setKey(k[1]);
                    e.pushData("Polling", false);
                }
            }
        });
        this.withOnClick((e, mouse) -> {
            if (mouse[2] == 2) {
                s.getModule().setKey(Keyboard.KEY_NONE);
            } else if (mouse[2] == 0) {
                e.pushData("ClickTime", System.currentTimeMillis());
                e.pushData("Polling", true);
            }
        });
        this.withAdditionalRendering((e, mouse) -> {
            String text = null;
            if (e.dataContains("Polling")) {
                if (e.getData("Polling")) {
                    final long time = (System.currentTimeMillis() - (long) e.getData("ClickTime")) % 1600;
                    if (time < 400) {
                        text = "Waiting";
                    } else if (time < 800) {
                        text = "Waiting.";
                    } else if (time < 1200) {
                        text = "Waiting..";
                    } else {
                        text = "Waiting...";
                    }
                }
            }
            if (text == null) {
                text = Keyboard.getKeyName(s.getModule().getKey());
            }
            FontUtil.drawStringWithShadow(text, e.getX() + e.getWidth() - e.getTextOffset() - FontUtil.getStringWidth(text), e.getY() + e.getHeight() / 2 - FontUtil.getFontHeight() / 2f + 2f, 0xFFFFFF);
        });
    }
}
