package com.radicalninja.pimidithing.util;

import android.support.annotation.NonNull;
import android.util.SparseBooleanArray;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.function.IntConsumer;

public class NumberArray implements Iterable<Integer> {

    private final SparseBooleanArray array = new SparseBooleanArray();

    public void add(final int number) {
        array.put(number, true);
    }

    public boolean has(final int number) {
        return array.get(number);
    }

    public int get(final int index) {
        return array.keyAt(index);
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
    public Iterator<Integer> iterator() {
        return new NumberArrayIterator();
    }

    public final class NumberArrayIterator implements PrimitiveIterator.OfInt {

        private final int last;
        private int cursor = 0;

        public NumberArrayIterator() {
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
