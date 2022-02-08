package cope.cosmos.client.events.render.entity.tile;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called when a {@link TileEntity} tile entity is rendered
 * @author linustouchtips
 * @since 12/24/2021
 */
@Cancelable
public class RenderTileEntityEvent extends Event {

    // the tile entity being rendered
    private final TileEntity tileEntity;

    private final double x, y, z;

    private final float partialTicks;
    private final int destroyStage;
    private final float partial;

    private final Tessellator buffer;

    public RenderTileEntityEvent(TileEntity tileEntity, double x, double y, double z, float partialTicks, int destroyStage, float partial, Tessellator buffer) {
        this.tileEntity = tileEntity;
        this.x = x;
        this.y = y;
        this.z = z;
        this.partialTicks = partialTicks;
        this.destroyStage = destroyStage;
        this.partial = partial;
        this.buffer = buffer;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public int getDestroyStage() {
        return destroyStage;
    }

    public float getPartial() {
        return partial;
    }

    public Tessellator getBuffer() {
        return buffer;
    }

    /**
     * Gets the tile entity being rendered
     * @return the tile entity being rendered
     */
    public TileEntity getTileEntity() {
        return tileEntity;
    }
}
