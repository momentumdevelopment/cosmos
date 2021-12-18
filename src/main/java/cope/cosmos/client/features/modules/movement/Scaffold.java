package cope.cosmos.client.features.modules.movement;

import cope.cosmos.client.events.RenderRotationsEvent;
import cope.cosmos.client.events.RotationUpdateEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.InventoryUtil;
import cope.cosmos.util.player.MotionUtil;
import cope.cosmos.util.player.Rotation;
import cope.cosmos.util.render.RenderBuilder;
import cope.cosmos.util.render.RenderUtil;
import cope.cosmos.util.system.Timer;
import cope.cosmos.util.world.AngleUtil;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static cope.cosmos.util.player.InventoryUtil.Switch;

public class Scaffold extends Module {
    public static Scaffold INSTANCE;

    public Scaffold() {
        super("Scaffold", Category.MOVEMENT, "Places blocks under you");
        INSTANCE = this;
    }

    // block placement options
    public static final Setting<Double> extend = new Setting<>("Extend", 1.0, 1.0, 4.0, 1).setDescription("How far to extend the blocks");
    public static final Setting<Boolean> tower = new Setting<>("Tower", false).setDescription("If to place blocks going upwards rapidly");

    // anti-cheaat
    public static final Setting<Boolean> strict = new Setting<>("Strict", false).setDescription("If to force blocks to be placed with strict servers in mind");

    // interaction options
    public static final Setting<Swap> swap = new Setting<>("Switch", Swap.NORMAL).setDescription("How to switch to the block");
    public static final Setting<Rotation.Rotate> rotate = new Setting<>("Rotate", Rotation.Rotate.PACKET).setDescription("How to rotate when placing blocks");

    // anti-cheat & anti-fall
    public static final Setting<Boolean> safewalk = new Setting<>("Safewalk", true).setDescription("If to attempt to stop you from walking off blocks");
    public static final Setting<Double> delay = new Setting<>("Delay", 0.0, 0.0, 500.0, 1).setDescription("How long to wait before placing another block");

    private final Queue<BlockPos> positions = new ConcurrentLinkedQueue<>();
    private Vec3d currentVector = null;

    private final Timer timer = new Timer();
    private final Timer towerTimer = new Timer();

    private int oldSlot = -1;

    @Override
    public void onRender3D() {
        if (currentVector != null) {
            RenderUtil.drawBox(new RenderBuilder().position(new BlockPos(currentVector.x, currentVector.y, currentVector.z)).box(RenderBuilder.Box.FILL).color(Color.WHITE).blend().depth(true).texture());
        }
    }

    @Override
    public void onUpdate() {
        if (swap.getValue().equals(Swap.NONE) && !InventoryUtil.isHolding(ItemBlock.class)) {
            return;
        } else {
            int slot = InventoryUtil.getItemSlot(ItemBlock.class, InventoryUtil.Inventory.HOTBAR);
            if (slot == -1) {
                return;
            }

            if (slot != mc.player.inventory.currentItem) {
                oldSlot = mc.player.inventory.currentItem;
                InventoryUtil.switchToSlot(slot, swap.getValue().swap);
            }
        }

        if (!timer.passedTime(delay.getValue().longValue(), Timer.Format.SYSTEM)) {
            return;
        }

        positions.removeIf((pos) -> !mc.world.getBlockState(pos).getMaterial().isReplaceable() || mc.player.getDistance(pos.getX(), pos.getY(), pos.getZ()) > extend.getValue());

        // get the block below us
        BlockPos below = new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ);
        if (positions.isEmpty() && mc.world.getBlockState(below).getMaterial().isReplaceable()) {
            addPlacePositions(below); // fetch all the valid place positions
            if (positions.isEmpty()) {
                return;
            }

            BlockPos next = positions.poll(); // get the next position
            if (next == null) {
                return;
            }

            EnumFacing facing = getBestFacing(next); // make sure we can still place the block
            if (facing == null) {
                return;
            }

            timer.resetTime();
            currentVector = new Vec3d(next);
            getCosmos().getInteractionManager().placeBlock(next, rotate.getValue(), strict.getValue());

            // if we're going upwards and the block successfully placed
            if (tower.getValue() && facing == EnumFacing.UP && mc.gameSettings.keyBindJump.isKeyDown() && !mc.world.getBlockState(next).getMaterial().isReplaceable()) {
                mc.player.motionX *= 0.3D;
                mc.player.motionZ *= 0.3D;
                mc.player.jump();

                // after 1.2 seconds, stop vertical motion for anti-cheat compatibility.
                if (towerTimer.passedTime(1200L, Timer.Format.SYSTEM)) {
                    towerTimer.resetTime();
                    mc.player.motionY = -0.28;
                }
            }

            if ((swap.getValue() != Swap.NONE && swap.getValue() != Swap.KEEP) && oldSlot != -1) {
                InventoryUtil.switchToSlot(oldSlot, swap.getValue().swap);
            }
        } else {
            currentVector = null;
        }
    }

    @SubscribeEvent
    public void onRotationUpdate(RotationUpdateEvent event) {
        if (isActive() && rotate.getValue().equals(Rotation.Rotate.PACKET) && currentVector != null) {
            event.setCanceled(true);

            float[] packetAngles = AngleUtil.calculateAngles(currentVector);
            getCosmos().getRotationManager().addRotation(new Rotation(packetAngles[0], packetAngles[1]), 1000);
        }
    }

    @SubscribeEvent
    public void onRenderRotations(RenderRotationsEvent event) {
        if (isActive() && rotate.getValue().equals(Rotation.Rotate.PACKET) && currentVector != null) {
            event.setCanceled(true);

            float[] packetAngles = AngleUtil.calculateAngles(currentVector);
            event.setYaw(packetAngles[0]);
            event.setPitch(packetAngles[1]);
        }
    }

    private void addPlacePositions(BlockPos pos) {
        ArrayList<BlockPos> blocks = new ArrayList<>();
        BlockPos blockPos = new BlockPos(pos);

        double x, z;

        int distance = 0;
        int extendedDistance = extend.getValue().intValue() * 2;

        while (getBestFacing(blockPos) != null) {
            x = mc.player.posX;
            z = mc.player.posZ;

            ++distance;

            if (distance > extendedDistance) {
                distance = extendedDistance;
            } else if (distance == extendedDistance) {
                break;
            }

            double[] motion = MotionUtil.getMoveSpeed(0.45);
            x += motion[0] * distance;
            z += motion[1] * distance;

            BlockPos position = new BlockPos(x, pos.getY(), z);
            if (getBestFacing(position) == null) {
                break;
            }

            blocks.add(blockPos = position);
        }

        if (!blocks.isEmpty()) {
            positions.addAll(blocks);
        }
    }

    private EnumFacing getBestFacing(BlockPos pos) {
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos neighbor = pos.offset(facing);
            if (!mc.world.getBlockState(neighbor).getMaterial().isReplaceable()) {
                return facing;
            }
        }

        return null;
    }

    public enum Swap {
        /**
         * If to not swap to any block and rely on the player to swap to the block themselves
         */
        NONE(Switch.NONE),

        /**
         * If to use packet swapping
         */
        PACKET(Switch.PACKET),

        /**
         * If to use client-sided swapping
         */
        NORMAL(Switch.NORMAL),

        /**
         * Useful on strict servers
         * NCP Updated has a check in scaffold for quick switches, which is flagged by swapping back and forth.
         * This will keep you on the block in your hotbar.
         */
        KEEP(Switch.NORMAL);

        private final Switch swap;

        Swap(Switch swap) {
            this.swap = swap;
        }
    }
}
