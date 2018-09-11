package com.radicalninja.pimidithing.util;

import android.support.annotation.NonNull;
import android.util.SparseBooleanArray;

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.function.IntConsumer;

public class NumberArray implements Iterable<Integer> {

    private final SparseBooleanArray array = new SparseBooleanArray();

    public void add(final int number) {
        array.put(number, true);
    }

    public int[] getAll() {
        final int[] results = new int[size()];
        final NumberArrayIterator i = iterator();
        int num = 0;
        while (i.hasNext()) {
            results[num++] = i.nextInt();
        }
        return results;
    }

    public int get(final int index) {
        return array.keyAt(index);
    }

    public boolean has(final int number) {
        return array.get(number);
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public void populate(final int[] numbers) {
        for (final int number : numbers) {
            add(number);
        }
    }

    public int size() {
        return array.size();
    }

    @NonNull
    @Override
    public NumberArrayIterator iterator() {
        return new NumberArrayIterator();
    }

    public final class NumberArrayIterator implements PrimitiveIterator.OfInt {

        private final int last;
        private int cursor = 0;

        NumberArrayIterator() {
            this.last = NumberArray.this.size() - 1;
        }

        @Override
        public int nextInt() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return NumberArray.this.get(cursor++);
        }

        @Override
        public boolean hasNext() {
            return cursor < last;
        }

        @Override
        public void forEachRemaining(IntConsumer action) {
            // TODO?
        }

        @Override
        public Integer next() {
            return nextInt();
        }
    }

}
