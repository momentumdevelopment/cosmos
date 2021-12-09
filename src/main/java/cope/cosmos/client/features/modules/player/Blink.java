package cope.cosmos.client.features.modules.player;

import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.event.annotation.Subscription;
import cope.cosmos.util.system.Timer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Blink extends Module {
    public static Blink INSTANCE;

    public Blink() {
        super("Blink", Category.PLAYER, "Suspends movement packets until a requirement is met");
        INSTANCE = this;
    }

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.MANUAL).setDescription("How to meet the requirement");
    public static Setting<Boolean> spawnFake = new Setting<>("SpawnFake", true).setDescription("If to spawn a fake player");

    public static Setting<Double> delay = new Setting<>("Delay", 0.1, 5.0, 50.0, 1).setDescription("The delay in seconds until sending all packets");
    public static Setting<Integer> packetCount = new Setting<>("Packets", 0, 10, 200, 1).setDescription("The amount of packets until sending all packets");
    public static Setting<Integer> distance = new Setting<>("Distance", 1, 10, 20, 1).setDescription("The distance in blocks from the last position until sending all packets");

    private final Queue<CPacketPlayer> packets = new ConcurrentLinkedQueue<>();
    private EntityOtherPlayerMP fakePlayer = null;
    private final Timer timer = new Timer();
    private boolean processing = false;

    private BlockPos lastPos = null;

    @Override
    public void onEnable() {
        super.onEnable();

        if (spawnFake.getValue()) {
            if (fakePlayer != null) {
                mc.world.removeEntity(fakePlayer);
                mc.world.removeEntityDangerously(fakePlayer);
            }

            fakePlayer = new EntityOtherPlayerMP(mc.world, mc.player.getGameProfile());
            fakePlayer.copyLocationAndAnglesFrom(mc.player);
            fakePlayer.inventory.copyInventory(mc.player.inventory);
            fakePlayer.setSneaking(mc.player.isSneaking());
            fakePlayer.setPrimaryHand(mc.player.getPrimaryHand());

            mc.world.spawnEntity(fakePlayer);
        }

        lastPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (nullCheck()) {
            process(false);

            if (fakePlayer != null) {
                mc.world.removeEntity(fakePlayer);
                mc.world.removeEntityDangerously(fakePlayer);
            }

            fakePlayer = null;
            lastPos = null;
            processing = false;
        }
    }

    @Override
    public void onUpdate() {
        switch (mode.getValue()) {
            case DELAY: {
                if (timer.passedTime(delay.getValue().longValue() * 1000, Timer.Format.SYSTEM)) {
                    timer.resetTime();
                    process(true);
                }

                break;
            }

            case PACKETS: {
                if (packets.size() >= packetCount.getValue()) {
                    process(true);
                }

                break;
            }

            case DISTANCE: {
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
    }

    @Subscription
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
            if (spawnFake.getValue()) {
                if (fakePlayer != null) {
                    mc.world.removeEntity(fakePlayer);
                    mc.world.removeEntityDangerously(fakePlayer);
                }

                fakePlayer = new EntityOtherPlayerMP(mc.world, mc.player.getGameProfile());
                fakePlayer.copyLocationAndAnglesFrom(mc.player);
                fakePlayer.inventory.copyInventory(mc.player.inventory);
                fakePlayer.setSneaking(mc.player.isSneaking());
                fakePlayer.setPrimaryHand(mc.player.getPrimaryHand());

                mc.world.spawnEntity(fakePlayer);
            }

            lastPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
        }

        processing = false;
    }

    public enum Mode {
        MANUAL, DELAY, PACKETS, DISTANCE
    }
}
