package cope.cosmos.event.listener;

/**
 * The event priority. Sorted from least -> highest value.
 * Hence Priority.HIGHEST is equal to 0.
 *
 * @author aesthetical
 * @since 12/08/2021
 */
public interface Priority {

    int DEFAULT = Integer.MAX_VALUE;
    int LOW = 30;
    int MEDIUM = 20;
    int HIGH = 10;
    int HIGHEST = 0;
}
