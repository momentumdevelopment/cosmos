package cope.cosmos.client.features.modules.movement;

import cope.cosmos.asm.mixins.accessor.ICPacketPlayer;
import cope.cosmos.asm.mixins.accessor.IEntity;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.math.Timer;
import cope.cosmos.util.math.Timer.Format;
import cope.cosmos.util.player.PlayerUtil;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
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

    public static Setting<Double> height = new Setting<>("Height", 0.0, 2.0, 10.0, 1)
            .setDescription("Maximum height to be pulled down");

    public static Setting<Boolean> webs = new Setting<>("Webs", false)
            .setDescription("Falls in webs");

    // fall timers
    private int fallTicks;
    private int fallStartTicks;
    private final Timer strictTimer = new Timer();

    @Override
    public void onUpdate() {

        // update ticks
        fallTicks--;

        // wait for packets to process
        if (fallTicks > 0) {
            mc.player.motionX = 0;
            mc.player.motionY = 0;
            mc.player.motionZ = 0;
            mc.player.setVelocity(0, 0, 0);
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
        if (mc.gameSettings.keyBindJump.isKeyDown() || mc.gameSettings.keyBindSneak.isKeyDown() || SpeedModule.INSTANCE.isEnabled()) {
            return;
        }

        // only fast fall if the player is on the ground
        if (mc.player.onGround && mc.world.isAirBlock(new BlockPos(mc.player.getPositionVector()).down())) {

            // fall start
            fallStartTicks = 0;

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

        // we are falling
        else {
            fallStartTicks++;
        }

        // we are stepping down a block
        if (mc.player.fallDistance > 0 && (fallStartTicks > 0 && fallStartTicks < 10)) {

            // attempt to fall faster by sending packets and instantly adjusting player position
            if (mode.getValue().equals(Mode.PACKET)) {

                // check all blocks within the height
                for (double fallHeight = 0; fallHeight < height.getValue() + 0.5; fallHeight += 0.01) {

                    // check if the fall area is empty
                    if (!mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0, -fallHeight, 0)).isEmpty()) {

                        // only attempt fast fall other second, prevents flag for SURVIVAL_FLY
                        if (strictTimer.passedTime(1, Format.SECONDS)) {

                            // send packets to simulate falling
                            if (mc.getConnection() != null) {
                                if (fallHeight > 0.5) {
                                    mc.getConnection().getNetworkManager().sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 0.07840000152, mc.player.posZ, false));
                                    mc.getConnection().getNetworkManager().sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 0.23363200604, mc.player.posZ, false));
                                    mc.getConnection().getNetworkManager().sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 0.46415937495, mc.player.posZ, false));
                                    mc.getConnection().getNetworkManager().sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 0.76847620241, mc.player.posZ, false));
                                    fallTicks = 4;

                                    if (fallHeight >= 1.5) {
                                        mc.getConnection().getNetworkManager().sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 1.14510670065, mc.player.posZ, false));
                                        mc.getConnection().getNetworkManager().sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 1.59260459764, mc.player.posZ, false));
                                        fallTicks = 6;

                                        if (fallHeight >= 2.5) {
                                            mc.getConnection().getNetworkManager().sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 2.10955254674, mc.player.posZ, false));
                                            mc.getConnection().getNetworkManager().sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 2.69456154825, mc.player.posZ, false));
                                            fallTicks = 8;

                                            if (fallHeight >= 3.5) {
                                                mc.getConnection().getNetworkManager().sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 3.34627038241, mc.player.posZ, false));
                                                fallTicks = 9;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // speed up
                        // mc.player.motionY -= 0.08;
                        mc.player.setPosition(mc.player.posX, mc.player.posY - fallHeight + 0.1, mc.player.posZ);
                        mc.player.motionX = 0;
                        mc.player.motionY = 0;
                        mc.player.motionZ = 0;
                        mc.player.setVelocity(0, 0, 0);
                        strictTimer.resetTime();
                        break;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {

        // packet for player updates
        if (event.getPacket() instanceof CPacketPlayer) {

            // if the packet is a movement packet
            if (((ICPacketPlayer) event.getPacket()).isMoving()) {

                // prevent packet from sending
                if (fallTicks > 0) {
                    event.setCanceled(true);
                }
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
