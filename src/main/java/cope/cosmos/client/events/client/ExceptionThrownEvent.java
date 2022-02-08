package cope.cosmos.client.events.client;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called when an exception is thrown by the server
 * @author linustouchtips
 * @since 12/25/2021
 */
@Cancelable
public class ExceptionThrownEvent extends Event {

    // the thrown exception
    private final Throwable exception;

    public ExceptionThrownEvent(Throwable exception) {
        this.exception = exception;
    }

    /**
     * Gets the thrown exception
     * @return the thrown exception
     */
    public Throwable getException() {
        return exception;
    }
}
