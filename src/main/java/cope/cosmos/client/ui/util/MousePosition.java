package cope.cosmos.client.ui.util;

import net.minecraft.util.math.Vec2f;

public class MousePosition {

    private Vec2f mousePosition;
    private boolean leftClick, rightClick, leftHeld, rightHeld;

    public MousePosition(Vec2f mousePosition, boolean leftClick, boolean rightClick, boolean leftHeld, boolean rightHeld) {
        this.mousePosition = mousePosition;
        this.leftClick = leftClick;
        this.rightClick = rightClick;
        this.leftHeld = leftHeld;
        this.rightHeld = rightHeld;
    }

    public boolean isLeftClick() {
        return leftClick;
    }

    public void setLeftClick(boolean in) {
        leftClick = in;
    }

    public boolean isRightClick() {
        return rightClick;
    }

    public void setRightClick(boolean in) {
        rightClick = in;
    }

    public boolean isLeftHeld() {
        return leftHeld;
    }

    public void setLeftHeld(boolean in) {
        leftHeld = in;
    }

    public boolean isRightHeld() {
        return rightHeld;
    }

    public void setRightHeld(boolean in) {
        rightHeld = in;
    }

    public void setPosition(Vec2f in) {
        mousePosition = in;
    }

    public Vec2f getPosition() {
        return mousePosition;
    }
}
