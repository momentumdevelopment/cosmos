package cope.cosmos.client.events;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class EntityHitboxSizeEvent extends Event {

    private float hitboxSize = 0;

    public void setHitboxSize(float hitboxSize) {
        this.hitboxSize = hitboxSize;
    }

    public float getHitboxSize() {
        return this.hitboxSize;
    }
}
