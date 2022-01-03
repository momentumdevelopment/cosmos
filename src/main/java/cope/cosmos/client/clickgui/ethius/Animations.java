package cope.cosmos.client.clickgui.ethius;

public class Animations {

    public static float getDecelerateAnimation(long duration, long time) {
        float x1 = (float) time / (float) duration;
        return 1 - ((x1 - 1) * (x1 - 1));
    }

}
