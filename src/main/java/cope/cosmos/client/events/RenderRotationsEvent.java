package cope.cosmos.client.events;

import cope.cosmos.event.annotation.Cancelable;
import cope.cosmos.event.listener.Event;

@Cancelable
public class RenderRotationsEvent extends Event {

    private float yaw, pitch;

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setYaw(float in) {
        yaw = in;
    }

    public void setPitch(float in) {
        pitch = in;
    }
}
