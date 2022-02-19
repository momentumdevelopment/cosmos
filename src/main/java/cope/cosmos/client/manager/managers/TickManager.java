package cope.cosmos.client.manager.managers;

import cope.cosmos.asm.mixins.accessor.IMinecraft;
import cope.cosmos.asm.mixins.accessor.ITimer;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.util.math.MathUtil;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author Rigamortis, linustouchtips
 * @since 06/08/2021
 */
public class TickManager extends Manager {

    // array of last 20 TPS calculations
    private final float[] TPS = new float[20];

    // time
    private long prevTime;
    private int currentTick;

    public TickManager() {
        super("TickManager", "Keeps track of the server ticks");
        prevTime = -1;

        // initialize an empty array
        for (int i = 0, len = TPS.length; i < len; i++) {
            TPS[i] = 0;
        }

        Cosmos.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {
        if (event.getPacket() instanceof SPacketTimeUpdate) {
            if (prevTime != -1) {
                // update our TPS
                TPS[currentTick % TPS.length] = MathHelper.clamp((20 / (System.currentTimeMillis() - prevTime / 1000F)), 0, 20);
                currentTick++;
            }

            // mark as last response
            prevTime = System.currentTimeMillis();
        }
    }

    /**
     * Gets the current TPS
     * @param tps the TPS mode to use
     * @return The server TPS
     */
    public float getTPS(TPS tps) {
        // do not calculate TPS if we are not on a server
        if (mc.isSingleplayer()) {
            return 20;
        }

        else {
            switch (tps) {
                case CURRENT:
                    // use the last TPS calculation
                    return MathUtil.roundFloat(MathHelper.clamp(TPS[0], 0, 20), 2);
                case AVERAGE:
                default:
                    int tickCount = 0;
                    float tickRate = 0;

                    // calculate the average TPS
                    for (float tick : TPS) {
                        if (tick > 0) {
                            tickRate += tick;
                            tickCount++;
                        }
                    }

                    return MathUtil.roundFloat(MathHelper.clamp((tickRate / tickCount), 0, 20), 2);
            }
        }
    }

    /**
     * Sets the client tick length
     * @param ticks The new tick length
     */
    public void setClientTicks(float ticks) {
        ((ITimer) ((IMinecraft) mc).getTimer()).setTickLength((50 / ticks));
    }

    /**
     * Gets the client tick length
     * @return The tick length
     */
    public float getTickLength() {
        return ((ITimer) ((IMinecraft) mc).getTimer()).getTickLength();
    }

    public enum TPS {

        /**
         * Uses the latest TPS calculation
         */
        CURRENT,

        /**
         * Uses the average TPS (over last 20 ticks) calculation
         */
        AVERAGE,

        /**
         * Does not calculate TPS
         */
        NONE
    }
}
