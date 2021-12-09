package cope.cosmos.client.events;

import cope.cosmos.event.annotation.Cancelable;
import cope.cosmos.event.listener.Event;

@Cancelable
public class MotionUpdateEvent extends Event {

    private double x, y, z;
    private float yaw, pitch;
    private boolean onGround;

    public double getX() {
        return x;
    }

    public void setX(double in) {
        x = in;
    }

    public double getY() {
        return y;
    }

    public void setY(double in) {
        y = in;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double in) {
        z = in;
    }

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

    public boolean getOnGround() {
        return onGround;
    }

    public void setOnGround(boolean in) {
        onGround = in;
    }
}
