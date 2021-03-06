package com.radicalninja.pimidithing;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.ArraySet;

import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CountDownWorkerThread extends Thread {

    public interface Latcher {
        boolean isLatched();

        @Nullable
        <T> T latch() throws InterruptedException;

        @Nullable
        <T> T latch(final long timeout, final TimeUnit unit) throws InterruptedException;

        <T> Set<T> latch(final int latches, final boolean trimNullResults)
                throws InterruptedException, NegativeArraySizeException;

        <T> Set<T> latch(final int latches,
                         final boolean trimNullResults,
                         final long timeout,
                         final TimeUnit unit)
                throws InterruptedException, NegativeArraySizeException;
    }

    public interface Unlatcher {
        boolean isLatched();

        void unlatch();

        <T> void unlatch(final T result);

        void unlatchAll();

        <T> void unlatchAll(final T result);
    }

    private static final String TAG = CountDownWorkerThread.class.getCanonicalName();

    private final AtomicInteger latchCount = new AtomicInteger(0);
    private final ConcurrentLinkedQueue<SubtaskResult> resultQueue = new ConcurrentLinkedQueue<>();

    private CountDownLatch latch;

    public CountDownWorkerThread(final CountDownRunnable target) {
        super(target);
        target.latcher = latcher;
        target.unlatcher = unlatcher;
    }

    public boolean isLatched() {
        return latchCount.get() > 0;
    }

    @SuppressWarnings("FieldCanBeLocal")
    private final Latcher latcher = new Latcher() {
        @Override
        public boolean isLatched() {
            return CountDownWorkerThread.this.isLatched();
        }

        @Nullable
        @Override
        public <T> T latch() throws InterruptedException {
            final int currentLatches = latchCount.incrementAndGet();
            if (currentLatches > 0) {
                latch = new CountDownLatch(currentLatches);
                latch.await();
            }
            return (T) resultQueue.poll().result;
        }

        @Nullable
        @Override
        public <T> T latch(final long timeout, final TimeUnit unit) throws InterruptedException {
            final int currentLatches = latchCount.incrementAndGet();
            if (currentLatches > 0) {
                latch = new CountDownLatch(currentLatches);
                final boolean timedOut = latch.await(timeout, unit);
                if (timedOut) {
                    return null;
                    // TODO: How should following calls to unlatch() be handled in the case of a timeout?
                }
            }
            return (T) resultQueue.poll().result;
        }

        @Override
        public <T> Set<T> latch(final int latches, final boolean trimNullResults)
                throws InterruptedException, NegativeArraySizeException {

            if (latches < 0) {
                throw new NegativeArraySizeException("Number of latches was less than zero.");
            }
            final Set<T> result = new ArraySet<>(latches);
            final int currentLatches = latchCount.addAndGet(latches);
            if (currentLatches > 0) {
                latch = new CountDownLatch(currentLatches);
                latch.await();
            }
            for (int i = 0; i < latches; i++) {
                final SubtaskResult<T> value = resultQueue.poll();
                if (trimNullResults && null == value.result) {
                    continue;
                }
                result.add(value.result);
            }
            return result;
        }

        @Override
        public <T> Set<T> latch(final int latches,
                                final boolean trimNullResults,
                                final long timeout,
                                final TimeUnit unit)
                throws InterruptedException, NegativeArraySizeException {

            if (latches < 0) {
                throw new NegativeArraySizeException("Number of latches was less than zero.");
            }
            final Set<T> result = new ArraySet<>(latches);
            final int currentLatches = latchCount.addAndGet(latches);
            if (currentLatches > 0) {
                latch = new CountDownLatch(currentLatches);
                final boolean timedOut = latch.await(timeout, unit);
                // TODO: What should be done here if timedOut is true? Can we gauge how many latches are left with latchCount?
            }
            for (int i = 0; i < latches; i++) {
                final SubtaskResult<T> value = resultQueue.poll();
                if (trimNullResults && null == value.result) {
                    continue;
                }
                result.add(value.result);
            }
            return result;
        }
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final Unlatcher unlatcher = new Unlatcher() {
        @Override
        public boolean isLatched() {
            return CountDownWorkerThread.this.isLatched();
        }

        @Override
        public void unlatch() {
            resultQueue.add(new SubtaskResult(null));
            latchCount.decrementAndGet();
            if (null != latch) {
                latch.countDown();
            }
        }

        @Override
        public <T> void unlatch(T result) {
            resultQueue.add(new SubtaskResult(result));
            latchCount.decrementAndGet();
            if (null != latch) {
                latch.countDown();
            }
        }

        @Override
        public void unlatchAll() {
            final int latches = latchCount.get();
            if (latches > 0) {
                latchCount.addAndGet(-latches); // Reduce to zero
                for (int i = 0; i < latches; i++) {
                    resultQueue.add(new SubtaskResult(null));
                    if (null != latch) {
                        latch.countDown();
                    }
                }
            }
        }

        @Override
        public <T> void unlatchAll(T result) {
            unlatch(result);
            unlatchAll();
        }
    };

    public static abstract class CountDownRunnable<D> implements Runnable {

        private static final String HANDLER_THREAD_NAME = "CountDownWorkerThreadHandler";
//        private static final int POST_DELAY_MS = 25;

        private D data;

        private Handler subtaskHandler;
        private HandlerThread subtaskThread;
        // TODO: Should Latcher/Unlatcher be created on a per-subtaskRunnable-basis?
        // TODO: Same for Atomic values. Prevent possible collisions if it ends up being used in two spots at once... right?
        private Latcher latcher;
        private Unlatcher unlatcher;

        public CountDownRunnable() {
            //
        }

        public CountDownRunnable(D data) {
            this.data = data;
        }

        public abstract void run(final Latcher latcher);

        @Override
        public void run() {
            run(latcher);
        }

        @Nullable
        public D getData() {
            return data;
        }

        public void setData(final D data) {
            this.data = data;
        }

        public Latcher getLatcher() {
            return latcher;
        }

        public Unlatcher getUnlatcher() {
            return unlatcher;
        }

        public Looper getSubtaskLooper() {
            if (null == subtaskThread) {
                subtaskThread = new HandlerThread(HANDLER_THREAD_NAME);
                subtaskThread.start();
            }
            return subtaskThread.getLooper();
        }

        public <T> T latchAndExecSubtask(final SubtaskRunnable<T> subtaskRunnable)
                throws InterruptedException {

            if (null != subtaskHandler) {
                // TODO: Bail out here? or wait until the handler is finished?
            }
            subtaskRunnable.unlatcher = unlatcher;
            subtaskHandler = new Handler(getSubtaskLooper());
            subtaskHandler.post(subtaskRunnable);
//            subtaskHandler.postDelayed(subtaskRunnable, POST_DELAY_MS);
            return latcher.latch();
        }

        public <T> Set<T> latchAndExecSubtask(final SubtaskRunnable<T> subtaskRunnable,
                                              final int latches,
                                              final boolean trimNullResults)
                throws InterruptedException {

            if (null != subtaskHandler) {
                // TODO: Bail out here? or wait until the handler is finished?
            }
            subtaskRunnable.unlatcher = unlatcher;
            subtaskHandler = new Handler(getSubtaskLooper());
            subtaskHandler.post(subtaskRunnable);
//            subtaskHandler.postDelayed(subtaskRunnable, POST_DELAY_MS);
            return latcher.latch(latches, trimNullResults);
        }

    }

    public static abstract class SubtaskRunnable<T> implements Runnable {

        private Unlatcher unlatcher;

        public abstract void run(final Unlatcher unlatcher);

        @Override
        public void run() {
            run(unlatcher);
        }

    }

    private static class SubtaskResult<T> {

        private final T result;

        SubtaskResult(final T result) {
            this.result = result;
        }

    }

}
