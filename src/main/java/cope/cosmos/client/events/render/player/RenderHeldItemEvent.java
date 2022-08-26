package cope.cosmos.client.events.render.player;

import net.minecraft.util.EnumHandSide;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * @author Surge
 * @since 31/03/2022
 */
@Cancelable
public class RenderHeldItemEvent extends Event {

    // The side the item is in
    private EnumHandSide side;

    public RenderHeldItemEvent(EnumHandSide enumHandSide) {
        this.side = enumHandSide;
    }

    /**
     * Gets the side the item is in
     * @return The side the item is in
     */
    public EnumHandSide getSide() {
        return side;
    }

    public static class Pre extends RenderHeldItemEvent {
        public Pre(EnumHandSide enumHandSide) {
            super(enumHandSide);
        }
    }

    public static class Post extends RenderHeldItemEvent {
        public Post(EnumHandSide enumHandSide) {
            super(enumHandSide);
        }
    }
}
