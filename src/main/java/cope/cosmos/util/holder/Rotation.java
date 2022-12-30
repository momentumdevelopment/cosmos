package cope.cosmos.util.holder;

import cope.cosmos.util.Wrapper;

/**
 * @author linustouchtips
 * @since 05/09/2021
 */
public class Rotation implements Wrapper {

    // default invalid rotation value
    public static final Rotation INVALID_ROTATION = new Rotation(Float.NaN, Float.NaN);

    // rotation values
    private float yaw, pitch;

    // rotation mode
    private final Rotate rotate;

    public Rotation(float yaw, float pitch, Rotate rotate) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.rotate = rotate;
    }

    public Rotation(float yaw, float pitch) {
        this(yaw, pitch, Rotate.NONE);
    }

    /**
     * Gets the yaw value for the rotation
     * @return The yaw value for the rotation
     */
    public float getYaw() {
        return yaw;
    }

    /**
     * Sets the yaw value for the rotation
     * @param in The new yaw value for the rotation
     */
    public void setYaw(float in) {
        yaw = in;
    }

    /**
     * Gets the pitch value for the rotation
     * @return The pitch value for the rotation
     */
    public float getPitch() {
        return pitch;
    }

    /**
     * Sets the pitch value for the rotation
     * @param in The new pitch value for the rotation
     */
    public void setPitch(float in) {
        pitch = in;
    }

    /**
     * Gets the current rotation mode (NONE if holder)
     * @return The current rotation mode
     */
    public Rotate getRotation() {
        return rotate;
    }

    /**
     * Checks if the current yaw and pitch are valid
     * @return Whether the current yaw and pitch are valid
     */
    public boolean isValid() {
        return !Float.isNaN(getYaw()) && !Float.isNaN(getPitch());
    }

    public enum Rotate {

        /**
         * Rotate via packets, should be silent client-side
         */
        PACKET,

        /**
         * Standard rotate
         */
        CLIENT,

        /**
         * No actual rotation, we are using this to hold yaw & pitch values
         */
        NONE
    }
}

