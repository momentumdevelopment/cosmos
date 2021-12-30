package cope.cosmos.client.events;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.awt.*;

@Cancelable
public class RenderFogColorEvent extends Event {
    private final Color color;

    public RenderFogColorEvent(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
