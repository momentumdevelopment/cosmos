package cope.cosmos.client.features.modules.combat;

import cope.cosmos.asm.mixins.accessor.IRenderGlobal;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.InventoryManager.Switch;
import cope.cosmos.util.combat.ExplosionUtil;
import cope.cosmos.util.entity.EntityUtil;
import cope.cosmos.util.holder.Rotation.Rotate;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.player.PlayerUtil;
import cope.cosmos.util.render.RenderBuilder;
import cope.cosmos.util.render.RenderBuilder.Box;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.world.BlockUtil;
import cope.cosmos.util.world.BlockUtil.Resistance;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketMultiBlockChange;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author linustouchtips
 * @since 12/08/2021
 */
public class SurroundModule extends Module {
    public static SurroundModule INSTANCE;

    public SurroundModule() {
        super("Surround", Category.COMBAT, "Surrounds your feet with obsidian");
        INSTANCE = this;
    }

    // **************************** anticheat ****************************

    public static Setting<Timing> timing = new Setting<>("Timing", Timing.SEQUENTIAL)
            .setDescription("When to place blocks");

    public static Setting<Boolean> strict = new Setting<>("Strict", false)
            .setDescription("Only places on visible sides");

    public static Setting<Rotate> rotate = new Setting<>("Rotation", Rotate.NONE)
            .setDescription("Mode for placement rotations");

    public static Setting<Double> blocks = new Setting<>("Blocks", 0.0, 4.0, 10.0, 0)
            .setDescription("Allowed block placements per tick");

    // **************************** general settings ****************************

    public static Setting<SurroundVectors> mode = new Setting<>("Mode", SurroundVectors.BASE)
            .setDescription("Block positions for surround");

    public static Setting<BlockItem> block = new Setting<>("Block", BlockItem.OBSIDIAN)
            .setDescription("Block item to use for surround");

    public static Setting<Completion> completion = new Setting<>("Completion", Completion.AIR)
            .setDescription("When to toggle surround");

    public static Setting<Center> center = new Setting<>("Center", Center.NONE)
            .setDescription("Mode to center the player position");

    public static Setting<Switch> autoSwitch = new Setting<>("Switch", Switch.NORMAL)
            .setDescription("Mode to switch to blocks");

    // **************************** extend ****************************

    public static Setting<Boolean> extend = new Setting<>("Extend", false)
            .setDescription("Extends surround when there is an entity blocking the surround");

    // public static Setting<Boolean> volatiles = new Setting<>("Volatile", false)
    //      .setDescription("Extends surround when the surround is being broken");

    public static Setting<Boolean> scatter = new Setting<>("Scatter", true)
            .setDescription("Clears entities before attempting to place");

    // **************************** render ****************************

    public static Setting<Boolean> render = new Setting<>("Render", true)
            .setDescription("Render a visual of the surround");

    public static Setting<Box> renderMode = new Setting<>("RenderMode", Box.FILL)
            .setDescription("Style of the visual").setExclusion(Box.GLOW, Box.REVERSE);

    // switch info
    private int previousSlot = -1;

    // blocks info
    private int blocksPlaced;
    private List<Vec3i> calculateOffsets = new ArrayList<>();

    // start info
    private double startY;

