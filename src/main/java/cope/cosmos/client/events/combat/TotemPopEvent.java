package cope.cosmos.client.events.combat;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

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
