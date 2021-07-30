package cope.cosmos.client.events;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class LivingUpdateEvent extends Event {

    private EntityPlayerSP entityPlayerSP;
    private boolean sprinting;

    public LivingUpdateEvent(EntityPlayerSP entityPlayerSP, boolean sprinting) {
        this.entityPlayerSP = entityPlayerSP;
        this.sprinting = sprinting;
    }

    public EntityPlayerSP getEntityPlayerSP() {
        return this.entityPlayerSP;
    }

    public boolean isSprinting() {
        return this.sprinting;
    }
}
