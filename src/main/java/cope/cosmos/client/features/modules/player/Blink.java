package cope.cosmos.client.features.modules.player;

import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.system.Timer;
import cope.cosmos.util.world.WorldUtil;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Blink extends Module {
    public Blink() {
        super("Blink", Category.PLAYER, "Suspends movement packets until a requirement is met");
    }

    public static final Setting<Mode> mode = new Setting<>("Mode", "How to meet the requirement", Mode.MANUAL);
    public static final Setting<Boolean> spawnFake = new Setting<>("SpawnFake", "If to spawn a fake player", true);

    public static final Setting<Double> delay = new Setting<>("Delay", "The delay in seconds until sending all packets", 0.1, 5.0, 50.0, 1);
    public static final Setting<Double> packetCount = new Setting<>("Packets", "The amount of packets until sending all packets", 0.0D, 10.0D, 200.0D, 0);
    public static final Setting<Double> distance = new Setting<>("Distance", "The distance in blocks from the last position until sending all packets", 1.0D, 10.0D, 20.0D, 0);

    private final Queue<CPacketPlayer> packets = new ConcurrentLinkedQueue<>();
    private final Timer timer = new Timer();
    private boolean processing = false;

    private BlockPos lastPos = null;

    @Override
    public void onEnable() {
        super.onEnable();
        setup(true);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (nullCheck()) {
            process(false);

            if (mc.world.getEntityByID(80085) != null) {
                mc.world.removeEntity(mc.world.getEntityByID(80085));
                mc.world.removeEntityDangerously(mc.world.getEntityByID(80085));
            }

            lastPos = null;
            processing = false;
        }
    }

    @Override
    public void onUpdate() {
        switch (mode.getValue()) {
            case DELAY:
                if (timer.passed(delay.getValue().longValue() * 1000L, Timer.Format.SYSTEM)) {
                    timer.reset();
                    process(true);
                }

                break;
            case PACKETS:
                if (packets.size() >= packetCount.getValue()) {
                    process(true);
                }

                break;
            case DISTANCE:
                if (lastPos == null) {
                    lastPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
                    return;
                }

                if (mc.player.getDistance(lastPos.getX(), lastPos.getY(), lastPos.getZ()) >= distance.getValue()) {
                    process(true);
                }

                break;
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketPlayer && !processing) {
            event.setCanceled(true);
            packets.add((CPacketPlayer) event.getPacket());
        }
    }

    private void process(boolean setup) {
        if (processing) {
            return;
        }

        processing = true;
        while (!packets.isEmpty()) {
            CPacketPlayer packet = packets.poll();
            if (packet == null) {
                break;
            }

            mc.player.connection.sendPacket(packet);
        }

        if (setup) {
            setup(true);
        }

        processing = false;
    }

    private void setup(boolean spawn) {
        if (spawnFake.getValue() && spawn) {
            if (mc.world.getEntityByID(80085) != null) {
                mc.world.removeEntity(mc.world.getEntityByID(80085));
                mc.world.removeEntityDangerously(mc.world.getEntityByID(80085));
            }

            WorldUtil.createFakePlayer(mc.player.getGameProfile(), 80085, true, true);
        }

        lastPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
    }

    public enum Mode {
        MANUAL, DELAY, PACKETS, DISTANCE
    }
}
