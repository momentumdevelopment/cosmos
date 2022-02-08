package cope.cosmos.client.events.motion.movement;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

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
