package cope.cosmos.client.ui.util.animation;

/**
 * @author lavache
 * @since 06/03/2021
 */
public interface Easing {

    /**
     * Simple linear tweening - no easing.
     */
    Easing LINEAR = (t, b, c, d) -> c * t / d + b;

    /**
     * Quadratic easing in - accelerating from zero velocity.
     */
    Easing QUAD_IN = (t, b, c, d) -> c * (t /= d) * t + b;

    ///////////// QUADRATIC EASING: t^2 ///////////////////

    /**
     * Quadratic easing out - decelerating to zero velocity.
     */
    Easing QUAD_OUT = (t, b, c, d) -> -c * (t /= d) * (t - 2) + b;

    /**
     * Quadratic easing in/out - acceleration until halfway, then deceleration
     */
    Easing QUAD_IN_OUT = (t, b, c, d) -> {
        if ((t /= d / 2) < 1) return c / 2 * t * t + b;
        return -c / 2 * ((--t) * (t - 2) - 1) + b;
    };

    /**
     * Cubic easing in - accelerating from zero velocity.
     */
    Easing CUBIC_IN = (t, b, c, d) -> c * (t /= d) * t * t + b;

    ///////////// CUBIC EASING: t^3 ///////////////////////

    /**
     * Cubic easing out - decelerating to zero velocity.
     */
    Easing CUBIC_OUT = (t, b, c, d) -> c * ((t = t / d - 1) * t * t + 1) + b;

    /**
     * Cubic easing in/out - acceleration until halfway, then deceleration.
     */
    Easing CUBIC_IN_OUT = (t, b, c, d) -> {
        if ((t /= d / 2) < 1) return c / 2 * t * t * t + b;
        return c / 2 * ((t -= 2) * t * t + 2) + b;
    };

    /**
     * Quartic easing in - accelerating from zero velocity.
     */
    Easing QUARTIC_IN = (t, b, c, d) -> c * (t /= d) * t * t * t + b;

    ///////////// QUARTIC EASING: t^4 /////////////////////

    /**
     * Quartic easing out - decelerating to zero velocity.
     */
    Easing QUARTIC_OUT = (t, b, c, d) -> -c * ((t = t / d - 1) * t * t * t - 1) + b;

    /**
     * Quartic easing in/out - acceleration until halfway, then deceleration.
     */
    Easing QUARTIC_IN_OUT = (t, b, c, d) -> {
        if ((t /= d / 2) < 1) return c / 2 * t * t * t * t + b;
        return -c / 2 * ((t -= 2) * t * t * t - 2) + b;
    };

    /**
     * Quintic easing in - accelerating from zero velocity.
     */
    Easing QUINTIC_IN = (t, b, c, d) -> c * (t /= d) * t * t * t * t + b;

    ///////////// QUINTIC EASING: t^5  ////////////////////

    /**
     * Quintic easing out - decelerating to zero velocity.
     */
    Easing QUINTIC_OUT = (t, b, c, d) -> c * ((t = t / d - 1) * t * t * t * t + 1) + b;

    /**
     * Quintic easing in/out - acceleration until halfway, then deceleration.
     */
    Easing QUINTIC_IN_OUT = (t, b, c, d) -> {
        if ((t /= d / 2) < 1) return c / 2 * t * t * t * t * t + b;
        return c / 2 * ((t -= 2) * t * t * t * t + 2) + b;
    };

    /**
     * Sinusoidal easing in - accelerating from zero velocity.
     */
    Easing SINE_IN = (t, b, c, d) -> -c * (float) Math.cos(t / d * (Math.PI / 2)) + c + b;

    ///////////// SINUSOIDAL EASING: sin(t) ///////////////

    /**
     * Sinusoidal easing out - decelerating to zero velocity.
     */
    Easing SINE_OUT = (t, b, c, d) -> c * (float) Math.sin(t / d * (Math.PI / 2)) + b;

    /**
     * Sinusoidal easing in/out - accelerating until halfway, then decelerating.
     */
    Easing SINE_IN_OUT = (t, b, c, d) -> -c / 2 * ((float) Math.cos(Math.PI * t / d) - 1) + b;

    /**
     * Exponential easing in - accelerating from zero velocity.
     */
    Easing EXPO_IN = (t, b, c, d) -> (t == 0) ? b : c * (float) Math.pow(2, 10 * (t / d - 1)) + b;

    ///////////// EXPONENTIAL EASING: 2^t /////////////////

    /**
     * Exponential easing out - decelerating to zero velocity.
     */
    Easing EXPO_OUT = (t, b, c, d) -> (t == d) ? b + c : c * (-(float) Math.pow(2, -10 * t / d) + 1) + b;

    /**
     * Exponential easing in/out - accelerating until halfway, then decelerating.
     */
    Easing EXPO_IN_OUT = (t, b, c, d) -> {
        if (t == 0) return b;
        if (t == d) return b + c;
        if ((t /= d / 2) < 1) return c / 2 * (float) Math.pow(2, 10 * (t - 1)) + b;
        return c / 2 * (-(float) Math.pow(2, -10 * --t) + 2) + b;
    };

