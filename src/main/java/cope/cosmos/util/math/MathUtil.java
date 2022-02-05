package cope.cosmos.util.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author linustouchtips
 * @since 05/06/2021
 */
public class MathUtil {

    /**
     * Rounds a double to the nearest decimal scale
     * @param number The double to round
     * @param scale The decimal points
     * @return The rounded double
     */
    public static double roundDouble(double number, int scale) {
        BigDecimal bigDecimal = new BigDecimal(number);

        // round
        bigDecimal = bigDecimal.setScale(scale, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }

    /**
     * Rounds a float to the nearest decimal scale
     * @param number The float to round
     * @param scale The decimal points
     * @return The rounded float
     */
    public static float roundFloat(double number, int scale) {
        BigDecimal bigDecimal = BigDecimal.valueOf(number);

        // round
        bigDecimal = bigDecimal.setScale(scale, RoundingMode.FLOOR);
        return bigDecimal.floatValue();
    }

    /**
     * Takes a number to an exponent (around 6.96x faster than {@link Math} Math.pow())
     * @param num The number to take to an exponent
     * @param exponent The exponent
     * @return The number to an exponent
     */
    public static double toExponent(double num, int exponent) {
        double result = 1;

        // abs, inverse
        if (exponent < 0) {
            int exponentAbs = Math.abs(exponent);

            while (exponentAbs > 0) {
                if ((exponentAbs & 1) != 0) {
                    result *= num;
                }

                exponentAbs >>= 1;
                num *= num;
            }

            return 1 / result;
        }

        else {
            while (exponent > 0) {
                if ((exponent & 1) != 0) { // 1.5% faster
                    result *= num;
                }

                exponent >>= 1; // bitshift fuckery
                num *= num;
            }

            return result;
        }
    }
}
