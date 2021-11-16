package cope.cosmos.client.clickgui.windowed.window;

import cope.cosmos.client.clickgui.util.BlurUtil;
import cope.cosmos.client.clickgui.util.GUIUtil;
import cope.cosmos.client.features.modules.client.ClickGUI;
import cope.cosmos.util.Wrapper;
import cope.cosmos.util.client.ColorUtil;
import cope.cosmos.util.render.FontUtil;
import cope.cosmos.util.render.RenderUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

@SuppressWarnings("unused")
public class Window implements GUIUtil, Wrapper {

    private final String name;
    private final ResourceLocation icon;

    private Vec2f position;

    private final float bar = 14;
    private float width;
    private float height;

    private boolean pinned;

    private boolean draggable = true;
    private boolean dragging = false;

    private boolean expandable = true;
    private boolean expanding = false;

    private boolean focused = false;
    private boolean interactable = true;

    private Vec2f previousMousePosition = Vec2f.MIN;

    public Window(String name, Vec2f position, float width, float height, boolean pinned) {
        this.name = name;
        this.position = position;
        this.width = width;
        this.height = height;
        this.icon = new ResourceLocation("cosmos", "textures/icons/warning.png");
        this.pinned = pinned;
    }

    public Window(String name, ResourceLocation icon, Vec2f position, float width, float height, boolean pinned) {
        this.name = name;
        this.icon = icon;
        this.position = position;
        this.width = width;
        this.height = height;
        this.pinned = pinned;
    }

    public void drawWindow() {

        // do window blur if it is necessary
        if (ClickGUI.windowBlur.getValue()) {
            BlurUtil.blurRect((int) position.x, (int) position.y, (int) width, (int) height, 6, 1, 1);
        }

        // check if we are dragging our window and update position accordingly
        if (mouseOver(position.x, position.y, width, bar) && getGUI().getMouse().isLeftHeld()) {
            setDragging(true);
        }

        if (isDragging() && isDraggable()) {
            setPosition(new Vec2f(position.x + (getGUI().getMouse().getMousePosition().x - previousMousePosition.x), position.y + (getGUI().getMouse().getMousePosition().y - previousMousePosition.y)));
        }

        // check if we are expanding our window and update our dimensions accordingly
        if (mouseOver(position.x + width - 10, position.y + height - 10, 10, 10) && getGUI().getMouse().isLeftHeld()) {
            setExpanding(true);
        }

        if (isExpanding() && isExpandable()) {
            // make sure the window isn't expanded past a certain point
            height = MathHelper.clamp(getGUI().getMouse().getMousePosition().y - position.y, 100, new ScaledResolution(mc).getScaledHeight());
            width = MathHelper.clamp(getGUI().getMouse().getMousePosition().x - position.x, 150, new ScaledResolution(mc).getScaledWidth());
        }

        glPushMatrix();

        // window background
        RenderUtil.drawRect(position.x, position.y, width, height, new Color(0, 0, 0, 90));

        // title bar
        RenderUtil.drawRect(position.x, position.y, width, bar, new Color(ColorUtil.getPrimaryColor().getRed(), ColorUtil.getPrimaryColor().getGreen(), ColorUtil.getPrimaryColor().getBlue(), 130));
        FontUtil.drawStringWithShadow(name, position.x + 5, position.y + 3, -1);

        glPushMatrix();
        mc.getTextureManager().bindTexture(new ResourceLocation("cosmos", "textures/icons/cancel.png"));
        Gui.drawModalRectWithCustomSizedTexture((int) (position.x + width - 14), (int) position.y + 1, 0, 0, 14, 14, 14, 14);
        glPopMatrix();

        if (mouseOver(position.x + width - 16, position.y, 14, 14)) {
            RenderUtil.drawRect(position.x + width - 14, position.y, 14, 14, new Color(25, 25, 25, 60));
        }

        // window outline
        RenderUtil.drawRect(position.x, position.y + bar, 1, height - bar - 1, new Color(ColorUtil.getPrimaryColor().getRed(), ColorUtil.getPrimaryColor().getGreen(), ColorUtil.getPrimaryColor().getBlue(), 130));
        RenderUtil.drawRect(position.x + width - 1, position.y + bar, 1, height - bar - 1, new Color(ColorUtil.getPrimaryColor().getRed(), ColorUtil.getPrimaryColor().getGreen(), ColorUtil.getPrimaryColor().getBlue(), 130));
        RenderUtil.drawRect(position.x, position.y + height - 1, width, 1, new Color(ColorUtil.getPrimaryColor().getRed(), ColorUtil.getPrimaryColor().getGreen(), ColorUtil.getPrimaryColor().getBlue(), 130));

        glPopMatrix();

        previousMousePosition = new Vec2f(getGUI().getMouse().getMousePosition().x, getGUI().getMouse().getMousePosition().y);
    }

    public void handleLeftClick() {
        if (mouseOver(position.x + width - 14, position.y, 14, 14)) {
            getManager().removeWindow(this);
        }
    }

    public void handleRightClick() {

    }

    public void handleScroll(int scroll) {

    }

    public void handleKeyPress(char typedCharacter, int key) {

    }

    public String getName() {
        return name;
    }

    public ResourceLocation getIcon() {
        return icon;
    }

    public void setPosition(Vec2f in) {
        position = in;
    }

    public Vec2f getPosition() {
        return position;
    }

    public float getBar() {
        return bar;
    }

    public void setHeight(float in) {
        height = in;
    }

    public float getHeight() {
        return height;
    }

    public void setWidth(float in) {
        width = in;
    }

    public float getWidth() {
        return width;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean in) {
        pinned = in;
    }

    public void setDraggable(boolean in) {
        draggable = in;
    }

    public boolean isDraggable() {
        return draggable;
    }

    public void setDragging(boolean in) {
        dragging = in;
    }

    public boolean isDragging() {
        return dragging;
    }

    public void setExpandable(boolean in) {
        expandable = in;
    }

    public boolean isExpandable() {
        return expandable;
    }

    public void setExpanding(boolean in) {
        expanding = in;
    }

    public boolean isExpanding() {
        return expanding;
    }

    public boolean isInteractable() {
        return interactable;
    }

    public void setInteractable(boolean in) {
        interactable = in;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean in) {
        focused = in;
    }
}
