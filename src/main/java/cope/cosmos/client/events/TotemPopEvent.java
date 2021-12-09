package cope.cosmos.client.events;

import net.minecraft.entity.Entity;
import cope.cosmos.event.annotation.Cancelable;
import cope.cosmos.event.listener.Event;

@Cancelable
public class TotemPopEvent extends Event {

    private final Entity popEntity;

    public TotemPopEvent(Entity popEntity) {
        this.popEntity = popEntity;
    }

    public Entity getPopEntity() {
        return popEntity;
    }
}
