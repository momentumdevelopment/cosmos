package cope.cosmos.client.events;

import net.minecraftforge.fml.common.eventhandler.Event;

// @TODO: possibly make this into RenderHeldItemColorEvent ?
public class RenderHeldItemAlphaEvent extends Event {
    private float alpha;

    public RenderHeldItemAlphaEvent(float alpha) {
        this.alpha = alpha;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }
}
