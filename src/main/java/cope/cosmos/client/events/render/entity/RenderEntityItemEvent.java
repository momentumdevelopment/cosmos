package cope.cosmos.client.events.render.entity;


import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called when an item entity is rendered
 * @author linustouchtips
 * @since 04/11/2022
 */
@Cancelable
public class RenderEntityItemEvent extends Event {

    // renderer
    private final RenderItem itemRenderer;

    // info
    private final EntityItem entityItem;
    private final double x, y, z;
    private final float entityYaw;
    private final float partialTicks;

    public RenderEntityItemEvent(RenderItem itemRenderer, EntityItem entityItem, double x, double y, double z, float entityYaw, float partialTicks) {
        this.itemRenderer = itemRenderer;
        this.entityItem = entityItem;
        this.x = x;
        this.y = y;
        this.z = z;
        this.entityYaw = entityYaw;
        this.partialTicks = partialTicks;
    }

    /**
     * Gets the item renderer
     * @return The item renderer
     */
    public RenderItem getItemRenderer() {
        return itemRenderer;
    }

    /**
     * Gets the item entity
     * @return The item entity
     */
    public EntityItem getEntityItem() {
        return entityItem;
    }

    /**
     * Gets the entity's x position
     * @return The entity's x position
     */
    public double getX() {
        return x;
    }

    /**
     * Gets the entity's y position
     * @return The entity's y position
     */
    public double getY() {
        return y;
    }

    /**
     * Gets the entity's z position
     * @return The entity's z position
     */
    public double getZ() {
        return z;
    }

    /**
     * Gets the entity's yaw
     * @return The entity's yaw
     */
    public float getEntityYaw() {
        return entityYaw;
    }

    /**
     * Gets the render partial ticks
     * @return The render partial ticks
     */
    public float getPartialTicks() {
        return partialTicks;
    }
}