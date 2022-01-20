package cope.cosmos.client.ui.clickgui;

/**
 * @author Ethius
 * @since 01/12/2022
 */
public class Animations {

    /**
     * Gets animation time that decelerates as time goes on
     * @param duration The animation time
     * @param time The current time
     * @return The decelerate animation time
     */
    public static float getDecelerateAnimation(long duration, long time) {
        float animationTime = time / (float) duration;
        return 1 - ((animationTime - 1) * (animationTime - 1));
    }
}
