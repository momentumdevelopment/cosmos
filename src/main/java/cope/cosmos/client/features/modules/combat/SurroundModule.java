package cope.cosmos.client.features.modules.combat;

import cope.cosmos.client.events.entity.player.RotationUpdateEvent;
import cope.cosmos.client.events.network.PacketEvent.PacketReceiveEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.InventoryManager.InventoryRegion;
import cope.cosmos.client.manager.managers.InventoryManager.Switch;
import cope.cosmos.util.entity.EntityUtil;
import cope.cosmos.util.holder.Rotation.Rotate;
import cope.cosmos.util.math.Timer;
import cope.cosmos.util.math.Timer.Format;
import cope.cosmos.util.world.BlockUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.*;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketMultiBlockChange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;

/**
 * @author linustouchtips, aesthetical
 * @since 12/08/2021
 */
public class SurroundModule extends Module {
    public static SurroundModule INSTANCE;

    public SurroundModule() {
        super("Surround", Category.COMBAT, "Surrounds your feet with obsidian");
        INSTANCE = this;
    }

    // **************************** anticheat****************************

    public static Setting<Timing> timing = new Setting<>("Timing", Timing.SEQUENTIAL)
            .setDescription("When to place blocks");

    public static Setting<Rotate> rotate = new Setting<>("Rotate", Rotate.NONE)
            .setDescription("How to rotate when placing blocks");

    public static Setting<Boolean> strict = new Setting<>("Strict", false)
            .setDescription("If to use strict direction to place blocks");

    // **************************** general ****************************

    public static Setting<Switch> autoSwitch = new Setting<>("Switch", Switch.NORMAL)
            .setDescription("How to switch when placing blocks");

    public static Setting<Double> blocks = new Setting<>("Blocks", 1.0, 4.0, 10.0, 0)
            .setDescription("Allowed block placements per tick");

    public static Setting<Boolean> extend = new Setting<>("Extend", true)
            .setDescription("If not centered on a block, it'll extend to a 2x1 and etc");

    public static Setting<Boolean> floor = new Setting<>("Floor", true)
            .setDescription("If to place at the floor");

    public static Setting<Boolean> support = new Setting<>("Support", true)
            .setDescription("If to place supporting blocks to be able to place blocks")
            .setVisible(() -> floor.getValue());

    public static Setting<Completion> completion = new Setting<>("Completion", Completion.SHIFT)
            .setDescription("When to disable the module");

    public static Setting<Center> center = new Setting<>("Center", Center.NONE)
            .setDescription("Mode to center the player position");

    // cached placements to place at, updated on a new thread
    private List<BlockPos> placements = new ArrayList<>();
    private List<BlockPos> replacements = new ArrayList<>();

    // clear
    private final Timer clearTimer = new Timer();

    // blocks placed per tick counter
    private int placed;

    // start info
    private double startY;

    // entities that are safe to place on
    private static final List<Class<? extends Entity>> SAFE_ENTITIES = Arrays.asList(EntityEnderCrystal.class, EntityItem.class, EntityXPOrb.class, EntityBoat.class, EntityMinecart.class);

