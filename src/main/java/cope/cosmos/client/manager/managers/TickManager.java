package cope.cosmos.client.manager.managers;

import cope.cosmos.asm.mixins.accessor.IMinecraft;
import cope.cosmos.asm.mixins.accessor.ITimer;
import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.network.PacketEvent;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.util.math.MathUtil;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author Rigamortis, linustouchtips
 * @since 06/08/2021
 */
public class TickManager extends Manager {

    // array of last 20 latestTicks calculations
    private final float[] latestTicks = new float[20];

    // time
    private long time;
    private int tick;

    public TickManager() {
        super("TickManager", "Keeps track of the server ticks");
        
        time = -1;

        // initialize an empty array
        for (int i = 0, len = latestTicks.length; i < len; i++) {
            latestTicks[i] = 0;
        }

        Cosmos.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {
        
        // packet for server time updates
        if (event.getPacket() instanceof SPacketTimeUpdate) {

            // update our latestTicks
            if (time != -1) {
                latestTicks[tick % latestTicks.length] = MathHelper.clamp((20 / ((float) (System.currentTimeMillis() - time) / 1000)), 0, 20);
                tick++;
            }

            // mark as last response
            time = System.currentTimeMillis();
        }
    }

    /**
     * Gets the current latestTicks
     * @param tps the latestTicks mode to use
     * @return The server latestTicks
     */
    public float getTPS(TPS tps) {

        // do not calculate latestTicks if we are not on a server
        if (mc.isSingleplayer() || tps.equals(TPS.NONE)) {
            return 20;
        }

        else {
            switch (tps) {
                case CURRENT:
                    // use the last latestTicks calculation
                    return MathUtil.roundFloat(MathHelper.clamp(latestTicks[0], 0, 20), 2);
                case AVERAGE:
                default:
                    int tickCount = 0;
                    float tickRate = 0;

                    // calculate the average latestTicks
                    for (float tick : latestTicks) {
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
         * Uses the latest latestTicks calculation
         */
        CURRENT,

        /**
         * Uses the average latestTicks (over last 20 ticks) calculation
         */
        AVERAGE,

        /**
         * Does not calculate latestTicks
         */
        NONE
    }
}
