package cope.cosmos.client.clickgui.ethius.taskbar;

import cope.cosmos.client.clickgui.ethius.EthiusGuiScreen;
import cope.cosmos.client.clickgui.ethius.element.Element;
import cope.cosmos.client.clickgui.ethius.element.SimpleElement;
import cope.cosmos.client.clickgui.ethius.window.Window;
import cope.cosmos.util.Wrapper;
import cope.cosmos.util.render.RenderUtil;

import java.util.HashMap;
import java.util.Iterator;

public class Taskbar extends Element implements Wrapper {

    private final EthiusGuiScreen mainScr;
    private HashMap<Window, SimpleElement> buttons = new HashMap<>();
    private SimpleElement bottomLeft = new SimpleElement().withText(() -> "R").withOnClick((e, f) -> getMainScr().reset());

    public Taskbar(EthiusGuiScreen mainScr) {
        this.mainScr = mainScr;
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        RenderUtil.drawRect(0, 0, width, height, 0xFF23232C);
        float xOffset = (float) mc.displayWidth / 2f - 13 * mainScr.getListElements().size();
        bottomLeft.withX(0).withY(mc.displayHeight - 20).withWidth(26).withHeight(20).draw(mouseX, mouseY);
        Iterator<Element> j = mainScr.getListElements().stream().filter(e -> e instanceof Window).sorted((o1, o2) -> (int) (o1.getOpenTime() - o2.getOpenTime())).iterator();
        while (j.hasNext()) {
            Window e = (Window) j.next();
            buttons.computeIfAbsent(e, w -> new SimpleElement().withText(() -> w.title.substring(0, 1)).withBackground(() -> mainScr.getListElements().indexOf(w) == 0 ? 0xff35353f : 0xff23232c));
            buttons.get(e).withX(xOffset).withY(mc.displayHeight - 20).withWidth(26).withHeight(20).draw(mouseX, mouseY);
            xOffset += 26f;
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (bottomLeft.mouseClicked(mouseX, mouseY, mouseButton)) {
            return true;
        }
        buttons.forEach((w, e) -> {
            if (mainScr.getListElements().contains(w)) {
                if (e.isWithin(mouseX, mouseY)) {
                    w.setOpenTime(System.currentTimeMillis());
                    w.setVisible(!w.isVisible());
                }
            }
        });
        return buttons.values().stream().anyMatch(e -> e.isWithin(mouseX, mouseY));
    }

    private EthiusGuiScreen getMainScr() {
        return mainScr;
    }

}