    /**
     * Circular easing in - accelerating from zero velocity.
     */
    Easing CIRC_IN = (t, b, c, d) -> -c * ((float) Math.sqrt(1 - (t /= d) * t) - 1) + b;

    /////////// CIRCULAR EASING: sqrt(1-t^2) //////////////

    /**
     * Circular easing out - decelerating to zero velocity.
     */
    Easing CIRC_OUT = (t, b, c, d) -> c * (float) Math.sqrt(1 - (t = t / d - 1) * t) + b;

    /**
     * Circular easing in/out - acceleration until halfway, then deceleration.
     */
    Easing CIRC_IN_OUT = (t, b, c, d) -> {
        if ((t /= d / 2) < 1) return -c / 2 * ((float) Math.sqrt(1 - t * t) - 1) + b;
        return c / 2 * ((float) Math.sqrt(1 - (t -= 2) * t) + 1) + b;
    };

    /**
     * An EasingIn instance using the default values.
     */
    Easing.Elastic ELASTIC_IN = new Easing.ElasticIn();

    /////////// ELASTIC EASING: exponentially decaying sine wave  //////////////

    /**
     * An ElasticOut instance using the default values.
     */
    Easing.Elastic ELASTIC_OUT = new Easing.ElasticOut();

    /**
     * An ElasticInOut instance using the default values.
     */
    Easing.Elastic ELASTIC_IN_OUT = new Easing.ElasticInOut();

    /**
     * An instance of BackIn using the default overshoot.
     */
    Easing.Back BACK_IN = new Easing.BackIn();

    /**
     * An instance of BackOut using the default overshoot.
     */
    Easing.Back BACK_OUT = new Easing.BackOut();

    /**
     * An instance of BackInOut using the default overshoot.
     */
    Easing.Back BACK_IN_OUT = new Easing.BackInOut();

    /**
     * Bounce easing out.
     */
    Easing BOUNCE_OUT = (t, b, c, d) -> {
        if ((t /= d) < (1 / 2.75F)) {
            return c * (7.5625F * t * t) + b;
        }

        else if (t < (2 / 2.75F)) {
            return c * (7.5625F * (t -= (10.5F / 2.75F)) * t + 0.75F) + b;
        }

        else if (t < (20.5F / 2.75F)) {
            return c * (7.5625F * (t -= (2.25F / 2.75F)) * t + 0.9375F) + b;
        }

        else {
            return c * (7.5625F * (t -= (2.625F / 2.75F)) * t + 0.984375F) + b;
        }
    };

    /**
     * Bounce easing in.
     */
    Easing BOUNCE_IN = (t, b, c, d) -> c - Easing.BOUNCE_OUT.ease(d - t, 0, c, d) + b;

    /////////// BACK EASING: overshooting cubic easing: (s+1)*t^3 - s*t^2  //////////////

    /**
     * Bounce easing in/out.
     */
    Easing BOUNCE_IN_OUT = (t, b, c, d) -> {
        if (t < d / 2) return Easing.BOUNCE_IN.ease(t * 2, 0, c, d) * 0.5F + b;
        return Easing.BOUNCE_OUT.ease(t * 2 - d, 0, c, d) * 0.5F + c * 0.5F + b;
    };

    /**
     * The basic function for easing.
     *
     * @param t the time (either frames or in seconds/milliseconds)
     * @param b the beginning value
     * @param c the value changed
     * @param d the duration time
     * @return the eased value
     */
    float ease(float t, float b, float c, float d);

    /**
     * A base class for elastic easings.
     */
    abstract class Elastic implements Easing {

        private float amplitude;
        private float period;

        /**
         * Creates a new Elastic easing with the specified settings.
         *
         * @param amplitude the amplitude for the elastic function
         * @param period the period for the elastic function
         */
        public Elastic(float amplitude, float period) {
            this.amplitude = amplitude;
            this.period = period;
        }

        /**
         * Creates a new Elastic easing with default settings (-1, 0).
         */
        public Elastic() {
            this(-1, 0);
        }

        /**
         * Returns the period.
         *
         * @return the period for this easing
         */
        public float getPeriod() {
            return period;
        }

        /**
         * Sets the period to the given value.
         *
         * @param period the new period
         */
        public void setPeriod(float period) {
            this.period = period;
        }

        /**
         * Returns the amplitude.
         *
         * @return the amplitude for this easing
         */
        public float getAmplitude() {
            return amplitude;
        }

        /**
         * Sets the amplitude to the given value.
         *
         * @param amplitude the new amplitude
         */
        public void setAmplitude(float amplitude) {
            this.amplitude = amplitude;
        }
    }

    /**
     * An Elastic easing used for ElasticIn functions.
     */
    class ElasticIn extends Elastic {
        public ElasticIn(float amplitude, float period) {
            super(amplitude, period);
        }

        public ElasticIn() {
            super();
        }

