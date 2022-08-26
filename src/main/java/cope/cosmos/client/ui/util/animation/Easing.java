package cope.cosmos.client.ui.util.animation;

import java.util.function.Function;

/**
 * Calculations from easings.net
 * @author Surge
 * @since 05/19/22
 */
public enum Easing {

    /**
     * No easing
     */
    LINEAR((input) -> input),

    /**
     * Speed gradually increases
     */
    SINE_IN((input) -> 1 - Math.cos(input * Math.PI) / 2),

    /**
     * Speed gradually decreases
     */
    SINE_OUT((input) -> Math.sin((input * Math.PI) / 2)),

    /**
     * Speed gradually increases until halfway and then decreases
     */
    SINE_IN_OUT((input) -> -(Math.cos(Math.PI * input) - 1) / 2),

    /**
     * Speed gradually increases
     */
    QUAD_IN((input) -> input * input),

    /**
     * Speed gradually decreases
     */
    QUAD_OUT((input) -> 1 - (1 - input) * (1 - input)),

    /**
     * Speed gradually increases until halfway and then decreases
     */
    QUAD_IN_OUT((input) -> input < 0.5 ? 2 * input * input : 1 - Math.pow(-2 * input + 2, 2) / 2),

    /**
     * Speed gradually increases
     */
    CUBIC_IN((input) -> input * input * input),

    /**
     * Speed gradually decreases
     */
    CUBIC_OUT((input) -> 1 - Math.pow(1 - input, 3)),

    /**
     * Speed gradually increases until halfway and then decreases
     */
    CUBIC_IN_OUT((input) -> input < 0.5 ? 4 * input * input * input : 1 - Math.pow(-2 * input + 2, 3) / 2),

    /**
     * Speed gradually increases
     */
    QUART_IN((input) -> input * input * input * input),

    /**
     * Speed gradually decreases
     */
    QUART_OUT((input) -> 1 - Math.pow(1 - input, 4)),

    /**
     * Speed gradually increases until halfway and then decreases
     */
    QUART_IN_OUT((input) -> input < 0.5 ? 8 * input * input * input * input : 1 - Math.pow(-2 * input + 2, 4) / 2),

    /**
     * Speed gradually increases
     */
    QUINT_IN((input) -> input * input * input * input * input),

    /**
     * Speed gradually decreases
     */
    QUINT_OUT((input) -> 1 - Math.pow(1 - input, 5)),

    /**
     * Speed gradually increases until halfway and then decreases
     */
    QUINT_IN_OUT((input) -> input < 0.5 ? 16 * input * input * input * input * input : 1 - Math.pow(-2 * input + 2, 5) / 2),

    /**
     * Speed gradually increases
     */
    EXPO_IN((input) -> input == 0 ? 0 : Math.pow(2, 10 * input - 10)),

    /**
     * Speed gradually decreases
     */
    EXPO_OUT((input) -> input == 1 ? 1 : 1 - Math.pow(2, -10 * input)),

    /**
     * Speed gradually increases until halfway and then decreases
     */
    EXPO_IN_OUT((input) -> input < 0.5 ? 4 * input * input * input : 1 - Math.pow(-2 * input + 2, 3) / 2),

    /**
     * Speed gradually increases
     */
    CIRC_IN((input) -> 1 - Math.sqrt(1 - Math.pow(input, 2))),

    /**
     * Speed gradually decreases
     */
    CIRC_OUT((input) -> Math.sqrt(1 - Math.pow(input - 1, 2))),

    /**
     * Speed gradually increases until halfway and then decreases
     */
    CIRC_IN_OUT((input) -> input < 0.5 ? (1 - Math.sqrt(1 - Math.pow(2 * input, 2))) / 2 : (Math.sqrt(1 - Math.pow(-2 * input + 2, 2)) + 1) / 2);

    // The function that calculates the easing
    private final Function<Double, Double> easeFunction;

    Easing(Function<Double, Double> easeFunction) {
        this.easeFunction = easeFunction;
    }

    /**
     * Apply the easing function to the input
     * @param input The linear animation that we want to ease
     * @return The eased animation
     */
    public double ease(double input) {
        return easeFunction.apply(input);
    }

}