    @Override
    public void onEnable() {
        super.onEnable();

        // mark our starting height
        startY = mc.player.posY;

        // if we need to be centered
        if (!center.getValue().equals(Center.NONE)) {

            // center positions
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
    public void onUpdate() {

        // we haven't placed on blocks on this tick
        blocksPlaced = 0;

        // pause if we have completed the process
        if (!completion.getValue().equals(Completion.PERSISTENT)) {

            // pause if we are not in the same starting position
            if (completion.getValue().equals(Completion.AIR) && Math.abs(mc.player.posY - startY) > 0.25) {
                disable(true);
                return;
            }

            // pause if we are already in a hole
            if (completion.getValue().equals(Completion.SURROUNDED) && !getCosmos().getHoleManager().isHole(mc.player.getPosition())) {
                disable(true);
                return;
            }
        }

        // offsets after calculations
        calculateOffsets = new ArrayList<>();

        // avoids UnsupportedOperationException
        List<Vec3i> modeOffsets = Arrays.asList(mode.getValue().getVectors());
        calculateOffsets.addAll(modeOffsets);

        for (Vec3i surroundOffset : mode.getValue().getVectors()) {

            // round the player's y position to allow placements if the player is standing on a block that's height is less than 1
            BlockPos playerPositionRounded = new BlockPos(mc.player.posX, Math.round(mc.player.posY), mc.player.posZ);

            // the position to place the block
            BlockPos surroundPosition = playerPositionRounded.add(surroundOffset);

            // make sure there is no entity on the block
            int unsafeEntities = 0;
            for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(surroundPosition))) {

                // can be placed on
                if (entity instanceof EntityItem || entity instanceof EntityXPOrb) {
                    continue;
                }

                // clear area before placing
                if (scatter.getValue()) {

                    // attack crystals that are in the way
                    if (entity instanceof EntityEnderCrystal) {

                        // player health
                        double health = PlayerUtil.getHealth();

                        // damage done by the crystal
                        double crystalDamage = mc.player.capabilities.isCreativeMode ? 0 : ExplosionUtil.getDamageFromExplosion(mc.player, entity.getPositionVector(), false);

                        // explode crystal if it won't kill us
                        if (health - crystalDamage >= 0.1) {

                            // player sprint state
                            boolean sprintState = mc.player.isSprinting();

                            // stop sprinting when attacking an entity
                            if (sprintState) {
                                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                            }

                            // attack
                            mc.player.connection.sendPacket(new CPacketUseEntity(entity));
                            mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));

                            // reset sprint state
                            if (sprintState) {
                                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
                            }
                        }

                        continue;
                    }

                    // attack vehicles 3 times
                    else if (EntityUtil.isVehicleMob(entity)) {

                        if (mc.getConnection() != null) {
                            for (int i = 0; i < 3; i++) {
                                mc.getConnection().getNetworkManager().sendPacket(new CPacketUseEntity(entity));
                                mc.getConnection().getNetworkManager().sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                            }
                        }

                        continue;
                    }
                }

                unsafeEntities++;
            }

