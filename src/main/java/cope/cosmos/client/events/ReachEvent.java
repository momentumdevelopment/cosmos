package cope.cosmos.client.events;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

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
