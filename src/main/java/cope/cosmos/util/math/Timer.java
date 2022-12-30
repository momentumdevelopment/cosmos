package cope.cosmos.util.math;

import cope.cosmos.client.Cosmos;
import cope.cosmos.util.Wrapper;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 05/06/2021
 */
public class Timer implements Wrapper {

    // time
    private long milliseconds;
    private long ticks;

    public Timer() {

        // initialize the values
        milliseconds = -1;
        ticks = -1;

        // register to event bus
        Cosmos.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onUpdate(LivingEvent.LivingUpdateEvent event) {
        // update ticks
        if (nullCheck()) {
            ticks++;
        }

        else {
            // reset time
            milliseconds = -1;
            ticks = -1;
        }
    }

    /**
     * Checks if the timer has passed a specified time
     * @param time The specified time
     * @param format The timing format to use
     * @return Whether the timer has passed the specified time
     */
    public boolean passedTime(long time, Format format) {
        switch (format) {
            case MILLISECONDS:
            default:
                return (System.currentTimeMillis() - milliseconds) >= time;
            case SECONDS:
                return (System.currentTimeMillis() - milliseconds) >= (time * 1000);
            case TICKS:
                return ticks >= time;
        }
    }

    /**
     * Gets the elapsed time in milliseconds
     * @return The elapsed time in milliseconds
     */
    public long getMilliseconds() {
        if (milliseconds <= 0) {
            return 0;
        }

        return System.currentTimeMillis() - milliseconds;
    }

    /**
     * Sets the timer time
     * @param in The new time
     * @param format The timing format to use
     */
    public void setTime(long in, Format format) {
        switch (format) {
            case MILLISECONDS:
            default:
                milliseconds = System.currentTimeMillis() - in;
                break;
            case SECONDS:
                milliseconds = System.currentTimeMillis() - (in * 1000);
                break;
            case TICKS:
                ticks = in;
                break;
        }
    }

    /**
     * Resets the timer
     */
    public void resetTime() {
        // reset our values
        milliseconds = System.currentTimeMillis();
        ticks = 0;
    }

    public enum Format {

        /**
         * Time in milliseconds
         */
        MILLISECONDS,

        /**
         * Time in seconds
         */
        SECONDS,

        /**
         * Time in ticks
         */
        TICKS
    }
}
