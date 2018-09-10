package com.radicalninja.pimidithing.util;

import android.support.annotation.NonNull;
import android.util.ArraySet;
import android.util.SparseIntArray;

import java.util.Iterator;
import java.util.Set;

public class NumberMap extends SparseIntArray implements Iterable<NumberMap.Entry> {

    public Set<Entry> entrySet() {
        final int size = size();
        final Set<Entry> result = new ArraySet<>(size);
        for (int i = 0; i < size; i++) {
            result.add(new Entry(keyAt(i), valueAt(i)));
        }
        return result;
    }

    @NonNull
    @Override
    public NumberMapIterator iterator() {
        return new NumberMapIterator();
    }

    public final class NumberMapIterator implements Iterator<Entry> {

        private final int last;
        private int cursor = 0;

        NumberMapIterator() {
            this.last = NumberMap.this.size() - 1;
        }

        @Override
        public boolean hasNext() {
            return cursor < last;
        }

        @Override
        public Entry next() {
            return new Entry(NumberMap.this.keyAt(cursor), NumberMap.this.valueAt(cursor++));
        }

    }

    public static final class Entry {
        private final int key, value;

        public Entry(final int key, final int value) {
            this.key = key;
            this.value = value;
        }

        public int getKey() {
            return key;
        }

        public int getValue() {
            return value;
        }
    }

}