            if (unsafeEntities > 0) {

                // extend surround to account for entity hitboxes
                if (extend.getValue() && center.getValue().equals(Center.NONE)) {

                    // extending blocks
                    List<Vec3d> extender = Arrays.asList(
                            new Vec3d(1, 0, 0),
                            new Vec3d(-1, 0, 0),
                            new Vec3d(0, 0, 1),
                            new Vec3d(0, 0, -1)
                    );

                    // extend based on offset
                    extender.forEach(extendOffset -> {
                        Vec3d extend = extendOffset.addVector(surroundOffset.getX(), surroundOffset.getY(), surroundOffset.getZ());

                        // add to offsets
                        calculateOffsets.add(new Vec3i(extend.x, extend.y, extend.z));
                    });

                    // remove centers
                    calculateOffsets.remove(surroundOffset);
                    calculateOffsets.removeIf(vec3i -> vec3i.getX() == 0 && vec3i.getZ() == 0);
                }
            }
        }

        // save the previous slot
        previousSlot = mc.player.inventory.currentItem;

        // switch to obsidian
        getCosmos().getInventoryManager().switchToBlock(block.getValue().getBlock(), autoSwitch.getValue());

        if (InventoryUtil.isHolding(block.getValue().getBlock())) {

            // place on each of the calculated offsets
            for (Vec3i surroundOffset : calculateOffsets) {

                // round the player's y position to allow placements if the player is standing on a block that's height is less than 1
                BlockPos playerPositionRounded = new BlockPos(mc.player.posX, Math.round(mc.player.posY), mc.player.posZ);

                // the position to place the block
                BlockPos surroundPosition = playerPositionRounded.add(surroundOffset);

                // check that the block can be replaced by obsidian
                if (BlockUtil.getResistance(surroundPosition).equals(Resistance.REPLACEABLE)) {

                    // make sure we haven't placed too many blocks this tick
                    if (blocksPlaced <= blocks.getValue()) {
                        blocksPlaced++;

                        // place a block
                        getCosmos().getInteractionManager().placeBlock(surroundPosition, rotate.getValue(), strict.getValue());
                    }
                }
            }
        }

        // switch back to our previous item
        if (previousSlot != -1) {
            getCosmos().getInventoryManager().switchToSlot(previousSlot, autoSwitch.getValue());

            // reset previous slot info
            previousSlot = -1;
        }
    }

    @Override
    public void onRender3D() {
        if (render.getValue()) {

            // render all of the surround blocks
            for (Vec3i surroundOffset : calculateOffsets) {

                // round the player's y position to allow placements if the player is standing on a block that's height is less than 1
                BlockPos playerPositionRounded = new BlockPos(mc.player.posX, Math.round(mc.player.posY), mc.player.posZ);

                // the position to place the block
                BlockPos surroundPosition = playerPositionRounded.add(surroundOffset);

                // find if the block is safe or not
                boolean safeBlock = BlockUtil.getResistance(surroundPosition).equals(Resistance.RESISTANT) || BlockUtil.getResistance(surroundPosition).equals(Resistance.UNBREAKABLE);

                // find if the block is being broken
                AtomicBoolean breakBlock = new AtomicBoolean(false);
                ((IRenderGlobal) mc.renderGlobal).getDamagedBlocks().forEach((integer, destroyBlockProgress) -> {
                    if (destroyBlockProgress.getPosition().equals(surroundPosition)) {
                        breakBlock.set(true);
                    }
                });

                RenderUtil.drawBox(new RenderBuilder()
                        .position(surroundPosition)
                        .color(safeBlock ? (breakBlock.get() ? new Color(255, 255, 0, 40) : new Color(0, 255, 0, 40)) : new Color(255, 0, 0, 40))
                        .box(renderMode.getValue())
                        .setup()
                        .line(1.5F)
                        .cull(renderMode.getValue().equals(Box.GLOW) || renderMode.getValue().equals(Box.REVERSE))
                        .shade(renderMode.getValue().equals(Box.GLOW) || renderMode.getValue().equals(Box.REVERSE))
                        .alpha(renderMode.getValue().equals(Box.GLOW) || renderMode.getValue().equals(Box.REVERSE))
                        .depth(true)
                        .blend()
                        .texture()
                );
            }
        }
    }

    @Override
    public boolean isActive() {
        boolean isSurrounding = true;
        for (Vec3i surroundOffset : calculateOffsets) {

            // round the player's y position to allow placements if the player is standing on a block that's height is less than 1
            BlockPos playerPositionRounded = new BlockPos(mc.player.posX, Math.round(mc.player.posY), mc.player.posZ);

            // the position to place the block
            BlockPos surroundPosition = playerPositionRounded.add(surroundOffset);

            // find if the block is safe or not
            if (!BlockUtil.getResistance(surroundPosition).equals(Resistance.RESISTANT) && !BlockUtil.getResistance(surroundPosition).equals(Resistance.UNBREAKABLE)) {
                isSurrounding = false;
                break;
            }
        }

        return isSurrounding;
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {

        // packet for block changes
        if (event.getPacket() instanceof SPacketBlockChange) {

            if (timing.getValue().equals(Timing.SEQUENTIAL)) {
                // check if the block is now replaceable
                if (((SPacketBlockChange) event.getPacket()).getBlockState().getMaterial().isReplaceable()) {

                    // the position of the block change
                    BlockPos changePosition = ((SPacketBlockChange) event.getPacket()).getBlockPosition();

                    // check each of the offsets
                    for (Vec3i surroundOffset : calculateOffsets) {

                        // round the player's y position to allow placements if the player is standing on a block that's height is less than 1
                        BlockPos playerPositionRounded = new BlockPos(mc.player.posX, Math.round(mc.player.posY), mc.player.posZ);

                        // the position to place the block
                        BlockPos surroundPosition = playerPositionRounded.add(surroundOffset);

                        if (changePosition.equals(surroundPosition)) {
                            // save the previous slot
                            previousSlot = mc.player.inventory.currentItem;

                            // switch to obsidian
                            getCosmos().getInventoryManager().switchToBlock(block.getValue().getBlock(), autoSwitch.getValue());

                            if (InventoryUtil.isHolding(block.getValue().getBlock())) {
                                // update blocks placed
                                blocksPlaced++;

                                // place a block
                                getCosmos().getInteractionManager().placeBlock(changePosition, rotate.getValue(), strict.getValue());
                            }

                            // switch back to our previous item
                            if (previousSlot != -1) {
                                getCosmos().getInventoryManager().switchToSlot(previousSlot, autoSwitch.getValue());

                                // reset previous slot info
                                previousSlot = -1;
                            }

                            break;
                        }
                    }
                }
            }
        }

        // packet for multiple block changes
        if (event.getPacket() instanceof SPacketMultiBlockChange) {
            if (timing.getValue().equals(Timing.SEQUENTIAL)) {

                // check each of the updated blocks
                for (SPacketMultiBlockChange.BlockUpdateData blockUpdateData : ((SPacketMultiBlockChange) event.getPacket()).getChangedBlocks()) {

                    // check if the block is now replaceable
                    if (blockUpdateData.getBlockState().getMaterial().isReplaceable()) {

                        // the position of the changed block
                        BlockPos changePosition = blockUpdateData.getPos();

                        // check each of the offsets
                        for (Vec3i surroundOffset : calculateOffsets) {

                            // round the player's y position to allow placements if the player is standing on a block that's height is less than 1
                            BlockPos playerPositionRounded = new BlockPos(mc.player.posX, Math.round(mc.player.posY), mc.player.posZ);

                            // the position to place the block
                            BlockPos surroundPosition = playerPositionRounded.add(surroundOffset);

                            if (changePosition.equals(surroundPosition)) {
                                
                                // save the previous slot
                                previousSlot = mc.player.inventory.currentItem;

                                // switch to obsidian
                                getCosmos().getInventoryManager().switchToBlock(block.getValue().getBlock(), autoSwitch.getValue());

                                if (InventoryUtil.isHolding(block.getValue().getBlock())) {
                                    
                                    // update blocks placed
                                    blocksPlaced++;

                                    // place a block
                                    getCosmos().getInteractionManager().placeBlock(changePosition, rotate.getValue(), strict.getValue());
                                }

                                // switch back to our previous item
                                if (previousSlot != -1) {
                                    getCosmos().getInventoryManager().switchToSlot(previousSlot, autoSwitch.getValue());

                                    // reset previous slot info
                                    previousSlot = -1;
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // packet for crystal explosions
        if (event.getPacket() instanceof SPacketSoundEffect && ((SPacketSoundEffect) event.getPacket()).getSound().equals(SoundEvents.ENTITY_GENERIC_EXPLODE) && ((SPacketSoundEffect) event.getPacket()).getCategory().equals(SoundCategory.BLOCKS)) {

            if (scatter.getValue()) {

                // the position of the changed block
                BlockPos soundPosition = new BlockPos(((SPacketSoundEffect) event.getPacket()).getX(), ((SPacketSoundEffect) event.getPacket()).getY(), ((SPacketSoundEffect) event.getPacket()).getZ());

                // check each of the offsets
                for (Vec3i surroundOffset : calculateOffsets) {

                    // round the player's y position to allow placements if the player is standing on a block that's height is less than 1
                    BlockPos playerPositionRounded = new BlockPos(mc.player.posX, Math.round(mc.player.posY), mc.player.posZ);

                    // the position to place the block
                    BlockPos surroundPosition = playerPositionRounded.add(surroundOffset);

                    if (soundPosition.equals(surroundPosition)) {

                        // save the previous slot
                        previousSlot = mc.player.inventory.currentItem;

                        // switch to obsidian
                        getCosmos().getInventoryManager().switchToBlock(block.getValue().getBlock(), autoSwitch.getValue());

                        if (InventoryUtil.isHolding(block.getValue().getBlock())) {

                            // update blocks placed
                            blocksPlaced++;

                            // place a block
                            getCosmos().getInteractionManager().placeBlock(soundPosition, rotate.getValue(), strict.getValue());
                        }

                        // switch back to our previous item
                        if (previousSlot != -1) {
                            getCosmos().getInventoryManager().switchToSlot(previousSlot, autoSwitch.getValue());

                            // reset previous slot info
                            previousSlot = -1;
                        }
                    }
                }
            }
        }

            // packet for block breaking animation
        if (event.getPacket() instanceof SPacketBlockBreakAnim) {

            /*

            getCosmos().getChatManager().sendClientMessage(((SPacketBlockBreakAnim) event.getPacket()).getProgress());

            if (volatiles.getValue()) {
                if () {

                    // the position of the block change
                    BlockPos changePosition = ((SPacketBlockBreakAnim) event.getPacket()).getPosition();

                    // check each of the offsets
                    for (Vec3i surroundOffset : calculateOffsets) {

                        // round the player's y position to allow placements if the player is standing on a block that's height is less than 1
                        BlockPos playerPositionRounded = new BlockPos(mc.player.posX, Math.round(mc.player.posY), mc.player.posZ);

                        // the position to place the block
                        BlockPos surroundPosition = playerPositionRounded.add(surroundOffset);

                        if (changePosition.equals(surroundPosition)) {
                            // save the previous slot
                            previousSlot = mc.player.inventory.currentItem;

                            // switch to obsidian
                            getCosmos().getInventoryManager().switchToBlock(block.getValue().getBlock(), autoSwitch.getValue());

                            // extending blocks
                            List<Vec3i> extender = Arrays.asList(
                                    new Vec3i(1, 0, 0),
                                    new Vec3i(-1, 0, 0),
                                    new Vec3i(0, 0, 1),
                                    new Vec3i(0, 0, -1)
                            );

                            // surround each extender
                            for (Vec3i extend : extender) {

                                // extended position
                                BlockPos extendPosition = changePosition.add(extend);

                                if (InventoryUtil.isHolding(block.getValue().getBlock())) {
                                    // update blocks placed
                                    blocksPlaced++;

                                    // place a block
                                    getCosmos().getInteractionManager().placeBlock(extendPosition, rotate.getValue(), strict.getValue());
                                }
                            }

                            // switch back to our previous item
                            if (previousSlot != -1) {
                                getCosmos().getInventoryManager().switchToSlot(previousSlot, autoSwitch.getValue());

                                // reset previous slot info
                                previousSlot = -1;
                            }

                            break;
                        }
                    }
                }
            }

             */
        }
    }

    public enum SurroundVectors {

        /**
         * Surrounds the lower offsets of the hole, Works better on ledges -> Rarely works on Updated NCP
         */
        BASE(
                new Vec3i(0, -1, 0),
                new Vec3i(1, -1, 0),
                new Vec3i(0, -1, 1),
                new Vec3i(-1, -1, 0),
                new Vec3i(0, -1, -1),
                new Vec3i(1, 0, 0),
                new Vec3i(0, 0, 1),
                new Vec3i(-1, 0, 0),
                new Vec3i(0, 0, -1)
        ),

        /**
         * Covers all sides of the player
         */
        STANDARD(
                new Vec3i(0, -1, 0),
                new Vec3i(1, 0, 0),
                new Vec3i(-1, 0, 0),
                new Vec3i(0, 0, 1),
                new Vec3i(0, 0, -1)
        ),

        /**
         * Doubles up on each offset of the surround for extra protection
         */
        PROTECT(
                new Vec3i(0, -1, 0),
                new Vec3i(1, 0, 0),
                new Vec3i(-1, 0, 0),
                new Vec3i(0, 0, 1),
                new Vec3i(0, 0, -1),
                new Vec3i(2, 0, 0),
                new Vec3i(-2, 0, 0),
                new Vec3i(0, 0, 2),
                new Vec3i(0, 0, -2),
                new Vec3i(3, 0, 0),
                new Vec3i(-3, 0, 0),
                new Vec3i(0, 0, 3),
                new Vec3i(0, 0, -3)
        );

        private final Vec3i[] vectors;

        SurroundVectors(Vec3i... vectors) {
            this.vectors = vectors;
        }

        /**
         * Gets the vector offsets for the surround mode
         * @return The vector offsets for the surround mode
         */
        public Vec3i[] getVectors() {
            return vectors;
        }
    }

    public enum Timing {

        /**
         * Places on each update tick
         */
        TICK,

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
        AIR,

        /**
         * Toggles the module if the player is in a hole
         */
        SURROUNDED,

        /**
         * Does not dynamically toggle the module
         */
        PERSISTENT
    }

    private enum BlockItem {

        /**
         * Obsidian
         */
        OBSIDIAN(Blocks.OBSIDIAN),

        /**
         * Chests, Blocks with 0.8 height
         */
        ENDER_CHEST(Blocks.ENDER_CHEST);

        private final Block block;

        BlockItem(Block block) {
            this.block = block;
        }

        /**
         * Get the blocks associated with the mode
         * @return The blocks associated with the mode
         */
        public Block getBlock() {
            return block;
        }
    }
}