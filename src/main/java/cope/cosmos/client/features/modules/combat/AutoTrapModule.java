package cope.cosmos.client.features.modules.combat;

import com.mojang.realmsclient.util.Pair;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.InventoryManager.*;
import cope.cosmos.client.manager.managers.SocialManager;
import cope.cosmos.util.combat.EnemyUtil;
import cope.cosmos.util.holder.Rotation.Rotate;
import cope.cosmos.util.world.BlockUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author linustouchtips
 * @since 08/28/2022
 */
public class AutoTrapModule extends Module {
    public static AutoTrapModule INSTANCE;

    public AutoTrapModule() {
        super("AutoTrap", Category.COMBAT, "Traps enemies");
        INSTANCE = this;
    }

    // **************************** anticheat****************************

    public static Setting<Rotate> rotate = new Setting<>("Rotation", Rotate.NONE)
            .setAlias("Rotate")
            .setDescription("How to rotate when placing blocks");

    public static Setting<Boolean> strict = new Setting<>("Strict", false)
            .setDescription("If to use strict direction to place blocks");

    // **************************** general ****************************

    public static Setting<Double> range = new Setting<>("Range", 0.0, 5.0, 6.0, 1)
            .setDescription("Range to place");

    public static Setting<Switch> autoSwitch = new Setting<>("Switch", Switch.NORMAL)
            .setAlias("AutoSwitch", "Swap", "AutoSwap")
            .setDescription("How to switch when placing blocks");

    public static Setting<Double> blocks = new Setting<>("Blocks", 1.0, 4.0, 10.0, 0)
            .setAlias("BlocksPerTick", "BPT")
            .setDescription("Allowed block placements per tick");

    public static Setting<Boolean> cover = new Setting<>("Cover", true)
            .setDescription("Covers the enemies head");

    public static Setting<Boolean> doubleCover = new Setting<>("DoubleCover", false)
            .setDescription("Double covers the enemies head to prevent stepping")
            .setVisible(() -> cover.getValue());

    public static Setting<Boolean> extend = new Setting<>("Extend", true)
            .setDescription("If not centered on a block, it'll extend to a 2x1 and etc");

    public static Setting<Boolean> support = new Setting<>("Support", true)
            .setDescription("If to place supporting blocks to be able to place blocks");

    // cached placements to place at, updated on a new thread
    private List<BlockPos> placements = new ArrayList<>();
    private List<BlockPos> replacements = new ArrayList<>();

    // blocks placed per tick counter
    private int placed;

    // entities that are safe to place on
    private static final List<Class<? extends Entity>> SAFE_ENTITIES = Arrays.asList(EntityEnderCrystal.class, EntityItem.class, EntityXPOrb.class, EntityBoat.class, EntityMinecart.class);

    @Override
    public void onThread() {

        // map of targets
        TreeMap<Double, EntityPlayer> targetMap = new TreeMap<>();

        // scan targets
        for (EntityPlayer player : mc.world.playerEntities) {

            // make sure the entity is valid to fill
            if (player == null || player.equals(mc.player) || player.getEntityId() < 0 || EnemyUtil.isDead(player) || getCosmos().getSocialManager().getSocial(player.getName()).equals(SocialManager.Relationship.FRIEND)) {
                continue;
            }

            // verify that the target is within range
            double targetDistance = mc.player.getDistance(player);
            if (targetDistance > range.getValue()) {
                continue;
            }

            // place in map
            targetMap.put(targetDistance, player);
        }

        // original block position
        BlockPos origin = null;

        // check targets
        if (!targetMap.isEmpty()) {

            // target
            EntityPlayer target = targetMap.firstEntry().getValue();

            // update origin
            origin = new BlockPos(target.posX, Math.floor(target.posY), target.posZ);
        }

        // get place positions
        placements = getPlacements(origin);
        replacements = getReplacements();
    }

