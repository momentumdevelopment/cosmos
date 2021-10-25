package cope.cosmos.client.features.modules.movement;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.player.PlayerUtil;
import cope.cosmos.util.world.HoleUtil;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;

@SuppressWarnings("unused")
public class ReverseStep extends Module {
    public static ReverseStep INSTANCE;

    public ReverseStep() {
        super("ReverseStep", Category.MOVEMENT, "Allows you to fall faster");
        INSTANCE = this;
    }

    public static Setting<Mode> mode = new Setting<>("Mode", "Mode for pulling down", Mode.MOTION);
    public static Setting<Double> height = new Setting<>("Height", "Required height to be pulled down", 0.0, 2.0, 5.0, 1);
    public static Setting<Double> speed = new Setting<>("Speed", "Pull down speed", 0.0, 1.0, 10.0, 2);
    public static Setting<Boolean> shift = new Setting<>("Shift", "Sneaks the player before pulling down", false);
    public static Setting<Boolean> strict = new Setting<>("Strict", "Uses a slower speed to bypass strict servers", false);
    public static Setting<Boolean> hole = new Setting<>("OnlyHole", "Only pulls you down into holes", false);

    @Override
    public void onUpdate() {
        if (PlayerUtil.isInLiquid() || mc.gameSettings.keyBindJump.isKeyDown() || mc.player.isOnLadder())
            return;

        if (hole.getValue() && !HoleUtil.isAboveHole(height.getValue()))
            return;

        switch (mode.getValue()) {
            case MOTION:
                if (mc.player.onGround) {
                    if (shift.getValue())
                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));

                    for (double y = 0; y < height.getValue() + 0.5; y += 0.01) {
                        if (!mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0, -y, 0)).isEmpty()) {
                            mc.player.motionY = strict.getValue() ? -0.22 : -speed.getValue();
                            break;
                        }
                    }

                    if (shift.getValue())
                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                }

            break;
            case SHIFT:
                if (mc.player.onGround) {
                    for (double y = 0; y < height.getValue() + 0.5; y += 0.01) {
                        if (!mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0, -y, 0)).isEmpty()) {
                            mc.player.connection.sendPacket(new CPacketPlayer(mc.player.onGround));
                            mc.player.motionY *= 1.75;
                            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                            break;
                        }
                    }
                }

                break;
            case TIMER:
                Cosmos.INSTANCE.getTickManager().setClientTicks(1);
                if (mc.player.onGround) {
                    for (double y = 0; y < height.getValue() + 0.5; y += 0.01) {
                        if (!mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0, -y, 0)).isEmpty()) {
                            Cosmos.INSTANCE.getTickManager().setClientTicks(speed.getValue().floatValue() * 2.0f);
                            break;
                        }
                    }
                }

                break;
        }
    }

    public enum Mode {
        MOTION, SHIFT, TIMER
    }
}
