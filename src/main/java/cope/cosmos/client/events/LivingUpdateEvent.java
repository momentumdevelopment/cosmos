package cope.cosmos.client.events;

import net.minecraft.client.entity.EntityPlayerSP;
import cope.cosmos.event.annotation.Cancelable;
import cope.cosmos.event.listener.Event;

@Cancelable
public class LivingUpdateEvent extends Event {

    private final EntityPlayerSP entityPlayerSP;
    private final boolean sprinting;

    public LivingUpdateEvent(EntityPlayerSP entityPlayerSP, boolean sprinting) {
        this.entityPlayerSP = entityPlayerSP;
        this.sprinting = sprinting;
    }

    public EntityPlayerSP getEntityPlayerSP() {
        return entityPlayerSP;
    }

    public boolean isSprinting() {
        return sprinting;
    }
}
