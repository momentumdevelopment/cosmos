package cope.cosmos.client.features.modules.combat;

import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.player.InventoryUtil.Switch;
import cope.cosmos.util.player.Rotation.Rotate;
import cope.cosmos.util.render.RenderBuilder;
import cope.cosmos.util.render.RenderBuilder.Box;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.world.BlockUtil;
import cope.cosmos.util.world.BlockUtil.*;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketMultiBlockChange;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

/**
 * @author linustouchtips
 * @since 12/08/2021
 */
@SuppressWarnings("unused")
public class Surround extends Module {
    public static Surround INSTANCE;

    public Surround() {
        super("Surround", Category.COMBAT, "Surrounds your feet with obsidian");
        INSTANCE = this;
    }

    public static Setting<SurroundVectors> mode = new Setting<>("Mode", SurroundVectors.BASE).setDescription("Block positions for surround");
    public static Setting<BlockItem> block = new Setting<>("Block", BlockItem.OBSIDIAN).setDescription("Block item to use for surround");
    public static Setting<Completion> completion = new Setting<>("Completion", Completion.AIR).setDescription("When to toggle surround");
    public static Setting<Center> center = new Setting<>("Center", Center.NONE).setDescription("Mode to center the player position");

    public static Setting<Switch> autoSwitch = new Setting<>("Switch", Switch.NORMAL).setDescription("Mode to switch to blocks");

    public static Setting<Double> blocks = new Setting<>("Blocks", 0.0, 4.0, 10.0, 0).setDescription("Allowed block placements per tick");

    public static Setting<Boolean> strict = new Setting<>("Strict", false).setDescription("Only places on visible sides");
    public static Setting<Boolean> reactive = new Setting<>("Reactive", true).setDescription("Replaces surround blocks when they break");

    public static Setting<Rotate> rotate = new Setting<>("Rotation", Rotate.NONE).setDescription("Mode for placement rotations");

    public static Setting<Boolean> render = new Setting<>("Render", true).setDescription("Render a visual of the surround");
    public static Setting<Box> renderMode = new Setting<>("Mode", Box.FILL).setParent(render).setDescription("Style of the visual");

    // switch info
    private int previousSlot = -1;

    // blocks info
    private int blocksPlaced = 0;

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
                disable();
                return;
            }

            // pause if we are already in a hole
            if (completion.getValue().equals(Completion.SURROUNDED) && !getCosmos().getHoleManager().isInHole(mc.player)) {
                disable();
                return;
            }
        }

        // save the previous slot
        previousSlot = mc.player.inventory.currentItem;

        // switch to obsidian
        InventoryUtil.switchToSlot(Item.getItemFromBlock(block.getValue().getBlock()), autoSwitch.getValue());

        // place on each of the offsets
        for (Vec3i surroundOffset : mode.getValue().getVectors()) {

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

        // switch back to our previous item
        if (previousSlot != -1) {
            InventoryUtil.switchToSlot(previousSlot, autoSwitch.getValue());

            // reset previous slot info
            previousSlot = -1;
        }
    }

    @Override
    public void onRender3D() {
        if (render.getValue()) {

            // render all of the surround blocks
            for (Vec3i surroundOffset : mode.getValue().getVectors()) {

                // round the player's y position to allow placements if the player is standing on a block that's height is less than 1
                BlockPos playerPositionRounded = new BlockPos(mc.player.posX, Math.round(mc.player.posY), mc.player.posZ);

                // the position to place the block
                BlockPos surroundPosition = playerPositionRounded.add(surroundOffset);

                // find if the block is safe or not
                boolean safeBlock = BlockUtil.getResistance(surroundPosition).equals(Resistance.RESISTANT) || BlockUtil.getResistance(surroundPosition).equals(Resistance.UNBREAKABLE);

                RenderUtil.drawBox(new RenderBuilder()
                        .position(surroundPosition)
                        .color(safeBlock ? new Color(0, 255, 0, 40) : new Color(255, 0, 0, 40))
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

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {
        // packet for block changes
        if (event.getPacket() instanceof SPacketBlockChange) {

            if (reactive.getValue()) {
                // check if the block is now replaceable
                if (((SPacketBlockChange) event.getPacket()).getBlockState().getMaterial().isReplaceable()) {

                    // the position of the block change
                    BlockPos changePosition = ((SPacketBlockChange) event.getPacket()).getBlockPosition();

                    // check each of the offsets
                    for (Vec3i surroundOffset : mode.getValue().getVectors()) {

                        // round the player's y position to allow placements if the player is standing on a block that's height is less than 1
                        BlockPos playerPositionRounded = new BlockPos(mc.player.posX, Math.round(mc.player.posY), mc.player.posZ);

                        // the position to place the block
                        BlockPos surroundPosition = playerPositionRounded.add(surroundOffset);

                        if (changePosition.equals(surroundPosition)) {
                            // save the previous slot
                            previousSlot = mc.player.inventory.currentItem;

                            // switch to obsidian
                            InventoryUtil.switchToSlot(Item.getItemFromBlock(block.getValue().getBlock()), autoSwitch.getValue());

                            // update blocks placed
                            blocksPlaced++;

                            // place a block
                            getCosmos().getInteractionManager().placeBlock(changePosition, rotate.getValue(), strict.getValue());

                            // switch back to our previous item
                            if (previousSlot != -1) {
                                InventoryUtil.switchToSlot(previousSlot, autoSwitch.getValue());

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
            if (reactive.getValue()) {

                // check each of the updated blocks
                for (SPacketMultiBlockChange.BlockUpdateData blockUpdateData : ((SPacketMultiBlockChange) event.getPacket()).getChangedBlocks()) {

                    // check if the block is now replaceable
                    if (blockUpdateData.getBlockState().getMaterial().isReplaceable()) {

                        // the position of the changed block
                        BlockPos changePosition = blockUpdateData.getPos();

                        // check each of the offsets
                        for (Vec3i surroundOffset : mode.getValue().getVectors()) {

                            // round the player's y position to allow placements if the player is standing on a block that's height is less than 1
                            BlockPos playerPositionRounded = new BlockPos(mc.player.posX, Math.round(mc.player.posY), mc.player.posZ);

                            // the position to place the block
                            BlockPos surroundPosition = playerPositionRounded.add(surroundOffset);

                            if (changePosition.equals(surroundPosition)) {
                                // save the previous slot
                                previousSlot = mc.player.inventory.currentItem;

                                // switch to obsidian
                                InventoryUtil.switchToSlot(Item.getItemFromBlock(block.getValue().getBlock()), autoSwitch.getValue());

                                // update blocks placed
                                blocksPlaced++;

                                // place a block
                                getCosmos().getInteractionManager().placeBlock(changePosition, rotate.getValue(), strict.getValue());

                                // switch back to our previous item
                                if (previousSlot != -1) {
                                    InventoryUtil.switchToSlot(previousSlot, autoSwitch.getValue());

                                    // reset previous slot info
                                    previousSlot = -1;
                                }
                            }
                        }
                    }
                }
            }
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