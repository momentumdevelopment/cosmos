package cope.cosmos.util.player;

import cope.cosmos.client.Cosmos;
import cope.cosmos.util.Wrapper;

public class Rotation implements Wrapper {
    public Rotate rotate;
    public float yaw;
    public float pitch;

    public Rotation(float yaw, float pitch, Rotate rotate) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.rotate = rotate;
    }

    // @todo this is deprecated, call Cosmos.INSTANCE.getRotationManager().rotate()
    public void updateModelRotations() {
        if (nullCheck()) {
            Cosmos.INSTANCE.getRotationManager().rotate(this.yaw, this.pitch, this.rotate);
        }
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

    public enum Rotate {
        PACKET, CLIENT, NONE
    }
}