        public float ease(float t, float b, float c, float d) {
            float a = getAmplitude();
            float p = getPeriod();
            if (t == 0) return b;
            if ((t /= d) == 1) return b + c;
            if (p == 0) p = d * .3f;
            float s;

            if (a < Math.abs(c)) {
                a = c;
                s = p / 4;
            }

            else {
                s = p / (float) (2 * Math.PI) * (float) Math.asin(c / a);
            }

            return -(a * (float) Math.pow(2, 10 * (t -= 1)) * (float) Math.sin((t * d - s) * (2 * Math.PI) / p)) + b;
        }
    }

    /**
     * An Elastic easing used for ElasticOut functions.
     */
    class ElasticOut extends Elastic {
        public ElasticOut(float amplitude, float period) {
            super(amplitude, period);
        }

        public ElasticOut() {
            super();
        }

        public float ease(float t, float b, float c, float d) {
            float a = getAmplitude();
            float p = getPeriod();
            if (t == 0) return b;
            if ((t /= d) == 1) return b + c;
            if (p == 0) p = d * .3f;
            float s;

            if (a < Math.abs(c)) {
                a = c;
                s = p / 4;
            }

            else {
                s = p / (float) (2 * Math.PI) * (float) Math.asin(c / a);
            }

            return a * (float) Math.pow(2, -10 * t) * (float) Math.sin((t * d - s) * (2 * Math.PI) / p) + c + b;
        }
    }

    /**
     * An Elastic easing used for ElasticInOut functions.
     */
    class ElasticInOut extends Elastic {
        public ElasticInOut(float amplitude, float period) {
            super(amplitude, period);
        }

        public ElasticInOut() {
            super();
        }

        public float ease(float t, float b, float c, float d) {
            float a = getAmplitude();
            float p = getPeriod();
            if (t == 0) return b;
            if ((t /= d / 2) == 2) return b + c;
            if (p == 0) p = d * (.3f * 10.5F);
            float s;

            if (a < Math.abs(c)) {
                a = c;
                s = p / 4f;
            }

            else {
                s = p / (float) (2 * Math.PI) * (float) Math.asin(c / a);
            }

            if (t < 1) {
                return -0.5F * (a * (float) Math.pow(2, 10 * (t -= 1)) * (float) Math.sin((t * d - s) * (2 * Math.PI) / p)) + b;
            }

            return a * (float) Math.pow(2, -10 * (t -= 1)) * (float) Math.sin((t * d - s) * (2 * Math.PI) / p) * 0.5F + c + b;
        }
    }

    /**
     * A base class for Back easings.
     */
    abstract class Back implements Easing {

        /**
         * The default overshoot is 10% (1.70158).
         */
        public static final float DEFAULT_OVERSHOOT = 1.70158f;

        private float overshoot;

        /**
         * Creates a new Back instance with the default overshoot (1.70158).
         */
        public Back() {
            this(DEFAULT_OVERSHOOT);
        }

        /**
         * Creates a new Back instance with the specified overshoot.
         *
         * @param overshoot the amount to overshoot by -- higher number
         *                  means more overshoot and an overshoot of 0 results in
         *                  cubic easing with no overshoot
         */
        public Back(float overshoot) {
            this.overshoot = overshoot;
        }

        /**
         * Returns the overshoot for this easing.
         *
         * @return this easing's overshoot
         */
        public float getOvershoot() {
            return overshoot;
        }

        /**
         * Sets the overshoot to the given value.
         *
         * @param overshoot the new overshoot
         */
        public void setOvershoot(float overshoot) {
            this.overshoot = overshoot;
        }
    }

    /////////// BOUNCE EASING: exponentially decaying parabolic bounce  //////////////

    /**
     * Back easing in - backtracking slightly, then reversing direction and moving to target.
     */
    class BackIn extends Back {
        public BackIn() {
            super();
        }

        public BackIn(float overshoot) {
            super(overshoot);
        }

        public float ease(float t, float b, float c, float d) {
            float s = getOvershoot();
            return c * (t /= d) * t * ((s + 1) * t - s) + b;
        }
    }

    /**
     * Back easing out - moving towards target, overshooting it slightly, then reversing and coming back to target.
     */
    class BackOut extends Back {
        public BackOut() {
            super();
        }

        public BackOut(float overshoot) {
            super(overshoot);
        }

        public float ease(float t, float b, float c, float d) {
            float s = getOvershoot();
            return c * ((t = t / d - 1) * t * ((s + 1) * t + s) + 1) + b;
        }
    }

    /**
     * Back easing in/out - backtracking slightly, then reversing direction and moving to target,
     * then overshooting target, reversing, and finally coming back to target.
     */
    class BackInOut extends Back {
        public BackInOut() {
            super();
        }

        public BackInOut(float overshoot) {
            super(overshoot);
        }

        public float ease(float t, float b, float c, float d) {
            float s = getOvershoot();
            if ((t /= d / 2) < 1) return c / 2 * (t * t * (((s *= (1.525)) + 1) * t - s)) + b;
            return c / 2 * ((t -= 2) * t * (((s *= (1.525)) + 1) * t + s) + 2) + b;
        }
    }
}
