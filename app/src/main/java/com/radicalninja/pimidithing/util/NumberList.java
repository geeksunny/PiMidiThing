package com.radicalninja.pimidithing.util;

import android.util.SparseBooleanArray;

public class NumberList extends SparseBooleanArray {

    public void add(final int number) {
        put(number, true);
    }

    public boolean has(final int number) {
        return get(number);
    }

}
