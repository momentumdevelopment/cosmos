package cope.cosmos.client.events;

import cope.cosmos.event.annotation.Cancelable;
import cope.cosmos.event.listener.Event;

@Cancelable
public class TravelEvent extends Event {

    private final float strafe, vertical, forward;

    public TravelEvent(float strafe, float vertical, float forward) {
        this.strafe = strafe;
        this.vertical = vertical;
        this.forward = forward;
    }

    public float getStrafe() {
        return strafe;
    }

    public float getVertical() {
        return vertical;
    }

    public float getForward() {
        return forward;
    }
}
