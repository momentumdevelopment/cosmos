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
                    mc.player.renderYawOffset = yaw;
                    mc.player.rotationYawHead = yaw;
                    Cosmos.INSTANCE.getRotationManager().setHeadPitch(pitch);
                    break;
                case CLIENT:
                    mc.player.rotationYaw = yaw;
                    mc.player.rotationPitch = pitch;
                    break;
                case NONE:
                	break;
            }
        }
    }

    public void restoreRotations() {
        if (nullCheck()) {
            yaw = mc.player.rotationYaw;
            pitch = mc.player.rotationPitch;
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

