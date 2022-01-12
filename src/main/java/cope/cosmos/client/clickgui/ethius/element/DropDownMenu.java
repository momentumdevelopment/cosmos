package cope.cosmos.client.clickgui.ethius.element;

import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.system.MathUtil;

import java.util.List;

public class DropDownMenu extends SimpleElement {
    private final List<SimpleElement> elements;

    public DropDownMenu(List<SimpleElement> values) {
        super();
        this.elements = values;
        this.width = MathUtil.maxOfFloat(elements, SimpleElement.ELEMENT_WIDTH_GETTER);
        this.height = values.size() * 15;
        this.withOnClick((e, f) -> {
            for (SimpleElement element: elements) {
                element.mouseClicked((int) f[0], (int) f[1], (int) f[2]);
            }
        });
    }

    @Override public void draw(int mouseX, int mouseY) {
        float yOffset = 0;
        for (SimpleElement element : elements) {
            element.x = x;
            element.y = y + yOffset;
            element.width = width;
            element.height = 15;
            element.draw(mouseX, mouseY);
            yOffset += 15;
        }
        RenderUtil.drawBorder(x, y, width, height, 0xff232329);
    }
}
