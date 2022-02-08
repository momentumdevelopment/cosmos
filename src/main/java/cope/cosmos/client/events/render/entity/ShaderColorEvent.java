package cope.cosmos.client.events.render.entity;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.awt.*;

@Cancelable
public class ShaderColorEvent extends Event {

    private final Entity entity;
    private Color color;

    public ShaderColorEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color in) {
        color = in;
    }
}
