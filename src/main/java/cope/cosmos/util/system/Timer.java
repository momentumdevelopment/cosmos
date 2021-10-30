package cope.cosmos.util.system;

import cope.cosmos.util.Wrapper;

public class Timer implements Wrapper {

    public long time;

    public Timer() {
        time = -1;
    }

    public long getMS(long time) {
        return time / 1000000L;
    }

    public boolean passed(long time, Format format) {
        switch (format) {
            case SYSTEM:
            default:
                return getMS(System.nanoTime() - this.time) >= time;
            case TICKS:
                return mc.player.ticksExisted % (int) time == 0;
        }
    }

    public boolean reach(long time, Format format) {
        switch (format) {
            case SYSTEM:
            default:
                return getMS(System.nanoTime() - this.time) <= time;
            case TICKS:
                return mc.player.ticksExisted % (int) time != 0;
        }
    }

    public boolean sleep(long time) {
        if ((System.nanoTime() / 1000000L - time) >= time) {
            reset();
            return true;
        }

        return false;
    }

    public void reset() {
        time = System.nanoTime();
    }

    public enum Format {
        SYSTEM,
        TICKS
    }
}
