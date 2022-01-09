package cope.cosmos.client.features.modules.movement;

import cope.cosmos.asm.mixins.accessor.ICPacketPlayer;
import cope.cosmos.client.events.CollisionBoundingBoxEvent;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.MotionUtil;
import cope.cosmos.util.player.PlayerUtil;
import cope.cosmos.util.system.Timer;
import cope.cosmos.util.system.Timer.Format;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author WolfSurge, aesthetical, linustouchtips
 * @since 01/08/2022
 */
public class Jesus extends Module {
    public static Jesus INSTANCE;

    public Jesus() {
        super("Jesus", Category.MOVEMENT, "Allows you to walk on water");
        INSTANCE = this;
    }

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.SOLID).setDescription("Mode to use when walking on water");

    // float offset
    private double floatOffset;
    private final Timer floatTimer = new Timer();

    @Override
    public void onDisable() {
        super.onDisable();
        floatOffset = 0;
        floatTimer.resetTime();
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
    }

    @Override
    public void onUpdate() {
        // apply liquid deceleration to player
        if (mode.getValue().equals(Mode.SOLID_STRICT)) {
            if (!PlayerUtil.isInLiquid() && isStandingOnLiquid()) {
                // Vec3d decelerationVector = block.modifyAcceleration(mc.world, position, mc.player, Vec3d.ZERO)
                // mc.player.motionX += decelerationVector.x * 0.014;
                // mc.player.motionY += decelerationVector.y * 0.014;
                // mc.player.motionZ += decelerationVector.z * 0.014;
            }
        }

        else if (mode.getValue().equals(Mode.DOLPHIN)) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), PlayerUtil.isInLiquid());
        }
    }

    @SubscribeEvent
    public void onBoundingBoxCollision(CollisionBoundingBoxEvent event) {
        if (event.getEntity() != null && event.getEntity().equals(mc.player)) {
            // check if the collision is with a liquid block
            if (event.getBlock() instanceof BlockLiquid) {
                if (mode.getValue().equals(Mode.SOLID) || mode.getValue().equals(Mode.SOLID_STRICT)) {
                    // make sure the player is standing on liquid not inside liquid
                    if (PlayerUtil.isInLiquid() || !isStandingOnLiquid()) {
                        return;
                    }

                    // make sure the player is not burning
                    if (mc.player.isBurning()) {
                        return;
                    }

                    // we want to fall into the water
                    if (mc.player.fallDistance > 3 || mc.player.isSneaking()) {
                        return;
                    }

                    // full box
                    AxisAlignedBB fullCollisionBox = new AxisAlignedBB(0, 0, 0, 1, 0.921, 1).offset(event.getPosition());

                    // add the full box to collision list
                    if (event.getCollisionBox().intersects(fullCollisionBox)) {
                        event.getCollisionList().add(fullCollisionBox);
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketPlayer) {
            // moving packet
            if (((ICPacketPlayer) event.getPacket()).isMoving()) {
                // check if user is standing in liquid
                if (!PlayerUtil.isInLiquid() && isStandingOnLiquid()) {
                    // prevent on ground packets, bypasses better
                    ((ICPacketPlayer) event.getPacket()).setOnGround(false);

                    // offset y
                    if (mode.getValue().equals(Mode.SOLID)) {
                        if (floatTimer.passedTime(1, Format.TICKS)) {
                            floatOffset = 0;
                            floatTimer.resetTime();
                        }

                        else {
                            floatOffset = 0.2;
                        }
                    }

                    // randomize float
                    else if (mode.getValue().equals(Mode.SOLID_STRICT)) {
                        if (MotionUtil.isMoving()) {
                            if (floatTimer.passedTime(1, Format.TICKS)) {
                                floatOffset = 0.005 - ThreadLocalRandom.current().nextDouble(0, 0.00002);
                                floatTimer.resetTime();
                            }

                            else {
                                floatOffset = 0.02 - ThreadLocalRandom.current().nextDouble(0, 0.000002);
                            }
                        }

                        else {
                            floatOffset = 0.005 - ThreadLocalRandom.current().nextDouble(0, 0.00002);
                        }
                    }

                    // update y offset
                    ((ICPacketPlayer) event.getPacket()).setY(((CPacketPlayer) event.getPacket()).getY(mc.player.posY) - floatOffset);
                }
            }
        }
    }

    /**
     * Checks if the player is standing on a liquid block
     * @return Whether the player is standing on a liquid block
     */
    public boolean isStandingOnLiquid() {
        return mc.world.handleMaterialAcceleration(mc.player.getEntityBoundingBox().grow(0, -3, 0).shrink(0.001), Material.WATER, mc.player) || mc.world.handleMaterialAcceleration(mc.player.getEntityBoundingBox().grow(0, -3, 0).shrink(0.001), Material.LAVA, mc.player);
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
         * Automatically jumps in water
         */
        DOLPHIN
    }
}
