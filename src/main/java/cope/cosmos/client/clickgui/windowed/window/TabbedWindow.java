package cope.cosmos.client.clickgui.windowed.window;

import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import net.minecraft.util.math.Vec2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

@SuppressWarnings("unused")
public class TabbedWindow extends Window {

    private final List<Tab<?>> tabs = new ArrayList<>();
    private static Tab<?> tab;

    private float offset;
    private final float height = 13;

    public TabbedWindow(String name, Vec2f position) {
        super(name, position);
    }

    @Override
    public void drawWindow() {
        super.drawWindow();

        offset = 0;
        tabs.forEach(tabComponent -> {
            tabComponent.drawTab(new Vec2f(getPosition().x + offset + 4, getPosition().y + getBar() + 4), FontUtil.getStringWidth(tabComponent.getName()) + 3, height);
            offset += tabComponent.getWidth() + 2;
        });
    }

    @Override
    public void handleLeftClick() {
        super.handleLeftClick();

        // update the current tab
        tabs.forEach(tabComponent -> {
            if (mouseOver(tabComponent.getPosition().x, tabComponent.getPosition().y, tabComponent.getWidth(), tabComponent.getHeight())) {
                setTab(tabComponent);
            }
        });
    }

    public List<Tab<?>> getTabs() {
        return tabs;
    }

    public void setTab(Tab<?> in) {
        tab = in;
    }

    public Tab<?> getTab() {
        return tab;
    }

    public static class Tab<T> {

        private final String name;
        private T object;

        private Vec2f position;
        private float width;
        private float height;

        public Tab(String name, T object) {
            this.name = name;
            this.object = object;
        }

        public void drawTab(Vec2f position, float width, float height) {
            setPosition(position);
            setWidth(width);
            setHeight(height);

            glPushMatrix();

            // tab background
            RenderUtil.drawRect(position.x, position.y, width, height, tab == this ? new Color(23, 23, 23, 70) : new Color(0, 0, 0, 70));

            // tab outline
            RenderUtil.drawRect(position.x, position.y - 0.5F, width, 1, new Color(0, 0, 0, 70));
            RenderUtil.drawRect(position.x, position.y + height - (tab == this ? 1.5F : 0.5F), width, tab == this ? 2 : 1, tab == this ? new Color(255, 0, 0, 130) : new Color(0, 0, 0, 70));
            RenderUtil.drawRect(position.x - 0.5F, position.y, 1, height, new Color(0, 0, 0, 70));
            RenderUtil.drawRect(position.x + width - 0.5F, position.y, 1, height, new Color(0, 0, 0, 70));

            // tab text
            glScaled(0.8, 0.8, 0.8); {
                float scaledX = (position.x + 4.5F) * 1.25F;
                float scaledY = (position.y + 3) * 1.25F;
                FontUtil.drawStringWithShadow(name, scaledX, scaledY, -1);
            }

            glScaled(1.25, 1.25, 1.25);

            glPopMatrix();
        }

        public String getName() {
            return name;
        }

        public void setObject(T in) {
            object = in;
        }

        public T getObject() {
            return object;
        }

        public void setPosition(Vec2f in) {
            position = in;
        }

        public Vec2f getPosition() {
            return position;
        }

        public void setWidth(float in) {
            width = in;
        }

        public float getWidth() {
            return width;
        }

        public void setHeight(float in) {
            height = in;
        }

        public float getHeight() {
            return height;
        }
    }
}
