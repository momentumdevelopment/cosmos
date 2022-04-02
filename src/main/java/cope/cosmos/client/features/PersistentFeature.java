package cope.cosmos.client.features;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Always enabled feature
 * @author linustouchtips
 * @since 04/01/2022
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PersistentFeature {

}
