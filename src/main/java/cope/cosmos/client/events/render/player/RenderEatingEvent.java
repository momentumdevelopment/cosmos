package cope.cosmos.client.events.render.player;

import net.minecraft.util.EnumHandSide;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called when the first person item is transformed for eating
 * @author Surge, linustouchtips
 * @since 08/27/2022
 */
@Cancelable
public class RenderEatingEvent extends Event {

    // eating scale
    private float scale;

    // hand side
    private final EnumHandSide handSide;

    public RenderEatingEvent(EnumHandSide handSide) {
        this.handSide = handSide;
    }

    /**
     * Sets the eating scale
     * @param in The new eating scale
     */
    public void setScale(float in) {
        scale = in;
    }

    /**
     * Gets the hand side
     * @return The hand side
     */
    public EnumHandSide getHandSide() {
        return handSide;
    }

    /**
     * Gets the eating scale
     * @return The eating scale
     */
    public float getScale() {
        return scale;
    }
}
