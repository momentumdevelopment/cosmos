package cope.cosmos.client.events.motion.movement;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called when a player steps up a block
 * @author Doogie13
 * @since 12/27/2021
 */
@Cancelable
public class StepEvent extends Event {

    // info
    private final AxisAlignedBB axisAlignedBB;
    private float height;

    public StepEvent(AxisAlignedBB axisAlignedBB, float height) {
        this.axisAlignedBB = axisAlignedBB;
        this.height = height;
    }

    /**
     * Gets the bounding box of the step height
     * @return The bounding box of the step height
     */
    public AxisAlignedBB getAxisAlignedBB() {
        return axisAlignedBB;
    }

    /**
     * Sets the maximum height of the step
     * @param in The new maximum height of the step
     */
    public void setHeight(float in) {
        height = in;
    }

    /**
     * Gets the maximum height of the step
     * @return The maximum height of the step
     */
    public float getHeight() {
        return height;
    }
}
