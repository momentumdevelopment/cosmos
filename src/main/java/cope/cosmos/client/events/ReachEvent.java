package cope.cosmos.client.events;

import cope.cosmos.event.annotation.Cancelable;
import cope.cosmos.event.listener.Event;

@Cancelable
public class ReachEvent extends Event {

    private float reach;

    public void setReach(float reach) {
        this.reach = reach;
    }

    public float getReach() {
        return reach;
    }
}
