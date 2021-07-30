package cope.cosmos.util.player;

import cope.cosmos.client.Cosmos;
import cope.cosmos.util.Wrapper;

public class Rotation implements Wrapper {

    private float yaw;
    private float pitch;
    private final Rotate rotate;

    public Rotation(float yaw, float pitch, Rotate rotate) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.rotate = rotate;
    }

    public void updateModelRotations() {
        if (nullCheck()) {
            switch (rotate) {
                case PACKET:
                    mc.player.renderYawOffset = this.yaw;
                    mc.player.rotationYawHead = this.yaw;
                    Cosmos.INSTANCE.getRotationManager().setHeadPitch(this.pitch);
                    break;
                case CLIENT:
                    mc.player.rotationYaw = this.yaw;
                    mc.player.rotationPitch = this.pitch;
                    break;
                case NONE:
                	break;
            }
        }
    }

    public void restoreRotations() {
        if (nullCheck()) {
            this.yaw = mc.player.rotationYaw;
            this.pitch = mc.player.rotationPitch;
        }
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public Rotate getRotation() {
        return this.rotate;
    }

    public enum Rotate {
        PACKET, CLIENT, NONE
    }
}

