package cope.cosmos.client.events.entity.hitbox;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

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
