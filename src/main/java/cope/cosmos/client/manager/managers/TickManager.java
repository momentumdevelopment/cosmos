package cope.cosmos.client.manager.managers;

import cope.cosmos.asm.mixins.accessor.IMinecraft;
import cope.cosmos.asm.mixins.accessor.ITimer;
import cope.cosmos.client.events.PacketEvent;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.util.Wrapper;
import cope.cosmos.util.player.Rotation;
import cope.cosmos.util.player.RotationUtil;
import cope.cosmos.util.system.MathUtil;
import net.minecraft.entity.MoverType;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@SuppressWarnings("unused")
public class TickManager extends Manager implements Wrapper {

    public long prevTime;
    public float[] TPS = new float[20];
    public int currentTick;

    public TickManager() {
        super("TickManager", "Keeps track of the server ticks");
        prevTime = -1;

        for (int i = 0, len = TPS.length; i < len; i++) {
            TPS[i] = 0;
        }

        MinecraftForge.EVENT_BUS.register(this);
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

    @SubscribeEvent
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

    public void shiftServerTicks(MoverType type, Vec3d motion, Rotation rotation, int tickShift) {
        for (int ticks = 0; ticks < tickShift; ticks++) {
            mc.player.move(type, motion.x, motion.y, motion.z);
            RotationUtil.sendRotationPackets(rotation.getYaw(), rotation.getPitch());
        }
    }

    public enum TPS {
        CURRENT, AVERAGE, NONE
    }
}
