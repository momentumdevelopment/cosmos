package cope.cosmos.client.ui.panels.panel;

import cope.cosmos.client.ui.panels.component.Component;
import cope.cosmos.client.ui.panels.component.SettingComponent;
import cope.cosmos.client.ui.utility.GUIUtil;
import cope.cosmos.utility.IUtility;
import cope.cosmos.utility.render.FontUtil;
import cope.cosmos.utility.render.RenderUtil;
import net.minecraft.util.math.Vec2f;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

@SuppressWarnings("unused")
public class SettingPanel extends Panel implements IUtility, GUIUtil {

    private Vec2f position = Vec2f.ZERO;

    private final Component parent;

    private int settingOffset;
    private float settingScroll;

    private final List<SettingComponent<?>> settingComponents;

    public SettingPanel(Component parent, List<SettingComponent<?>> settingComponents) {
        this.parent = parent;
        this.settingComponents = settingComponents;
    }

    @Override
    public void drawPanel(Vec2f position) {
        setPosition(position);

        glPushAttrib(GL_SCISSOR_BIT); {
            RenderUtil.scissor((int) position.x, (int) (position.y + TITLE + BAR), (int) (position.x + WIDTH), (int) (position.y + TITLE + BAR + HEIGHT));
            glEnable(GL_SCISSOR_TEST);
        }

        RenderUtil.drawRoundedRect(position.x, position.y, WIDTH, HEIGHT, 10, Color.BLACK);
        RenderUtil.drawHalfRoundedRect(position.x, position.y, WIDTH, TITLE, 10, Color.BLACK);

        // window title
        glScaled(1.05, 1.05, 1.05); {
            float scaledX = (position.x + 7) * 0.95238095F;
            float scaledY = (position.y + TITLE - 14) * 0.95238095F;
            FontUtil.drawStringWithShadow(parent.getModule().getName(), scaledX, scaledY, -1);
        }

        glScaled(0.95238095, 0.95238095, 0.95238095);

        // bar
        RenderUtil.drawRect(position.x, position.y + TITLE, WIDTH, BAR, Color.BLACK);

        settingOffset = 0;
        settingComponents.forEach(settingComponent -> {
            settingComponent.drawSettingComponent(new Vec2f(position.x, position.y + BAR + TITLE + (settingComponent.getHeight() * settingOffset) + settingScroll));
            settingOffset++;
        });

        glDisable(GL_SCISSOR_TEST);
        glPopAttrib();
    }

    @Override
    public void handleLeftClick(int mouseX, int mouseY) {
        settingComponents.forEach(settingComponent -> {
            settingComponent.handleLeftClick(mouseX, mouseY);
        });
    }

    @Override
    public void handleRightClick(int mouseX, int mouseY) {
        settingComponents.forEach(settingComponent -> {
            settingComponent.handleRightClick(mouseX, mouseY);
        });
    }

    @Override
    public void handleKeyPress(char typedCharacter, int key) {
        if (key == Keyboard.KEY_ESCAPE) {
            parent.getAnimation().setStateHard(false);

            if (mc.entityRenderer.isShaderActive())
                mc.entityRenderer.getShaderGroup().deleteShaderGroup();
        }

        settingComponents.forEach(settingComponent -> {
            settingComponent.handleKeyPress(typedCharacter, key);
        });
    }

    @Override
    public void handleScroll(int scroll) {
        
    }

    public void setPosition(Vec2f in) {
        position = in;
    }

    public Vec2f getPosition() {
        return position;
    }
}
