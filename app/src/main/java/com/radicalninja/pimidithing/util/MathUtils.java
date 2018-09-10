package com.radicalninja.pimidithing.util;

public class MathUtils {

    private MathUtils() {}

    /**
     * Ensure that a given value is within the `min`/`max` range. If not, the resulting value
     * will be clipped to the value it falls outside of.
     *
     * @param value the value to be clipped.
     * @param min minimum range value.
     * @param max maximum range value.
     *
     * @return the clipped value.
     */
    public static float clipToRange(final float value, float min, float max) {
        // Swap min and max if fed in the wrong order
        if (min > max) {
            min += (max - (max = min));
        }
        return (value < min)
                ? min
                : (value > max) ? max : value;
    }

    /**
     * Determine if a given value is within the `min`/`max` range.
     * @param value the value to check.
     * @param min minimum range value.
     * @param max maximum range value.
     * @return true if the value is within the range.
     */
    public static boolean withinRange(final float value, final float min, final float max) {
        return clipToRange(value, min, max) - value == 0;
    }

    /**
     * Ensure that a given value is within the `min`/`max` range. If not, the resulting value
     * will be clipped to the value it falls outside of.
     *
     * @param value the value to be clipped.
     * @param min minimum range value.
     * @param max maximum range value.
     *
     * @return the clipped value.
     */
    public static double clipToRange(final double value, double min, double max) {
        // Swap min and max if fed in the wrong order
        if (min > max) {
            min += (max - (max = min));
        }
        return (value < min)
                ? min
                : (value > max) ? max : value;
    }

    /**
     * Determine if a given value is within the `min`/`max` range.
     * @param value the value to check.
     * @param min minimum range value.
     * @param max maximum range value.
     * @return true if the value is within the range.
     */
    public static boolean withinRange(final double value, final double min, final double max) {
        return clipToRange(value, min, max) - value == 0;
    }

    /**
     * Ensure that a given value is within the `min`/`max` range. If not, the resulting value
     * will be clipped to the value it falls outside of.
     *
     * @param value the value to be clipped.
     * @param min minimum range value.
     * @param max maximum range value.
     *
     * @return the clipped value.
     */
    public static int clipToRange(final int value, int min, int max) {
        // Swap min and max if fed in the wrong order
        if (min > max) {
            min += (max - (max = min));
        }
        return (value < min)
                ? min
                : (value > max) ? max : value;
    }

    /**
     * Determine if a given value is within the `min`/`max` range.
     * @param value the value to check.
     * @param min minimum range value.
     * @param max maximum range value.
     * @return true if the value is within the range.
     */
    public static boolean withinRange(final int value, final int min, final int max) {
        return clipToRange(value, min, max) - value == 0;
    }

}