    @Override
    public void onUpdate() {

        // haven't placed any blocks on this tick yet
        placed = 0;

        // check if need to place any blocks
        if (!replacements.isEmpty()) {

            // log previous slot, we'll switch back to this item
            int previousSlot = mc.player.inventory.currentItem;

            // switch to block before placing
            if (!autoSwitch.getValue().equals(Switch.NONE)) {

                // slot to switch to
                int obsidianSlot = getCosmos().getInventoryManager().searchSlot(Item.getItemFromBlock(Blocks.OBSIDIAN), InventoryRegion.HOTBAR);
                int echestSlot = getCosmos().getInventoryManager().searchSlot(Item.getItemFromBlock(Blocks.ENDER_CHEST), InventoryRegion.HOTBAR);

                // prefer obsidian over echests
                if (obsidianSlot != -1) {
                    getCosmos().getInventoryManager().switchToSlot(obsidianSlot, autoSwitch.getValue());
                }

                // fallback if we don't have obsidian
                else if (echestSlot != -1) {
                    getCosmos().getInventoryManager().switchToSlot(echestSlot, autoSwitch.getValue());
                }
            }

            // place at each of our placements
            for (BlockPos position : replacements) {

                // check the blocks per tick
                if (placed >= blocks.getValue().intValue()) {
                    break;
                }

                // place block
                if (placeBlock(position)) {

                    // getCosmos().getChatManager().sendClientMessage("placed");
                    placed++;
                }
            }

            // switch back to previous slot
            if (previousSlot != -1) {
                getCosmos().getInventoryManager().switchToSlot(previousSlot, autoSwitch.getValue());
            }
        }
    }

    /**
     * Places a block at this position
     * @param in the position
     */
    private boolean placeBlock(BlockPos in) {

        // check if block is replaceable
        if (BlockUtil.isReplaceable(in)) {

            // place block
            getCosmos().getInteractionManager().placeBlock(in, rotate.getValue(), strict.getValue(), SAFE_ENTITIES);

            // block placement was successful
            return true;
        }

        return false;
    }

