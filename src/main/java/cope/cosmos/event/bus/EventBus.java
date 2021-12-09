package cope.cosmos.event.bus;

import cope.cosmos.event.annotation.Subscription;
import cope.cosmos.event.listener.Event;
import cope.cosmos.event.listener.Listener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Handles all events and their subscribers
 * @author aesthetical
 * @since 12/08/2021
 */
public class EventBus {

    // contains the object and the object's listeners
    private final Map<Object, ArrayList<Listener>> listeners = new ConcurrentHashMap<>();

    // our runnable for invoking our event methods
    private final Consumer<Runnable> consumer = Runnable::run;

    /**
     * Subscribes an object to the EventBus
     * @param object The object to subscribe.
     */
    @SuppressWarnings("unchecked")
    public void subscribe(Object object) {
        // if the object is null, we obviously cannot pull methods from it
        // if its already in our map, we already registered the subscriber
        if (object != null && !listeners.containsKey(object)) {

            ArrayList<Listener> members = new ArrayList<>();

            for (Method method : object.getClass().getDeclaredMethods()) {
                method.setAccessible(true);

                // we check if the method is annotated with the Subscription annotation, and we only want the method parameter count to be one, our event class
                // we also check if that single parameter can be assigned to an Event
                if (method.isAnnotationPresent(Subscription.class) && method.getParameterCount() == 1 && Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
                    Subscription sub = method.getDeclaredAnnotation(Subscription.class);
                    members.add(new Listener(object, (Class<? extends Event>) method.getParameterTypes()[0], method, sub.receiveCancelled(), sub.priority()));
                }
            }

            members.sort(Comparator.comparingInt((l) -> l.priority)); // sort by priority
            listeners.put(object, members); // put our object and its listeners in the map
        }
    }

    /**
     * Removes a subscriber
     * @param object The object to unsubscribe
     */
    public void unsubscribe(Object object) {
        // this check is needed, as if we try to remove null it will throw an exception.
        if (object != null) {
            listeners.remove(object);
        }
    }

    /**
     * Dispatches an event to all subscribers
     * @param event The event to dispatch
     */
    public void dispatch(Event event) {
        // we're going to loop though only the values of the map and then map out only the listeners of each subscriber, returning a map of all of the listener objects
        // we then sort them by their priorities, and then we can loop though them.
        listeners.values().stream()
                .flatMap(Collection::stream)
                .sorted(Comparator.comparingInt((l) -> l.priority))
                .forEach((listener) -> {
                    if (listener.clazz.isInstance(event)) {
                        // if the event is canceled and we did not say we should receive canceled events in the @Subscription annotation, return
                        if (event.isCanceled() && !listener.receiveCancelled) {
                            return;
                        }

                        consumer.accept(() -> {
                            try {
                                listener.method.invoke(listener.object, event); // invoke the method used for our event
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                });
    }
}
