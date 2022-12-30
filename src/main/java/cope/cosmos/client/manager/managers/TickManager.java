package cope.cosmos.client.manager.managers;

import cope.cosmos.asm.mixins.accessor.IMinecraft;
import cope.cosmos.asm.mixins.accessor.ITimer;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.util.math.MathUtil;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author Rigamortis, linustouchtips
 * @since 06/08/2021
 */
public class TickManager extends Manager {

    // array of last 20 latestTicks calculations
    private final float[] latestTicks = new float[10];

    // time
    private long time = -1;
    private int tick;

    public TickManager() {
        super("TickManager", "Keeps track of the server ticks");
        Cosmos.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {
        
        // packet for server time updates
        if (event.getPacket() instanceof SPacketTimeUpdate) {

            // update our ticks
            if (time != -1) {
                latestTicks[tick % latestTicks.length] = (20 / ((float) (System.currentTimeMillis() - time) / 1000));
                tick++;
            }

            // mark as last response
            time = System.currentTimeMillis();
        }
    }

    /**
     * Gets the current ticks
     * @param tps the ticks mode to use
     * @return The server ticks
     */
    public float getTPS(TPS tps) {

        // do not calculate ticks if we are not on a server
        if (mc.isSingleplayer() || tps.equals(TPS.NONE)) {
            return 20;
        }

        else {
            switch (tps) {
                case CURRENT:
                    // use the last ticks calculation
                    return MathUtil.roundFloat(latestTicks[0], 2);
                case AVERAGE:
                default:
                    int tickCount = 0;
                    float tickRate = 0;

                    // calculate the average ticks
                    for (float tick : latestTicks) {
                        if (tick > 0) {
                            tickRate += tick;
                            tickCount++;
                        }
                    }

                    return MathUtil.roundFloat((tickRate / tickCount), 2);
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
         * Uses the latest ticks calculation
         */
        CURRENT,

        /**
         * Uses the average ticks (over last 20 ticks) calculation
         */
        AVERAGE,

        /**
         * Does not calculate ticks
         */
        NONE
    }
}
