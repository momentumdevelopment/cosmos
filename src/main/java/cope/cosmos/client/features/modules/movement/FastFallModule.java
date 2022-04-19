package cope.cosmos.client.features.modules.movement;

import cope.cosmos.asm.mixins.accessor.IEntity;
import cope.cosmos.asm.mixins.accessor.IEntityPlayerSP;
import cope.cosmos.client.events.motion.movement.MotionEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.math.Timer;
import cope.cosmos.util.math.Timer.Format;
import cope.cosmos.util.player.PlayerUtil;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 04/18/2022
 */
public class FastFallModule extends Module {
    public static FastFallModule INSTANCE;

    public FastFallModule() {
        super("FastFall", Category.MOVEMENT, "Falls faster");
        INSTANCE = this;
    }

    // **************************** general ****************************

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.MOTION)
            .setDescription("Mode for falling");

    public static Setting<Double> speed = new Setting<>("Speed", 0.0, 1.0, 10.0, 2)
            .setDescription("Fall speed")
            .setVisible(() -> !mode.getValue().equals(Mode.PACKET));

    public static Setting<Double> shiftTicks = new Setting<>("ShiftTicks", 1.0, 1.0, 5.0, 0)
            .setDescription("Ticks to shift forward when falling")
            .setVisible(() -> mode.getValue().equals(Mode.PACKET));

    public static Setting<Boolean> webs = new Setting<>("Webs", false)
            .setDescription("Falls in webs");

    // state
    public boolean colliding;
    public boolean moving;

    // strict fall timer
    private final Timer strictTimer = new Timer();

    @SubscribeEvent
    public void onMove(MotionEvent event) {

        getCosmos().getTickManager().setClientTicks(1);

        // we are already moving, no need to run this event again
        if (moving) {
            return;
        }

        // NCP will flag these as irregular movements
        if (PlayerUtil.isInLiquid() || mc.player.capabilities.isFlying || mc.player.isElytraFlying() || mc.player.isOnLadder()) {
            return;
        }

        // web fast fall, patched on most servers
        if (((IEntity) mc.player).getInWeb() && !webs.getValue()) {
            return;
        }

        // don't attempt to fast fall while jumping or sneaking
        if (mc.gameSettings.keyBindJump.isKeyDown() || mc.player.isSneaking() || SpeedModule.INSTANCE.isEnabled()) {
            return;
        }

        // if we are not falling, don't attempt to speed
        if (!colliding || mc.player.onGround || event.getY() > 0) {
            return;
        }

        switch (mode.getValue()) {
            case PACKET:

                if (strictTimer.passedTime(200 * shiftTicks.getValue().longValue(), Format.MILLISECONDS)) {

                    // cancel vanilla movements
                    event.setCanceled(true);

                    // shift ticks
                    for (int i = 0; i < shiftTicks.getValue(); i++) {

                        // move player
                        moving = true;
                        mc.player.move(event.getType(), 0, event.getY(), 0);
                        moving = false;

                        // see {@link EntityPlayerSP.class}

                        if (mc.player.isSprinting() != ((IEntityPlayerSP) mc.player).getServerSprintState()) {
                            if (mc.player.isSprinting()) {
                                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
                            }

                            else {
                                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                            }

                            ((IEntityPlayerSP) mc.player).setServerSprintState(mc.player.isSprinting());
                        }

                        if (mc.player.isSneaking() != ((IEntityPlayerSP) mc.player).getServerSneakState()) {
                            if (mc.player.isSneaking()) {
                                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                            }

                            else {
                                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                            }

                            ((IEntityPlayerSP) mc.player).setServerSneakState(mc.player.isSneaking());
                        }

                        double updatedPosX = mc.player.posX - ((IEntityPlayerSP) mc.player).getLastReportedPosX();
                        double updatedPosY = mc.player.getEntityBoundingBox().minY - ((IEntityPlayerSP) mc.player).getLastReportedPosY();
                        double updatedPosZ = mc.player.posZ - ((IEntityPlayerSP) mc.player).getLastReportedPosZ();

                        double updatedRotationYaw = mc.player.rotationYaw - ((IEntityPlayerSP) mc.player).getLastReportedYaw();
                        double updatedRotationPitch = mc.player.rotationPitch - ((IEntityPlayerSP) mc.player).getLastReportedPitch();

                        int positionUpdateTicks = ((IEntityPlayerSP) mc.player).getPositionUpdateTicks();
                        ((IEntityPlayerSP) mc.player).setPositionUpdateTicks(positionUpdateTicks + 1);

                        boolean positionUpdate = updatedPosX * updatedPosX + updatedPosY * updatedPosY + updatedPosZ * updatedPosZ > 9.0E-4 || ((IEntityPlayerSP) mc.player).getPositionUpdateTicks() >= 20;
                        boolean rotationUpdate = updatedRotationYaw != 0 || updatedRotationPitch != 0;

                        if (mc.player.isRiding()) {
                            mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.motionX, -999, mc.player.motionZ, mc.player.rotationYaw, mc.player.rotationPitch, mc.player.onGround));
                            positionUpdate = false;
                        }

                        else if (positionUpdate && rotationUpdate) {
                            mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.getEntityBoundingBox().minY, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, mc.player.onGround));
                        }

                        else if (positionUpdate) {
                            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.getEntityBoundingBox().minY, mc.player.posZ, mc.player.onGround));
                        }

                        else if (rotationUpdate) {
                            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw, mc.player.rotationPitch, mc.player.onGround));
                        }

                        else if (((IEntityPlayerSP) mc.player).getPreviousOnGround() != mc.player.onGround) {
                            mc.player.connection.sendPacket(new CPacketPlayer(mc.player.onGround));
                        }

                        if (positionUpdate) {
                            ((IEntityPlayerSP) mc.player).setLastReportedPosX(mc.player.posX);
                            ((IEntityPlayerSP) mc.player).setLastReportedPosY(mc.player.getEntityBoundingBox().minY);
                            ((IEntityPlayerSP) mc.player).setLastReportedPosZ(mc.player.posZ);
                            ((IEntityPlayerSP) mc.player).setPositionUpdateTicks(0);
                        }

                        if (rotationUpdate) {
                            ((IEntityPlayerSP) mc.player).setLastReportedYaw(mc.player.rotationYaw);
                            ((IEntityPlayerSP) mc.player).setLastReportedPitch(mc.player.rotationPitch);
                        }

                        ((IEntityPlayerSP) mc.player).setPreviousOnGround(mc.player.onGround);
                    }

                    strictTimer.resetTime();
                }

                break;
            case MOTION:

                // adjust player velocity
                // mc.player.connection.sendPacket(new CPacketPlayer(false));
                mc.player.motionY = -speed.getValue();
                // mc.player.setVelocity(0, -speed.getValue(), 0);
                break;
            case TIMER:

                // speed up client ticks
                getCosmos().getTickManager().setClientTicks(speed.getValue().floatValue() * 2);
                break;
        }
    }

    @Override
    public void onUpdate() {

        if (mode.getValue().equals(Mode.PACKET)) {

            // there is something blocking our movement
            if (!mc.world.isAirBlock(mc.player.getPosition())) {
                colliding = false;
                return;
            }

            // speed up movement
            if (mc.player.onGround) {

                // check strict time
                if (strictTimer.passedTime(200 * shiftTicks.getValue().longValue(), Format.MILLISECONDS)) {
                    mc.player.motionY = -0.0784;
                }

                colliding = true;
            }

            // ?? somehow we are no longer falling
            if (mc.player.motionY > 0) {
                colliding = false;
            }
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();

        // reset states
        colliding = false;
        moving = false;
        strictTimer.resetTime();
    }

    public enum Mode {

        /**
         * Adjust verticals velocity to speed up falling
         */
        MOTION,

        /**
         * Sends position packets to instantly fall server side
         */
        PACKET,

        /**
         * Speeds up client ticks while falling
         */
        TIMER
    }
}
