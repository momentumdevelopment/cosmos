package cope.cosmos.client.features.modules.player;

import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.system.Timer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Blink extends Module {
    public static final Setting<Mode> mode = new Setting<>("Mode", "How to meet the requirement", Mode.MANUAL);
    public static final Setting<Boolean> spawnFake = new Setting<>("SpawnFake", "If to spawn a fake player", true);

    public static final Setting<Double> delay = new Setting<>("Delay", "The delay in seconds until sending all packets", 0.1, 5.0, 50.0, 1);
    public static final Setting<Integer> packetCount = new Setting<>("Packets", "The amount of packets until sending all packets", 0, 10, 200, 1);
    public static final Setting<Integer> distance = new Setting<>("Distance", "The distance in blocks from the last position until sending all packets", 1, 10, 20, 1);

    private final Queue<CPacketPlayer> packets = new ConcurrentLinkedQueue<>();
    private EntityOtherPlayerMP fakePlayer = null;
    private final Timer timer = new Timer();
    private boolean processing = false;

    private BlockPos lastPos = null;

    public Blink() {
        super("Blink", Category.PLAYER, "Suspends movement packets until a requirement is met", () -> Setting.formatEnum(mode.getValue()));
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (!nullCheck()) {
            this.toggle();
            return;
        }

        this.setup(true);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (nullCheck()) {
            this.process(false);

            if (this.fakePlayer != null) {
                mc.world.removeEntity(this.fakePlayer);
                mc.world.removeEntityDangerously(this.fakePlayer);
            }

            this.fakePlayer = null;
            this.lastPos = null;
            this.processing = false;
        }
    }

    @Override
    public void onUpdate() {
        switch (mode.getValue()) {
            case DELAY: {
                if (this.timer.passed(delay.getValue().longValue() * 1000L, Timer.Format.SYSTEM)) {
                    this.timer.reset();
                    this.process(true);
                }
                break;
            }

            case PACKETS: {
                if (this.packets.size() >= packetCount.getValue()) {
                    this.process(true);
                }
                break;
            }

            case DISTANCE: {
                if (this.lastPos == null) {
                    this.lastPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
                    return;
                }

                if (mc.player.getDistance(this.lastPos.getX(), this.lastPos.getY(), this.lastPos.getZ()) >= distance.getValue()) {
                    this.process(true);
                }
                break;
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketPlayer && !this.processing) {
            event.setCanceled(true);
            this.packets.add((CPacketPlayer) event.getPacket());
        }
    }

    private void process(boolean setup) {
        if (this.processing) {
            return;
        }

        this.processing = true;
        while (!this.packets.isEmpty()) {
            CPacketPlayer packet = this.packets.poll();
            if (packet == null) {
                break;
            }

            mc.player.connection.sendPacket(packet);
        }

        if (setup) {
            this.setup(true);
        }

        this.processing = false;
    }

    private void setup(boolean spawn) {
        if (spawnFake.getValue() && spawn) {
            if (this.fakePlayer != null) {
                mc.world.removeEntity(this.fakePlayer);
                mc.world.removeEntityDangerously(this.fakePlayer);
            }

            this.fakePlayer = new EntityOtherPlayerMP(mc.world, mc.player.getGameProfile());
            this.fakePlayer.copyLocationAndAnglesFrom(mc.player);
            this.fakePlayer.inventory.copyInventory(mc.player.inventory);

            mc.world.spawnEntity(this.fakePlayer);
        }

        this.lastPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
    }

    public enum Mode {
        MANUAL, DELAY, PACKETS, DISTANCE
    }
}
