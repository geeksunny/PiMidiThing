package com.radicalninja.pimidithing.util;

import android.support.annotation.NonNull;

import java.util.List;

public class ArrayUtils {

    public static <T> void addArrayToList(@NonNull final T[] array, @NonNull final List<T> list) {
        if (null == array || null == list) {
            return;
        }
        for (final T item : array) {
            list.add(item);
        }
    }

}
