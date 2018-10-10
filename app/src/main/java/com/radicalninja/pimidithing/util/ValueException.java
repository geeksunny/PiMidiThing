package com.radicalninja.pimidithing.util;

import java.util.Locale;

public class ValueException extends IllegalArgumentException {

    public ValueException(final String s) {
        super(s);
    }

    public static class BelowMinimumValueException extends ValueException {

        public BelowMinimumValueException(final String name, final int minValue) {
            super(String.format(Locale.US, "%s must have a minimum value of %d.", name, minValue));
        }

        // TODO: Is this (or int) redundant?
        public BelowMinimumValueException(final String name, final long minValue) {
            super(String.format(Locale.US, "%s must have a minimum value of %d.", name, minValue));
        }

        // TODO: Is this redundant?
//    public BelowMinimumValueException(final String name, final float minValue) {
//        super(String.format(Locale.US, "%s must have a minimum value of %f.", name, minValue));
//    }

        public BelowMinimumValueException(final String name, final double minValue) {
            super(String.format(Locale.US, "%s must have a minimum value of %f.", name, minValue));
        }

    }

    public static class ValueIsNullException extends ValueException {

        public ValueIsNullException(final String name) {
            super(String.format(Locale.US, "%s must not be null.", name));
        }

    }

}
