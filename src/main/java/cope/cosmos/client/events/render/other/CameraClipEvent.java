package cope.cosmos.client.events.render.other;

import net.minecraftforge.fml.common.eventhandler.Event;

public class CameraClipEvent extends Event {

    private double distance;

    public CameraClipEvent(double distance) {
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double in) {
        distance = in;
    }
}
