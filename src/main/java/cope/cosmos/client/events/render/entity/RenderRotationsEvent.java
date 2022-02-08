package cope.cosmos.client.events.render.entity;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class RenderRotationsEvent extends Event {

    private float yaw, pitch;

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setYaw(float in) {
        yaw = in;
    }

    public void setPitch(float in) {
        pitch = in;
    }
}
