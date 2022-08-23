package cope.cosmos.client.events.input;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class KeyDownEvent extends Event {
    
    private final int keyCode;
    private boolean pressed;

    public KeyDownEvent(int keyCode, boolean pressed) {
        this.pressed = pressed;
        this.keyCode = keyCode;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public boolean isPressed() {
        return pressed;
    }

    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }
}