    @Override
    public void onEnable() {
        super.onEnable();

        // mark our starting height
        startY = mc.player.posY;

        // if we need to be centered
        if (!center.getValue().equals(Center.NONE)) {

            // center placements
            double centerX = Math.floor(mc.player.posX) + 0.5;
            double centerZ = Math.floor(mc.player.posZ) + 0.5;

            // center player on their current block to allow surround to fully place
            switch (center.getValue()) {
                case NONE:
                default:
                    break;
                case MOTION:
                    // move player to center of block
                    mc.player.motionX = (centerX - mc.player.posX) / 2;
                    mc.player.motionZ = (centerZ - mc.player.posZ) / 2;
                    break;
                case TELEPORT:
                    // teleport player to center of block, send position packet
                    mc.player.setPosition(centerX, mc.player.posY, centerZ);
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(centerX, mc.player.posY, centerZ, mc.player.onGround));
                    break;
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // reset placements
        placements.clear();
        replacements.clear();
    }

    @Override
    public void onThread() {

        // original block position
        BlockPos origin = new BlockPos(mc.player.posX, Math.round(mc.player.posY), mc.player.posZ);

        // get place positions
        placements = getPlacements(origin);
        replacements = getReplacements();
    }

    @SubscribeEvent
    public void onRotationUpdateEvent(RotationUpdateEvent event) {

        // haven't placed any blocks on this tick yet
        placed = 0;

        // we are no long in the same spot
        // to linus: if someone mines the block under us we want to keep surrounded, and the Math.abs will return the
        // absolute value, so i'll do this. has the same effect, but allows us to fall down.
        if (mc.player.posY > startY && completion.getValue().equals(Completion.SHIFT)) {
            toggle();
            return;
        }

        // check if need to place any blocks
        if (!replacements.isEmpty()) {

            // check for blocking entities
            for (BlockPos position : replacements) {

                // player ping
                int ping = mc.player.connection.getPlayerInfo(mc.player.getUniqueID()).getResponseTime();

                // check unsafe entities and clear if necessary
                for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(position))) {

                    // can be placed on
                    if (entity == null || entity instanceof EntityItem || entity instanceof EntityXPOrb) {
                        continue;
                    }

                    // make sure we aren't attacking too fast TODO: normalize delays based on packets in the future maybe???
                    if (clearTimer.passedTime(ping <= 50 ? 75 : 100, Format.MILLISECONDS)) {

                        // attack crystals that are in the way
                        if (entity instanceof EntityEnderCrystal) {

                            // player sprint state
                            boolean sprintState = mc.player.isSprinting();

                            // stop sprint
                            if (strict.getValue()) {

                                // stop sprinting when attacking an entity
                                if (sprintState) {
                                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                                }
                            }

                            // attack
                            mc.player.connection.sendPacket(new CPacketUseEntity(entity));
                            mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));

                            // stop sprint
                            if (strict.getValue()) {

                                // reset sprint state
                                if (sprintState) {
                                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
                                }
                            }

                            clearTimer.resetTime();
                            break;
                        }

                        // attack vehicles 3 times
                        else if (EntityUtil.isVehicleMob(entity)) {

                            // attack
                            for (int i = 0; i < 3; i++) {
                                mc.player.connection.sendPacket(new CPacketUseEntity(entity));
                                mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                            }

                            clearTimer.resetTime();
                            break;
                        }
                    }
                }
            }

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

            // prevent vanilla packets from sending
            event.setCanceled(true);

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

        else {

            // done surrounding
            if (completion.getValue().equals(Completion.SURROUNDED)) {
                toggle();
            }
        }
    }

    @Override
    public boolean isActive() {
        return !replacements.isEmpty() || getCosmos().getInteractionManager().isPlacing();
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {

        // place on packets
        if (timing.getValue().equals(Timing.SEQUENTIAL)) {

            // packet for block changes
            if (event.getPacket() instanceof SPacketBlockChange) {

                // the position of the block change
                BlockPos changePosition = ((SPacketBlockChange) event.getPacket()).getBlockPosition();

                // check if its been changed to air
                if (((SPacketBlockChange) event.getPacket()).getBlockState().getMaterial().isReplaceable()) {

                    // if our placement has been changed then we need to replace it
                    if (placements.contains(changePosition)) {

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

                        // place block
                        if (placeBlock(changePosition)) {
                            // getCosmos().getChatManager().sendClientMessage("placed");
                            placed++;
                        }

                        // switch back to previous slot
                        if (previousSlot != -1) {
                            getCosmos().getInventoryManager().switchToSlot(previousSlot, autoSwitch.getValue());
                        }
                    }
                }
            }

            // packet for multiple block changes
            if (event.getPacket() instanceof SPacketMultiBlockChange) {

                // check each of the updated blocks
                for (SPacketMultiBlockChange.BlockUpdateData blockUpdateData : ((SPacketMultiBlockChange) event.getPacket()).getChangedBlocks()) {

                    // the position of the changed block
                    BlockPos changePosition = blockUpdateData.getPos();

                    // check if its been changed to air
                    if (blockUpdateData.getBlockState().getMaterial().isReplaceable()) {

                        // if our placement has been changed then we need to replace it
                        if (placements.contains(changePosition)) {

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

                            // place block
                            if (placeBlock(changePosition)) {
                                // getCosmos().getChatManager().sendClientMessage("placed");
                                placed++;
                            }

                            // switch back to previous slot
                            if (previousSlot != -1) {
                                getCosmos().getInventoryManager().switchToSlot(previousSlot, autoSwitch.getValue());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Places a block at this position
     * @param in the position
     */
    private boolean placeBlock(BlockPos in) {

        // if we cannot replace the block, cancel
        if (!BlockUtil.isReplaceable(in)) {
            return false;
        }

        // place block
        getCosmos().getInteractionManager().placeBlock(in, rotate.getValue(), strict.getValue(), SAFE_ENTITIES);

        // block placement was successful
        return true;
    }

    /**
     * Gets the valid placements for a given origin
     * @param in The origin
     * @return A list of all the valid placements for a given origin
     */
    private List<BlockPos> getPlacements(BlockPos in) {

        // set of queued block placements
        List<BlockPos> queue = new ArrayList<>();

        // directions that were inhibited
        Set<EnumFacing> inhibitedDirections = new HashSet<>();

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

                // extend surround to encompass the entity
                if (extend.getValue() && center.getValue().equals(Center.NONE)) {

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
                            }
                        }
                    }
                }
            }

            // if an entity is not blocking the placement, we can just queue it
            else {
                queue.add(offset);
            }
        }

        // sometimes blocks in the queue can't realistically be placed on so they need support blocks to place against
        if (support.getValue()) {

            // check each block about to be queued
            for (BlockPos position : new ArrayList<>(queue)) {

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
                    queue.add(position.down());
                }
            }

            // reverse queue since we need support blocks first
            Collections.reverse(queue);
        }

        return queue;
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

    /**
     * Checks if the facing is an extension that cannot be placed on
     * @param in The given facing
     * @return Whether the facing is an extension that cannot be placed on
     */
    private boolean isInvalidDirection(EnumFacing in) {
        return in.equals(EnumFacing.UP) || (!floor.getValue() && in.equals(EnumFacing.DOWN));
    }

    public enum Timing {

        /**
         * Places on each update tick
         */
        VANILLA,

        /**
         * Places when receiving packets
         */
        SEQUENTIAL
    }

    public enum Center {

        /**
         * Teleports the player the center of the block
         */
        TELEPORT,

        /**
         * Moves the player to the center of the block
         */
        MOTION,

        /**
         * Does not attempt to center the player
         */
        NONE
    }

    public enum Completion {

        /**
         * Toggles the module when you have moved out of the block
         */
        SHIFT,

        /**
         * Toggles the module if the player is in a hole
         */
        SURROUNDED,

        /**
         * Does not dynamically toggle the module
         */
        PERSISTENT
    }
}