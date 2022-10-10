package cope.cosmos.client.features.modules.movement;

import cope.cosmos.asm.mixins.accessor.IEntity;
import cope.cosmos.asm.mixins.accessor.INetHandlerPlayClient;
import cope.cosmos.client.events.entity.player.UpdateWalkingPlayerEvent;
import cope.cosmos.client.events.motion.movement.MotionEvent;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.combat.SelfFillModule;
import cope.cosmos.client.features.modules.exploits.PacketFlightModule;
import cope.cosmos.client.features.modules.visual.FreecamModule;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.math.Timer;
import cope.cosmos.util.math.Timer.Format;
import cope.cosmos.util.player.PlayerUtil;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 04/18/2022
 */
public class FastFallModule extends Module {
    public static FastFallModule INSTANCE;

    public FastFallModule() {
        super("FastFall", new String[] {"ReverseStep", "HoleTP"}, Category.MOVEMENT, "Falls faster");
        INSTANCE = this;
    }

    // **************************** general ****************************

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.MOTION)
            .setDescription("Mode for falling");

    public static Setting<Double> speed = new Setting<>("Speed", 0.1, 1.0, 10.0, 2)
            .setDescription("Fall speed")
            .setVisible(() -> !mode.getValue().equals(Mode.PACKET));

    public static Setting<Double> shiftTicks = new Setting<>("ShiftTicks", 1.0, 3.0, 5.0, 0)
            .setDescription("Ticks to shift forward when falling")
            .setVisible(() -> mode.getValue().equals(Mode.PACKET));

    public static Setting<Double> height = new Setting<>("Height", 0.0, 2.0, 10.0, 1)
            .setDescription("Maximum height to be pulled down");

    public static Setting<Boolean> webs = new Setting<>("Webs", false)
            .setDescription("Falls in webs");

    // previous onGround state
    private boolean previousOnGround;

    // fall timers
    private final Timer rubberbandTimer = new Timer();
    private final Timer strictTimer = new Timer();

    // pause ticks
    private int ticks;
    private boolean stop;

    @Override
    public void onTick() {

        // save ground state
        previousOnGround = mc.player.onGround;
    }

    @Override
    public void onUpdate() {

        // NCP will flag these as irregular movements
        if (PlayerUtil.isInLiquid() || mc.player.isOverWater() || mc.player.capabilities.isFlying || mc.player.isElytraFlying() || mc.player.isOnLadder()) {
            return;
        }

        // web fast fall, patched on most servers
        if (((IEntity) mc.player).getInWeb() && !webs.getValue()) {
            return;
        }

        // don't attempt to fast fall while jumping or sneaking
        if (mc.gameSettings.keyBindJump.isKeyDown() || mc.gameSettings.keyBindSneak.isKeyDown() || SpeedModule.INSTANCE.isEnabled() || SelfFillModule.INSTANCE.isActive() || PacketFlightModule.INSTANCE.isActive() || FreecamModule.INSTANCE.isEnabled() || JesusModule.INSTANCE.isActive()) {
            return;
        }

        // recently rubberbanded or teleported
        if (!rubberbandTimer.passedTime(1, Format.SECONDS)) {
            return;
        }

        // only fast fall if the player is on the ground
        if (mc.player.onGround) {

            // attempt to fall faster by adjusting player velocity
            if (mode.getValue().equals(Mode.MOTION)) {

                // check all blocks within the height
                for (double fallHeight = 0; fallHeight < height.getValue() + 0.5; fallHeight += 0.01) {

                    // check if the fall area is empty
                    if (!mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0, -fallHeight, 0)).isEmpty()) {

                        // adjust player velocity
                        // mc.player.connection.sendPacket(new CPacketPlayer(false));
                        mc.player.motionY = -speed.getValue();
                        break;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {

        // NCP will flag these as irregular movements
        if (PlayerUtil.isInLiquid() || mc.player.isOverWater() || mc.player.capabilities.isFlying || mc.player.isElytraFlying() || mc.player.isOnLadder()) {
            return;
        }

        // web fast fall, patched on most servers
        if (((IEntity) mc.player).getInWeb() && !webs.getValue()) {
            return;
        }

        // don't attempt to fast fall while jumping or sneaking
        if (mc.gameSettings.keyBindJump.isKeyDown() || mc.gameSettings.keyBindSneak.isKeyDown() || SpeedModule.INSTANCE.isEnabled() || SelfFillModule.INSTANCE.isEnabled() || PacketFlightModule.INSTANCE.isEnabled() || FlightModule.INSTANCE.isEnabled() || FreecamModule.INSTANCE.isEnabled() || JesusModule.INSTANCE.isActive()) {
            return;
        }

        // recently rubberbanded or teleported
        if (!rubberbandTimer.passedTime(1, Format.SECONDS)) {
            return;
        }

        // attempt to fall faster by adjusting player packets and quick adjusting position
        if (mode.getValue().equals(Mode.PACKET)) {

            // cancel event
            event.setCanceled(true);

            // falling down the side of a block
            if (mc.player.motionY < 0 && (previousOnGround && !mc.player.onGround)) {

                // check all blocks within the height
                for (double fallHeight = 0; fallHeight < height.getValue() + 0.5; fallHeight += 0.01) {

                    // check if the fall area is empty
                    if (!mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0, -fallHeight, 0)).isEmpty()) {

                        // wait for 1 second, fast falling too frequently flags NCP Updated
                        if (strictTimer.passedTime(1, Format.SECONDS)) {

                            // fall, update packets; multiple iterations of onUpdateWalkingPlayer in {@link EntityPlayerSP.class}
                            mc.player.motionX = 0;
                            mc.player.motionZ = 0;
                            event.setIterations(shiftTicks.getValue().intValue());
                            stop = true;
                            ticks = 0;
                            strictTimer.resetTime();
                            break;
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onMotion(MotionEvent event) {

        // needs pause
        if (mode.getValue().equals(Mode.PACKET) && stop) {

            // override motion
            event.setCanceled(true);
            event.setX(0);
            event.setZ(0);
            mc.player.motionX = 0;
            mc.player.motionZ = 0;

            // update pause ticks
            ticks++;

            // passed wait time
            if (ticks > shiftTicks.getValue()) {
                stop = false;
                ticks = 0;
            }
        }
    }

    /*
    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {

        // movement and rotations packet
        if (event.getPacket() instanceof CPacketPlayer) {

            // packet has movements
            if (((ICPacketPlayer) event.getPacket()).isMoving()) {

                // motion paused
                if (stop) {
                    event.setCanceled(true);
                }
            }
        }
    }
     */

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {

        if (nullCheck()) {

            // if the client is not done loading the surrounding terrain, DO NOT CANCEL MOVEMENT PACKETS!!!!
            if (!((INetHandlerPlayClient) mc.player.connection).isDoneLoadingTerrain()) {
                return;
            }

            // packet for rubberbands
            if (event.getPacket() instanceof SPacketPlayerPosLook) {
                rubberbandTimer.resetTime();
            }
        }
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
    }
}
