package cope.cosmos.client.events.motion.collision;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.List;

/**
 * Called when an entity collides with a bounding box
 * @author linustouchtips
 * @since 01/08/2022
 */
@Cancelable
public class CollisionBoundingBoxEvent extends Event {

    // block info
    private final Block block;
    private final BlockPos position;
    private final Entity entity;

    // collision bounding box
    private final AxisAlignedBB collisionBox;
    private final List<AxisAlignedBB> collisionList;

    public CollisionBoundingBoxEvent(Block block, BlockPos position, Entity entity, AxisAlignedBB collisionBox, List<AxisAlignedBB> collisionList) {
        this.block = block;
        this.position = position;
        this.entity = entity;
        this.collisionBox = collisionBox;
        this.collisionList = collisionList;
    }

    /**
     * Gets the block
     * @return The block
     */
    public Block getBlock() {
        return block;
    }

    /**
     * Gets the position of the block
     * @return The position of the block
     */
    public BlockPos getPosition() {
        return position;
    }

    /**
     * Gets the colliding entity
     * @return The colliding entity
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * Gets the collision box
     * @return The collision box
     */
    public AxisAlignedBB getCollisionBox() {
        return collisionBox;
    }

    /**
     * Gets the collissionList of collision boxes
     * @return The collissionList of collision boxes
     */
    public List<AxisAlignedBB> getCollisionList() {
        return collisionList;
    }
}