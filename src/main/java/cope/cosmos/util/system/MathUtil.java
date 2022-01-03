package cope.cosmos.util.system;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.objects.Object2FloatFunction;
import it.unimi.dsi.fastutil.objects.Object2IntFunction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

public class MathUtil {

    public static double roundDouble(double number, int scale) {
        BigDecimal bd = new BigDecimal(number);
        bd = bd.setScale(scale, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static float roundFloat(double number, int scale) {
        BigDecimal bd = BigDecimal.valueOf(number);
        bd = bd.setScale(scale, RoundingMode.FLOOR);
        return bd.floatValue();
    }

    public static <T> int sumOfInt(Collection<T> objs, Object2IntFunction<T> function) {
        int sum = 0;
        for (T obj: objs) {
            sum += function.getInt(obj);
        }
        return sum;
    }

    public static <T> float sumOfFloat(Collection<T> objs, Object2FloatFunction<T> function) {
        float sum = 0;
        for (T obj: objs) {
            sum += function.getFloat(obj);
        }
        return sum;
    }

    public static <T> int maxOfInt(Collection<T> objs, Object2IntFunction<T> function) {
        int max = Integer.MIN_VALUE;
        for (T obj: objs) {
            int value = function.getInt(obj);
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    public static <T> float maxOfFloat(Collection<T> objs, Object2FloatFunction<T> function) {
        float max = Float.MIN_VALUE;
        for (T obj: objs) {
            float value = function.getFloat(obj);
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    public static float average(FloatArrayList values) {
        if (values.size() == 0) {
            return 0;
        }
        float sum = 0;
        for (float value: values) {
            sum += value;
        }
        return sum / values.size();
    }

    public static double roundOnSet(double value, int places) {
        final double scale = Math.pow(10.0, places);
        return Math.round(value * scale) / scale;
    }
}
