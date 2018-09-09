package com.radicalninja.pimidithing.util;

import android.util.SparseBooleanArray;

public class NumberArray extends SparseBooleanArray {

    public void add(final int number) {
        put(number, true);
    }

    public boolean has(final int number) {
        return get(number);
    }

    public void populate(final int[] numbers) {
        for (final int number : numbers) {
            add(number);
        }
    }

}