    /**
     * Gets the valid placements for a given origin
     * @param in The origin
     * @return A list of all the valid placements for a given origin
     */
    private List<BlockPos> getPlacements(BlockPos in) {

        // check position
        if (in == null) {
            return new ArrayList<>();
        }

        // set of queued block placements
        List<BlockPos> queue = new CopyOnWriteArrayList<>();

        // directions that were inhibited
        Set<EnumFacing> inhibitedDirections = new HashSet<>();

        // cover
        if (cover.getValue()) {
            queue.add(in.up(2));

            // anti-step
            if (doubleCover.getValue()) {
                queue.add(in.up(3));
            }
        }

        // find a facing to place against
        for (EnumFacing facing : EnumFacing.VALUES) {

            // check its validity
            if (isInvalidDirection(facing)) {
                continue;
            }

            // get the neighboring block
            BlockPos offset = in.offset(facing);

            // entity is obstructing our placement
            if (isEntityIntersecting(offset)) {

                // cover
                if (cover.getValue()) {
                    queue.add(offset.up(2));

                    // anti-step
                    if (doubleCover.getValue()) {
                        queue.add(offset.up(3));
                    }
                }

                // extend surround to encompass the entity
                if (extend.getValue()) {

                    // check all directions for extensions
                    for (EnumFacing direction : EnumFacing.VALUES) {

                        // check its validity
                        if (isInvalidDirection(direction)) {
                            continue;
                        }

                        // position based on the extend direction
                        BlockPos extendedOffset = offset.offset(direction);

                        // can't place at the origin
                        if (extendedOffset.equals(in)) {
                            continue;
                        }

                        // offset entity intersection and build the queue around the entity
                        if (BlockUtil.isReplaceable(extendedOffset)) {

                            // entity is not obstructing our placement
                            if (!isEntityIntersecting(extendedOffset)) {
                                queue.add(extendedOffset);
                                queue.add(extendedOffset.up());
                            }

                            else {
                                inhibitedDirections.add(direction);  // direction wasn't able to placed on
                            }
                        }
                    }

                    // needs an additional offset
                    if (inhibitedDirections.size() > 1) {

                        // additional offset
                        BlockPos addPosition = in;

                        // offset by inhibited directions
                        for (EnumFacing direction : inhibitedDirections) {

                            // opposite of the inhibited directions
                            // EnumFacing oppositeDirection = direction.getOpposite();

                            // offset the position
                            addPosition = addPosition.offset(direction);
                        }

                        // check all directions for extensions
                        for (EnumFacing direction : EnumFacing.VALUES) {

                            // check its validity
                            if (isInvalidDirection(direction)) {
                                continue;
                            }

                            // position based on the extend direction
                            BlockPos additionPosition = addPosition.offset(direction);

                            // offset entity intersection and build the queue around the entity
                            if (!isEntityIntersecting(additionPosition)) {

                                // entity is not obstructing our placement
                                queue.add(additionPosition);
                                queue.add(additionPosition.up());
                            }
                        }
                    }
                }
            }

            // if an entity is not blocking the placement, we can just queue it
            else {
                queue.add(offset);
                queue.add(offset.up());
            }
        }

        // sometimes blocks in the queue can't realistically be placed on so they need support blocks to place against
        if (support.getValue()) {

            // check each block about to be queued
            for (BlockPos position : queue) {

                // first layer & covers need support, others will resolve themselves
                if (position.getY() == in.getY() || position.getY() == in.getY() + 2) {

                    // checks if the position needs support
                    boolean support = true;

                    // check all side
                    for (EnumFacing direction : EnumFacing.VALUES) {

                        // offset position based on the direction
                        BlockPos offsetPosition = position.offset(direction);

                        // check if it can be placed on
                        if (!mc.world.isAirBlock(offsetPosition)) {

                            // block needs support if we want to place on it
                            support = false;
                            break;
                        }
                    }

                    // usually down should work because we have the origin to place on
                    if (support) {

                        // down support
                        if (position.getY() == in.getY()) {
                            queue.add(position.down());
                        }

                        else {

                            // closest support
                            Pair<Double, BlockPos> supportPosition = Pair.of(Double.MAX_VALUE, BlockPos.ORIGIN);

                            // find closest support
                            for (EnumFacing facing : EnumFacing.HORIZONTALS) {

                                // offset position
                                BlockPos offset = position.offset(facing);

                                // check distances
                                double distance = BlockUtil.getDistanceToCenter(mc.player, offset);
                                if (distance < supportPosition.first()) {
                                    supportPosition = Pair.of(distance, offset);
                                }
                            }

                            // add to queue
                            queue.add(supportPosition.second());
                        }
                    }
                }
            }
        }

        // filter by range and sort by y-value
        return queue.stream().filter(position -> BlockUtil.getDistanceToCenter(mc.player, position) <= range.getValue()).sorted(Comparator.comparing(Vec3i::getY)).collect(Collectors.toList());
    }

    /**
     * Gets the set of all replacements to be made
     * @return The set of all replacements to be made
     */
    private List<BlockPos> getReplacements() {

        // set of replacements
        List<BlockPos> replacements = new ArrayList<>();

        // check our placements if they have been removed, we need to replace them
        for (BlockPos position : placements) {
            if (BlockUtil.isReplaceable(position)) {
                replacements.add(position);
            }
        }

        return replacements;
    }

    /**
     * Checks if the facing is an extension that cannot be placed on
     * @param in The given facing
     * @return Whether the facing is an extension that cannot be placed on
     */
    private boolean isInvalidDirection(EnumFacing in) {
        return in.equals(EnumFacing.UP) || in.equals(EnumFacing.DOWN);
    }

    /**
     * Checks if an entity hitbox is intersecting this position
     * @param in the block position
     * @return if there is an entity hitbox preventing a placement
     */
    private boolean isEntityIntersecting(BlockPos in) {

        // check all entities
        for (Entity entity : new ArrayList<>(mc.world.loadedEntityList)) {

            // entities that cannot intersect our placements
            if (entity == null || entity instanceof EntityXPOrb || entity instanceof EntityItem || entity instanceof EntityEnderCrystal) {
                continue;
            }

            // entity intersects with our position
            if (entity.getEntityBoundingBox().intersects(new AxisAlignedBB(in))) {
                return true;
            }
        }

        return false;
    }
}
