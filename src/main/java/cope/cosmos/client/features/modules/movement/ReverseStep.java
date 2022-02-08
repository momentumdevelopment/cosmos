package cope.cosmos.client.features.modules.movement;

import cope.cosmos.asm.mixins.accessor.IEntity;
import cope.cosmos.client.events.motion.movement.MotionEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.PlayerUtil;
import cope.cosmos.util.math.Timer;
import cope.cosmos.util.math.Timer.Format;
import net.minecraft.block.BlockSlab;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 06/08/2021
 */
public class ReverseStep extends Module {
    public static ReverseStep INSTANCE;

    public ReverseStep() {
        super("ReverseStep", Category.MOVEMENT, "Allows you to fall faster");
        INSTANCE = this;
    }

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.MOTION).setDescription("Mode for falling");
    public static Setting<Double> speed = new Setting<>("Speed", 0.0, 0.4, 10.0, 2).setDescription("Fall speed").setVisible(() -> !mode.getValue().equals(Mode.PACKET));
    public static Setting<Double> height = new Setting<>("Height", 0.0, 2.0, 5.0, 1).setDescription("Maximum height to fall");
    public static Setting<Boolean> hole = new Setting<>("OnlyHole", false).setDescription("Only falls into holes");
    public static Setting<Boolean> webs = new Setting<>("Webs", false).setDescription("Falls in webs");

    // fall timer
    private int fallTicks;
    private final Timer strictTimer = new Timer();

    @SubscribeEvent
    public void onMotion(MotionEvent event) {
        // reset client ticks
        getCosmos().getTickManager().setClientTicks(1);

        // NCP will flag these as irregular movements
        if (PlayerUtil.isInLiquid() || mc.player.capabilities.isFlying || mc.player.isElytraFlying() || mc.player.isOnLadder()) {
            return;
        }

        // web fast fall, patched on most servers
        if (((IEntity) mc.player).getInWeb() && !webs.getValue()) {
            return;
        }

        // don't attempt to fast fall while jumping or sneaking
        if (mc.gameSettings.keyBindJump.isKeyDown() || mc.gameSettings.keyBindSneak.isKeyDown() || Speed.INSTANCE.isEnabled()) {
            return;
        }

        // fall start
        if (mc.player.onGround && mc.world.isAirBlock(new BlockPos(mc.player.getPositionVector()).down())) {
            fallTicks = 0;
        }

        // we are falling
        else {
            fallTicks++;
        }

        // we are stepping down a block
        if (mc.player.fallDistance > 0 && (fallTicks > 0 && fallTicks < 10)) {

            // nearest block below
            double fallingBlock = 0;
            for (double y = mc.player.posY; y > 0; y -= 0.001) {
                // cannot fall onto slabs
                if (mc.world.getBlockState(new BlockPos(mc.player.posX, y, mc.player.posZ)).getBlock() instanceof BlockSlab) {
                    continue;
                }

                // check block state, if it's not null then it means something is there
                if (mc.world.getBlockState(new BlockPos(mc.player.posX, y, mc.player.posZ)).getBlock().getDefaultState().getCollisionBoundingBox(mc.world, new BlockPos(0, 0, 0)) == null) {
                    continue;
                }

                fallingBlock = y;
                break;
            }

            // fall height
            double fallHeight = mc.player.posY - fallingBlock;

            // if we didn't find a block then we have a default value
            if (fallingBlock - mc.player.posY >= 0) {
                fallingBlock = mc.player.posY;
            }

            // check fall height
            if (fallHeight > height.getValue()) {
                return;
            }

            // check hole fall
            if (!getCosmos().getHoleManager().isHole(new BlockPos(mc.player.posX, fallingBlock + 0.1, mc.player.posZ)) && hole.getValue()) {
                return;
            }

            switch (mode.getValue()) {
                case MOTION:
                    // adjust player velocity
                    mc.player.connection.sendPacket(new CPacketPlayer(false));
                    mc.player.motionY = -speed.getValue();
                    break;
                case PACKET:
                    // only attempt fast fall other second, prevents flag for SURVIVAL_FLY
                    if (strictTimer.passedTime(1, Format.SECONDS)) {

                        // send packets to simulate falling
                        if (mc.getConnection() != null) {
                            if (fallHeight > 0.5) {
                                mc.getConnection().getNetworkManager().sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 0.07840000152, mc.player.posZ, false));
                                mc.getConnection().getNetworkManager().sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 0.23363200604, mc.player.posZ, false));
                                mc.getConnection().getNetworkManager().sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 0.46415937495, mc.player.posZ, false));
                                mc.getConnection().getNetworkManager().sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 0.76847620241, mc.player.posZ, false));

                                if (fallHeight >= 1.5) {
                                    mc.getConnection().getNetworkManager().sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 1.14510670065, mc.player.posZ, false));
                                    mc.getConnection().getNetworkManager().sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 1.59260459764, mc.player.posZ, false));

                                    if (fallHeight >= 2.5) {
                                        mc.getConnection().getNetworkManager().sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 2.10955254674, mc.player.posZ, false));
                                        mc.getConnection().getNetworkManager().sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 2.69456154825, mc.player.posZ, false));

                                        if (fallHeight >= 3.5) {
                                            mc.getConnection().getNetworkManager().sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 3.34627038241, mc.player.posZ, false));
                                        }
                                    }
                                }
                            }
                        }

                        // speed up
                        // mc.player.motionY -= 0.08;
                        mc.player.setPosition(mc.player.posX, fallingBlock + 0.1, mc.player.posZ);
                        mc.player.setVelocity(0, 0, 0);
                        strictTimer.resetTime();
                    }

                    break;
                case TIMER:
                    // speed up client ticks
                    getCosmos().getTickManager().setClientTicks(speed.getValue().floatValue() * 2);
                    break;
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

        /**
         * Speeds up client ticks while falling
         */
        TIMER
    }
}
