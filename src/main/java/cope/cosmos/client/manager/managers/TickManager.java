package cope.cosmos.client.manager.managers;

import cope.cosmos.asm.mixins.accessor.IMinecraft;
import cope.cosmos.asm.mixins.accessor.ITimer;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.event.annotation.Subscription;
import cope.cosmos.util.Wrapper;
import cope.cosmos.util.player.RotationUtil;
import cope.cosmos.util.system.MathUtil;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@SuppressWarnings("unused")
public class TickManager extends Manager implements Wrapper {

    private long prevTime;
    private final float[] TPS = new float[20];
    private int currentTick;

    public TickManager() {
        super("TickManager", "Keeps track of the server ticks");
        prevTime = -1;

        for (int i = 0, len = TPS.length; i < len; i++) {
            TPS[i] = 0;
        }

        Cosmos.EVENT_BUS.subscribe(this);
    }

    public float getTPS(TPS tps) {
        switch (tps) {
            case CURRENT:
                return mc.isSingleplayer() ? 20 : (float) MathUtil.roundDouble(MathHelper.clamp(TPS[0], 0, 20), 2);
            case AVERAGE:
                int tickCount = 0;
                float tickRate = 0;

                for (float tick : TPS) {
                    if (tick > 0) {
                        tickRate += tick;
                        tickCount++;
                    }
                }

                return mc.isSingleplayer() ? 20 : (float) MathUtil.roundDouble(MathHelper.clamp((tickRate / tickCount), 0, 20), 2);
        }

        return 0;
    }

    @Subscription
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {
        if (event.getPacket() instanceof SPacketTimeUpdate) {
            if (prevTime != -1) {
                TPS[currentTick % TPS.length] = MathHelper.clamp((20 / ((float) (System.currentTimeMillis() - prevTime) / 1000)), 0, 20);
                currentTick++;
            }

            prevTime = System.currentTimeMillis();
        }
    }

    public void setClientTicks(float ticks) {
        ((ITimer) ((IMinecraft) mc).getTimer()).setTickLength((50 / ticks));
    }

    // should tell the server to move forward by a number of ticks
    public void shiftServerTicks(int tickShift) {
        for (int ticks = 0; ticks < tickShift; ticks++) {
            // send vanilla movement & rotation packets
            RotationUtil.sendRotationPackets(mc.player.rotationYaw, mc.player.rotationPitch);
        }
    }

    public enum TPS {
        CURRENT, AVERAGE, NONE
    }
}
