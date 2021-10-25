package cope.cosmos.util.player;

import cope.cosmos.util.Wrapper;

public class Rotation implements Wrapper {

    public float yaw;
    public float pitch;

    public Rotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public void updateModelRotations() {

    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public enum Rotate {
        PACKET, CLIENT, NONE
    }

    public static class MutableRotation extends Rotation {

        public MutableRotation(float yaw, float pitch) {
            super(yaw, pitch);
        }

        public boolean isValid() {
            return !Float.isNaN(yaw) && !Float.isNaN(pitch);
        }

        public void restoreRotations() {
            setYaw(Float.NaN);
            setPitch(Float.NaN);
        }

        public void setYaw(float in) {
            yaw = in;
        }

        public void setPitch(float in) {
            pitch = in;
        }
    }
}

