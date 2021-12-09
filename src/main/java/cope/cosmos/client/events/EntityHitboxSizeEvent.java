package cope.cosmos.client.events;

import cope.cosmos.event.annotation.Cancelable;
import cope.cosmos.event.listener.Event;

@Cancelable
public class EntityHitboxSizeEvent extends Event {

    private float hitboxSize;

    public void setHitboxSize(float in) {
        hitboxSize = in;
    }

    public float getHitboxSize() {
        return hitboxSize;
    }
}
