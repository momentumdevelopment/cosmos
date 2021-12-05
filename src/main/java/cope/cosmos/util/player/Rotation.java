package cope.cosmos.util.player;

import cope.cosmos.util.Wrapper;

public class Rotation implements Wrapper {

    public float yaw;
    public float pitch;
    public Rotate rotate;

    public Rotation(float yaw, float pitch, Rotate rotate) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.rotate = rotate;
    }

    public Rotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.rotate = Rotate.NONE;
    }

    public void updateRotations() {

    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public Rotate getRotation() {
        return rotate;
    }

    public boolean isValid() {
        return !Float.isNaN(getYaw()) || !Float.isNaN(getPitch());
    }

    public enum Rotate {
        PACKET, CLIENT, NONE
    }

    public static class MutableRotation extends Rotation {

        public MutableRotation(float yaw, float pitch, Rotate rotate) {
            super(yaw, pitch, rotate);
        }

        public MutableRotation(float yaw, float pitch) {
            super(yaw, pitch, Rotate.NONE);
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

        public void setRotation(Rotate in) {
            rotate = in;
        }
    }
}

