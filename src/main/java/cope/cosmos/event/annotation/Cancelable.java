package cope.cosmos.event.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used above the Event class, to mark the event as cancelable.
 * @author aesthetical
 * @since 12/08/2021
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Cancelable {

}
