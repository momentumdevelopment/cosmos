package cope.cosmos.event.listener;

import cope.cosmos.event.annotation.Cancelable;

/**
 * Represents a base Event class, used to dispatch items across the event bus to post to event listeners
 * @author aesthetical
 * @since 12/08/2021
 */
public class Event {

    /**
     * If the event should be cancelable
     */
    protected boolean cancelable = false;

    /**
     * If the event is canceled
     */
    private boolean canceled = false;

    public Event() {
        // check if theres a Cancelable annotation on the class
        cancelable = getClass().isAnnotationPresent(Cancelable.class);
    }

    /**
     * If the event is able to mark as canceled
     * @return {boolean}
     */
    public boolean isCancelable() {
        return cancelable;
    }

    /**
     * If the event has been marked as canceled
     * @return {boolean}
     */
    public boolean isCanceled() {
        return cancelable && canceled;
    }

    /**
     * Sets the event's cancel state.
     * @param in The canceled state
     */
    public void setCanceled(boolean in) {
        if (cancelable) {
            canceled = in;
        }
    }
}
