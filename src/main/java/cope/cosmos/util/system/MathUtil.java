package cope.cosmos.util.system;

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
}
