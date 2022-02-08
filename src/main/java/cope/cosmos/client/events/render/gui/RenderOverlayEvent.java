package cope.cosmos.client.events.render.gui;

import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called when a HUD overlay is rendered
 * @author linustouchtips
 * @since 12/17/2021
 */
@Cancelable
public class RenderOverlayEvent extends Event {

    // HUD overlay
    private final RenderBlockOverlayEvent.OverlayType overlayType;

    public RenderOverlayEvent(RenderBlockOverlayEvent.OverlayType overlayType) {
        this.overlayType = overlayType;
    }

    /**
     * Gets the overlay type of the render
     * @return tTe overlay type of the render
     */
    public RenderBlockOverlayEvent.OverlayType getOverlayType() {
        return overlayType;
    }
}
