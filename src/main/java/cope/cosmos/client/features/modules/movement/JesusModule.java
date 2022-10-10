package cope.cosmos.client.features.modules.movement;

import cope.cosmos.asm.mixins.accessor.ICPacketPlayer;
import cope.cosmos.client.events.motion.collision.CollisionBoundingBoxEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.exploits.PacketFlightModule;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.PlayerUtil;
import cope.cosmos.util.string.StringFormatter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author aesthetical, linustouchtips, Surge
 * @since 01/08/2022
 */
public class JesusModule extends Module {
    public static JesusModule INSTANCE;

    public JesusModule() {
        super("Jesus", new String[] {"WaterWalk"}, Category.MOVEMENT, "Allows you to walk on water", () -> StringFormatter.formatEnum(mode.getValue()));
        INSTANCE = this;
    }

    // **************************** general ****************************

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.SOLID)
            .setDescription("Mode to use when walking on water");

    // float offset
    private double floatOffset;
    private int floatTicks = 1000;

    @Override
    public void onDisable() {
        super.onDisable();

        // reset process
        floatOffset = 0;

        // prevents non-water floating
        floatTicks = 1000;
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void onUpdate() {

        // alternate offsets
        if (mode.getValue().equals(Mode.SOLID)) {

            // reset
            if (floatOffset == 0.05) {
                floatOffset = 0;
            }

            // offset
            else if (floatOffset == 0) {
                floatOffset = 0.05;
            }
        }

        // incompatibilities
        if (PacketFlightModule.INSTANCE.isEnabled() || FlightModule.INSTANCE.isEnabled()) {
            return;
        }

        // apply liquid deceleration to player
        if (mode.getValue().equals(Mode.SOLID) || mode.getValue().equals(Mode.SOLID_STRICT)) {
            if (!PlayerUtil.isInLiquid() && isStandingOnLiquid()) {
                // Vec3d decelerationVector = block.modifyAcceleration(mc.world, position, mc.player, Vec3d.ZERO)
                // mc.player.motionX += decelerationVector.x * 0.014;
                // mc.player.motionY += decelerationVector.y * 0.014;
                // mc.player.motionZ += decelerationVector.z * 0.014;
            }

            // reset offset
            if (PlayerUtil.isInLiquid() || mc.player.fallDistance > 3 || mc.player.isSneaking()) {
                floatOffset = 0;
            }

            // check if we are not canceling the motion
            if (!mc.player.isSneaking() && !mc.gameSettings.keyBindJump.isKeyDown()) {

                // float up
                if (PlayerUtil.isInLiquid()) {
                    mc.player.motionY = 0.11;

                    // reset float stage
                    floatTicks = 0;
                    return;
                }

                // float above water
                if (floatTicks > 0 && floatTicks < 5) {
                    mc.player.motionY = 0.11;
                }

                floatTicks++;
             }
        }

        // floats up while in the water
        else if (mode.getValue().equals(Mode.DOLPHIN)) {
            if (PlayerUtil.isInLiquid()) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), true);
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @SubscribeEvent
    public void onBoundingBoxCollision(CollisionBoundingBoxEvent event) {
        if (nullCheck()) {

            // incompatibilities
            if (PacketFlightModule.INSTANCE.isEnabled() || FlightModule.INSTANCE.isEnabled()) {
                return;
            }

            // allow if we are spectating
            if (mc.player.isSpectator()) {
                return;
            }

            // check if we are the ones colliding
            if (event.getEntity() != null && event.getEntity().equals(mc.player)) {

                // check if the collision is with a liquid block
                if (event.getBlock() instanceof BlockLiquid) {

                    // make the block solid
                    if (mode.getValue().equals(Mode.SOLID) || mode.getValue().equals(Mode.SOLID_STRICT)) {

                        // make sure the player is standing on liquid not inside liquid
                        if (PlayerUtil.isInLiquid() || !isStandingOnLiquid()) {
                            return;
                        }

                        // make sure the player is not burning, dip down
                        if (mc.player.isBurning()) {
                            return;
                        }

                        // we want to fall into the water
                        if (mc.player.fallDistance > 3 || mc.player.isSneaking() || mc.player.isRowingBoat()) {
                            return;
                        }

                        // full box
                        AxisAlignedBB fullCollisionBox = Block.FULL_BLOCK_AABB.offset(event.getPosition());

                        // add the full box to collision list
                        if (event.getCollisionBox().intersects(fullCollisionBox)) {
                            event.getCollisionList().add(fullCollisionBox);
                            event.setCanceled(true);
                        }

                        // decelerate the player motion (simulate moving through liquid)
                        if (mode.getValue().equals(Mode.SOLID_STRICT)) {
                            // Vec3d decelerationVector = event.getBlock().modifyAcceleration(mc.world, event.getPosition(), mc.player, Vec3d.ZERO);
                            // mc.player.motionX += decelerationVector.x * 0.014;
                            // mc.player.motionY += decelerationVector.y * 0.014;
                            // mc.player.motionZ += decelerationVector.z * 0.014;
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {

        // packet for player positions
        if (event.getPacket() instanceof CPacketPlayer) {

            // moving packet
            if (((ICPacketPlayer) event.getPacket()).isMoving()) {

                // check if user is standing in liquid
                if (!PlayerUtil.isInLiquid() && isStandingOnLiquid()) {

                    // wait for floating
                    if (mode.getValue().equals(Mode.SOLID) && floatTicks < 5) {
                        return;
                    }

                    // y position
                    double y = ((CPacketPlayer) event.getPacket()).getY(mc.player.posY);

                    // prevent on ground packets, bypasses better
                    ((ICPacketPlayer) event.getPacket()).setOnGround(false);

                    // offset y
                    if (mode.getValue().equals(Mode.SOLID) || mode.getValue().equals(Mode.SOLID_STRICT)) {

                        // update y offset
                        ((ICPacketPlayer) event.getPacket()).setY(y - floatOffset);

                        // update offsets
                        if (mode.getValue().equals(Mode.SOLID_STRICT)) {

                            // increase dip
                            floatOffset += 0.12;

                            // clamp
                            if (floatOffset > 0.4) {
                                floatOffset = 0.2;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks if the player is standing on a liquid block
     * @return Whether the player is standing on a liquid block
     */
    public boolean isStandingOnLiquid() {

        // check if we are inside a liquid
        if (PlayerUtil.isInLiquid()) {
            return false;
        }

        // check below positions
        for (double y = 0; y < 1; y += 0.1) {

            // below position
            BlockPos position = new BlockPos(mc.player.posX, mc.player.getEntityBoundingBox().minY - y, mc.player.posZ);

            // below block
            Block block = mc.world.getBlockState(position).getBlock();

            // check if liquid
            if (block instanceof BlockLiquid && !block.equals(Blocks.FLOWING_LAVA) && !block.equals(Blocks.FLOWING_WATER)) {
                return true;
            }
        }

        return false;
    }

    public enum Mode {

        /**
         * Allows you to walk on water as if it were a solid block
         */
        SOLID,

        /**
         * Allows you to walk on water as if it were a solid block, Bypasses NCP Updated
         */
        SOLID_STRICT,

        /**
         * Automatically floats up in water
         */
        DOLPHIN
    }
}
