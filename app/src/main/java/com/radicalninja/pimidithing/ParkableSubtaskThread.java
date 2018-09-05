package com.radicalninja.pimidithing;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.Nullable;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

public class ParkableSubtaskThread extends Thread {

    // TODO: Should these interfaces be converted into classes? They probably won't be useful anywhere else.
    public interface ParkingValet {
        boolean isParked();
        // TODO: Add ParkUntil/ParkNanos methods
        @Nullable
        <T> T park(final Class<T> resultClass) throws InterruptedException;
    }

    public interface RetrievingValet {
        boolean isParked();
        void unpark();
        <T> void unpark(final T result);
    }

    private final AtomicBoolean parked = new AtomicBoolean(false);
    private final ConcurrentLinkedQueue<Object> resultQueue = new ConcurrentLinkedQueue<>();

    public ParkableSubtaskThread(final ParkableThreadRunnable target) {
        // TODO: Figure out a way to re-use thread for other Runnables after initial run
        super(target);
        target.parkingValet = parkingValet;
        target.retrievingValet = retrievingValet;
    }

    private final ParkingValet parkingValet = new ParkingValet() {
        @Override
        public <T> T park(final Class<T> resultClass) throws InterruptedException {
            // If this is called while already parked, we'll ignore it.
            if (ParkableSubtaskThread.this.parked.get()) {
                // TODO: Should some logging take place when ignored?
                return null;
            }
            ParkableSubtaskThread.this.parked.set(true);
            LockSupport.park();
            if (Thread.interrupted()) {
                throw new InterruptedException("Thread was interrupted before .unpark() was called.");
            }
            return (T) resultQueue.poll();
        }

        @Override
        public boolean isParked() {
            return ParkableSubtaskThread.this.isParked();
        }
    };

    private final RetrievingValet retrievingValet = new RetrievingValet() {
        @Override
        public boolean isParked() {
            return ParkableSubtaskThread.this.isParked();
        }

        @Override
        public void unpark() {
            unpark(null);
        }

        @Override
        public <T> void unpark(final T result) {
            if (null != result) {
                resultQueue.add(result);
            }
            ParkableSubtaskThread.this.parked.compareAndSet(true, false);
            LockSupport.unpark(ParkableSubtaskThread.this);
        }
    };

    static {
        // Reduce the risk of "lost unpark" due to classloading
        Class<?> ensureLoaded = LockSupport.class;
    }

    public boolean isParked() {
        return parked.get();
    }

    public static abstract class ParkableThreadRunnable implements Runnable {

        private static final String HANDLER_THREAD_NAME = "ParkableThreadSubtaskHandler";

        private Handler subtaskHandler;
        private HandlerThread subtaskThread;
        private ParkingValet parkingValet;
        private RetrievingValet retrievingValet;

        public abstract void run(final ParkingValet parkingValet);

        @Override
        public void run() {
            run(parkingValet);
        }

        public ParkingValet getParkingValet() {
            return parkingValet;
        }

        public RetrievingValet getRetrievingValet() {
            return retrievingValet;
        }

        public Looper getSubtaskLooper() {
            if (null == subtaskThread) {
                subtaskThread = new HandlerThread(HANDLER_THREAD_NAME);
                subtaskThread.start();
            }
            return subtaskThread.getLooper();
        }

        public <T> T parkAndExecSubtask(final SubtaskThreadRunnable<T> subtaskRunnable)
                throws InterruptedException {

            if (null != subtaskHandler) {
                // TODO: Bail out here? or wait until the handler is finished?
            }
            subtaskRunnable.retrievingValet = retrievingValet;
            subtaskHandler = new Handler(getSubtaskLooper());
            subtaskHandler.post(subtaskRunnable);
            return parkingValet.park(subtaskRunnable.resultClass);
        }

    }

    public static abstract class SubtaskThreadRunnable<T> implements Runnable {

        private final Class<T> resultClass;

        private RetrievingValet retrievingValet;

        public SubtaskThreadRunnable(final Class<T> resultClass) {
            this.resultClass = resultClass;
        }

        public abstract void run(final RetrievingValet retrievingValet);

        @Override
        public void run() {
            run(retrievingValet);
            // If the main thread has not been unparked yet, unpark before the runnable finishes.
            if (retrievingValet.isParked()) {
                retrievingValet.unpark();
            }
        }

    }

}
