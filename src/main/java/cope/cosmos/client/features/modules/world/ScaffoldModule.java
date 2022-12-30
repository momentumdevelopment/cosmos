package cope.cosmos.client.features.modules.world;

import com.mojang.realmsclient.util.Pair;
import cope.cosmos.client.events.entity.player.RotationUpdateEvent;
import cope.cosmos.client.events.motion.movement.MotionUpdateEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.InventoryManager.Switch;
import cope.cosmos.util.holder.Rotation;
import cope.cosmos.util.math.Timer;
import cope.cosmos.util.math.Timer.Format;
import cope.cosmos.util.player.AngleUtil;
import cope.cosmos.util.world.BlockUtil;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author aesthetical
 * @since 12/15/2022
 */
public class ScaffoldModule extends Module {
    public static ScaffoldModule INSTANCE;

    public ScaffoldModule() {
        super("Scaffold", new String[] {"BlockFly"}, Category.WORLD, "Places blocks under you");
        INSTANCE = this;
    }

    public static Setting<Double> delay = new Setting<>("Delay", 0.0, 50.0, 2000.0, 0)
            .setAlias("PlaceDelay")
            .setDescription("How long to wait before placing another block");

    public static Setting<Boolean> safewalk = new Setting<>("SafeWalk", false)
            .setAlias("Eagle", "NoFall")
            .setDescription("If to automatically safewalk when placing blocks");

    public static Setting<Boolean> swap = new Setting<>("Swap", true)
            .setAlias("Switch", "SwapBlock", "BlockPicker", "Picker")
            .setDescription("If to automatically swap to a block in your hotbar");

    public static Setting<Boolean> rotate = new Setting<>("Rotate", true)
            .setAlias("Rot", "Face", "FaceBlock")
            .setDescription("If to rotate towards the block placed");

    public static Setting<Tower> tower = new Setting<>("Tower", Tower.FAST)
            .setDescription("If to quickly move upwards when holding down space");


    private final Timer placeTimer = new Timer();
    private final Timer towerTimer = new Timer();

    private Pair<BlockPos, EnumFacing> previous, current;
    private Rotation rotations = Rotation.INVALID_ROTATION;

    @Override
    public void onDisable() {
        super.onDisable();

        if (nullCheck()) {
            return;
        }

        rotations = Rotation.INVALID_ROTATION;
        previous = null;
        current = null;

        getCosmos().getInventoryManager().switchToSlot(mc.player.inventory.currentItem, Switch.NORMAL);
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onMotionUpdate(MotionUpdateEvent event) {

        if (current == null) {
            return;
        }

        int slot = getSlot();
        if (slot == -1) {
            return;
        }

        // TODO: fix rotation system (thanks linus) so scaffold works on NCP Updated
        if (placeTimer.passedTime(delay.getValue().longValue(), Format.MILLISECONDS)) {

            if (getCosmos().getInventoryManager().getServerSlot() != slot) {

                if (!swap.getValue() && mc.player.inventory.currentItem != slot) {
                    return;
                }

                if (swap.getValue()) {
                    getCosmos().getInventoryManager().switchToSlot(slot, Switch.NORMAL);
                }
            }

            EnumActionResult actionResult = mc.playerController.processRightClickBlock(
                    mc.player,
                    mc.world,
                    current.first(),
                    current.second(),
                    getHitVec(current),
                    EnumHand.MAIN_HAND
            );

            if (!actionResult.equals(EnumActionResult.FAIL)) {

                placeTimer.resetTime();
                mc.player.swingArm(EnumHand.MAIN_HAND);

                if (mc.gameSettings.keyBindJump.isKeyDown()) {

                    switch (tower.getValue()) {
                        case SLOW:
                        case FAST:

                            if (tower.getValue().equals(Tower.FAST)) {
                                mc.player.jump();
                            } else {
                                mc.player.motionY = 0.2;
                            }

                            mc.player.motionX *= 0.3;
                            mc.player.motionZ *= 0.3;

                            if (towerTimer.passedTime(1200L, Format.MILLISECONDS)) {
                                towerTimer.resetTime();
                                mc.player.motionY = -0.28;
                            }
                            break;

                        case NONE:
                            break;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onRotationUpdate(RotationUpdateEvent event) {
        if (current != null) {
            previous = current;
        }

        current = get();

        if (previous != null && rotate.getValue()) {
            Rotation angles = AngleUtil.calculateAngles(getHitVec(previous));
            if (angles.isValid()) {

                if (rotations.isValid()) {

                    float yawDifference = rotations.getYaw() - angles.getYaw();

                    if (yawDifference > 40.0f) {
                        angles.setYaw(MathHelper.wrapDegrees(rotations.getYaw() - 20.0f));
                    } else if (yawDifference < -40.0f) {
                        angles.setYaw(MathHelper.wrapDegrees(rotations.getYaw() + 20.0f));
                    }

                }

                rotations = angles;
            }
        } else {
            rotations = Rotation.INVALID_ROTATION;
        }

        if (rotations.isValid()) {
            getCosmos().getRotationManager().setRotation(rotations);
        }

        if (current != null) {
            event.setCanceled(true);
        }
    }

    private int getSlot() {
        int s = -1;
        int count = 0;

        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemBlock) {

                // TODO: check if we can place with this block so we dont go placing flowers and shit

                if (s == -1 || stack.getCount() > count) {
                    s = i;
                    count = stack.getCount();
                }
            }
        }

        return s;
    }

    /**
     * Gets the raytrace hit vector
     * @param in The placement
     * @return The raytrace hit vector
     */
    private Vec3d getHitVec(Pair<BlockPos, EnumFacing> in) {
        return new Vec3d(in.first()
                .offset(in.second()))
                .addVector(0.5, 0.5, 0.5)
                .add(new Vec3d(in.second().getOpposite().getDirectionVec())
                        .scale(0.5));
    }

    private Pair<BlockPos, EnumFacing> get() {
        BlockPos pos = new BlockPos(mc.player.getPositionVector()).down();

        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos neighbor = pos.offset(facing);
            if (!BlockUtil.isReplaceable(neighbor)) {
                return Pair.of(neighbor, facing.getOpposite());
            }
        }

        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos neighbor = pos.offset(facing);
            if (BlockUtil.isReplaceable(neighbor)) {
                for (EnumFacing direction : EnumFacing.values()) {
                    BlockPos neighbor2 = neighbor.offset(direction);
                    if (!BlockUtil.isReplaceable(neighbor2)) {
                        return Pair.of(neighbor2, direction.getOpposite());
                    }
                }
            }
        }

        return null;
    }

    public enum Tower {
        /**
         * Do not tower
         */
        NONE,

        /**
         * Slower tower, may bypass better on NCP Updated
         */
        SLOW,

        /**
         * Faster tower, should work great on normal NCP
         */
        FAST
    }
}