package cope.cosmos.client.clickgui.cosmos.window.windows;

import cope.cosmos.client.clickgui.cosmos.component.components.ModuleComponent;
import cope.cosmos.client.clickgui.cosmos.window.Window;
import cope.cosmos.client.clickgui.util.GUIUtil;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.client.ClickGUI;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.AnimationManager;
import cope.cosmos.client.manager.managers.ModuleManager;
import cope.cosmos.util.Wrapper;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.system.Timer;
import cope.cosmos.util.system.Timer.Format;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

@SuppressWarnings("unused")
public class CategoryWindow extends Window implements Wrapper, GUIUtil {

    private final float WIDTH = 100;
    private final float TITLE = 19;
    private final float BAR = 2;
    private float SCISSOR_HEIGHT;
    private float HEIGHT;

    private final Category category;

    private Vec2f position;
    private Vec2f previousMousePosition = Vec2f.ZERO;

    private final List<ModuleComponent> moduleComponents = new ArrayList<>();
    private float moduleComponentOffset;
    private float moduleOffset;

    private float scrollSpeed = 0;
    private float moduleScroll;
    private final Timer scrollTimer = new Timer();

    private final AnimationManager animationManager;
    private boolean open;

    public CategoryWindow(Vec2f position, Category category) {
        this.position = position;
        this.category = category;

        // add all modules in this category
        ModuleManager.getModules(module -> module.getCategory().equals(category)).forEach(module -> {
            moduleComponents.add(new ModuleComponent(module, this));
        });

        HEIGHT = TITLE + (moduleComponents.size() * 14) + 3;

        // needs to be less than height to keep the rounded edges at the bottom of the window
        SCISSOR_HEIGHT = HEIGHT - 7;

        // filler components
        for (int i = 0; i <= 100; i++) {
            moduleComponents.add(new ModuleComponent(null, this));
        }

        // windows should be open by default
        open = false;
        animationManager = new AnimationManager(200, false);

        // register to event bus
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void drawWindow() {
        // dragging windows
        if (mouseOver(position.x, position.y, WIDTH, TITLE) && getGUI().getMouse().isLeftHeld())
            setDragging(true);

        // expanding windows
        if (mouseOver(position.x, position.y + TITLE + HEIGHT, WIDTH, 10) && getGUI().getMouse().isLeftHeld())
            setExpanding(true);

        if (isDragging())
            setPosition(new Vec2f(position.x + (getGUI().getMouse().getMousePosition().x - previousMousePosition.x), position.y + (getGUI().getMouse().getMousePosition().y - previousMousePosition.y)));

        if (isExpanding())
            HEIGHT = getGUI().getMouse().getMousePosition().y - position.y;

        SCISSOR_HEIGHT = HEIGHT - 7;

        glPushMatrix();

        float categoryAnimation = (float) MathHelper.clamp(animationManager.getAnimationFactor(), 0, 1);

        // window
        RenderUtil.drawRoundedRect(position.x, position.y, WIDTH, TITLE + (HEIGHT * categoryAnimation), 10, new Color(ClickGUI.INSTANCE.getBackgroundColor().getRed(), ClickGUI.INSTANCE.getBackgroundColor().getGreen(), ClickGUI.INSTANCE.getBackgroundColor().getBlue()));
        RenderUtil.drawHalfRoundedRect(position.x, position.y, WIDTH, TITLE, 10, new Color(ClickGUI.INSTANCE.getAccentColor().getRed(), ClickGUI.INSTANCE.getAccentColor().getGreen(), ClickGUI.INSTANCE.getAccentColor().getBlue()));

        // window title
        glScaled(1.05, 1.05, 1.05); {
            float scaledX = (position.x + (((position.x + WIDTH) - position.x) / 2) - (FontUtil.getStringWidth(Setting.formatEnum(category)) / 2F) - 2) * 0.95238095F;
            float scaledY = (position.y + TITLE - 14) * 0.95238095F;
            FontUtil.drawStringWithShadow(Setting.formatEnum(category), scaledX, scaledY, new Color(255, 255, 255).getRGB());
        }

        glScaled(0.95238095, 0.95238095, 0.95238095);

        // bar
        RenderUtil.drawRect(position.x, position.y + TITLE, WIDTH, BAR, new Color(ClickGUI.INSTANCE.getPrimaryColor().getRed(), ClickGUI.INSTANCE.getPrimaryColor().getGreen(), ClickGUI.INSTANCE.getPrimaryColor().getBlue()));

        if (categoryAnimation > 0) {
            // scissor for scrolling
            glPushAttrib(GL_SCISSOR_BIT); {
                RenderUtil.scissor((int) position.x, (int) (position.y + TITLE + BAR), (int) (position.x + WIDTH), (int) (position.y + TITLE + BAR + (SCISSOR_HEIGHT * categoryAnimation)));
                glEnable(GL_SCISSOR_TEST);
            }

            moduleOffset = 0;
            moduleComponentOffset = 0;
            moduleComponents.forEach(moduleComponent -> {
                float visibleY = position.y + BAR + TITLE + (moduleComponent.getHeight() * moduleOffset) - moduleScroll;
                moduleComponent.drawComponent(new Vec2f(position.x, visibleY));

                if (moduleComponent.getModule() != null)
                    moduleComponentOffset++;

                moduleOffset++;
            });

            glDisable(GL_SCISSOR_TEST);
            glPopAttrib();
        }

        glPopMatrix();

        previousMousePosition = new Vec2f(getGUI().getMouse().getMousePosition().x, getGUI().getMouse().getMousePosition().y);
    }

    @Override
    public void handleLeftClick(int mouseX, int mouseY) {
        if (open) {
            moduleComponents.forEach(moduleComponent -> {
                if (moduleComponent.getModule() != null) {
                    moduleComponent.handleLeftClick(mouseX, mouseY);
                }
            });
        }
    }

    @Override
    public void handleLeftDrag(int mouseX, int mouseY) {
        if (open) {
            moduleComponents.forEach(moduleComponent -> {
                if (moduleComponent.getModule() != null) {
                    moduleComponent.handleLeftDrag(mouseX, mouseY);
                }
            });
        }
    }

    @Override
    public void handleRightClick(int mouseX, int mouseY) {
        if (mouseOver(position.x, position.y, WIDTH, TITLE)) {
            // SoundUtil.clickSound();

            open = !open;
            animationManager.setState(open);
        }

        if (open) {
            moduleComponents.forEach(moduleComponent -> {
                if (moduleComponent.getModule() != null) {
                    moduleComponent.handleRightClick(mouseX, mouseY);
                }
            });
        }
    }

    @Override
    public void handleKeyPress(char typedCharacter, int key) {
        if (open) {
            moduleComponents.forEach(moduleComponent -> {
                if (moduleComponent.getModule() != null) {
                    moduleComponent.handleKeyPress(typedCharacter, key);
                }
            });
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent tickEvent) {
        float upperBound = Math.max((position.y + TITLE + BAR + ((moduleComponentOffset - 2) * 14)) - HEIGHT, 0.01F);

        moduleScroll += scrollSpeed;
        scrollSpeed *= 0.5;

        if (scrollTimer.passed(100, Format.SYSTEM)) {
            if (moduleScroll < 0)
                scrollSpeed = moduleScroll * -0.25F;
            else if (moduleScroll > upperBound)
                scrollSpeed = (moduleScroll - upperBound) * -0.25F;
        }
    }

    @Override
    public void handleScroll(int scroll) {
        if (open) {
            if (Mouse.getEventDWheel() != 0) {
                scrollTimer.reset();
                scrollSpeed -= scroll * 0.05F;
            }

            moduleComponents.forEach(moduleComponent -> {
                if (moduleComponent.getModule() != null) {
                    moduleComponent.handleScroll(scroll);
                }
            });
        }
    }

    public List<ModuleComponent> getModuleComponents() {
        return moduleComponents;
    }

    public float getHeight() {
        return HEIGHT + TITLE + BAR;
    }

    public float getUpperHeight() {
        return position.y + TITLE + BAR;
    }

    public float getLowerHeight() {
        return position.y + TITLE + BAR + SCISSOR_HEIGHT;
    }

    public float getCategoryAnimation() {
        return (float) MathHelper.clamp(animationManager.getAnimationFactor(), 0, 1);
    }

    public float getWidth() {
        return WIDTH;
    }

    public float getTitle() {
        return TITLE;
    }

    public void setModuleOffset(float in) {
        moduleOffset += in;
        moduleComponentOffset += in;
    }

    public float getModuleScroll() {
        return moduleScroll;
    }

    public void setModuleScroll(int in) {
        moduleScroll = in;
    }

    public Vec2f getPosition() {
        return position;
    }

    public void setPosition(Vec2f in) {
        position = in;
    }

    public Category getCategory() {
        return category;
    }

    public AnimationManager getAnimation() {
        return animationManager;
    }

    public void setOpen(boolean in) {
        open = in;
    }

    public void setHeight(float in) {
        HEIGHT = in;
    }
}
