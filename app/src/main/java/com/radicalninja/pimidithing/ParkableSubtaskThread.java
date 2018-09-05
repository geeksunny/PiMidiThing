package com.radicalninja.pimidithing;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.Nullable;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

public class ParkableSubtaskThread<T> extends Thread {

    // TODO: Should these interfaces be converted into classes? They probably won't be useful anywhere else.
    public interface ParkingValet<T> {
        boolean isParked();
        // TODO: Add ParkUntil/ParkNanos methods
        @Nullable
        T park() throws InterruptedException;
    }

    public interface RetrievingValet<T> {
        boolean isParked();
        void unpark();
        void unpark(final T result);
    }

    private final AtomicBoolean parked = new AtomicBoolean(false);
    private final ConcurrentLinkedQueue<T> resultQueue = new ConcurrentLinkedQueue<>();

    public ParkableSubtaskThread(final ParkableThreadRunnable<T> target) {
        super(target);
        target.parkingValet = parkingValet;
        target.retrievingValet = retrievingValet;
    }

    final ParkingValet<T> parkingValet = new ParkingValet<T>() {
        @Override
        public T park() throws InterruptedException {
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
            return resultQueue.poll();
        }

        @Override
        public boolean isParked() {
            return ParkableSubtaskThread.this.isParked();
        }
    };

    final RetrievingValet<T> retrievingValet = new RetrievingValet<T>() {
        @Override
        public boolean isParked() {
            return ParkableSubtaskThread.this.isParked();
        }

        @Override
        public void unpark() {
            unpark(null);
        }

        @Override
        public void unpark(final T result) {
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

    public static abstract class ParkableThreadRunnable<T> implements Runnable {

        private static final String HANDLER_THREAD_NAME = "ParkableThreadSubtaskHandler";

        private Handler subtaskHandler;
        private HandlerThread subtaskThread;
        private ParkingValet<T> parkingValet;
        private RetrievingValet<T> retrievingValet;

        public abstract void run(final ParkingValet<T> parkingValet);

        @Override
        public void run() {
            run(parkingValet);
        }

        public ParkingValet<T> getParkingValet() {
            return parkingValet;
        }

        public RetrievingValet<T> getRetrievingValet() {
            return retrievingValet;
        }

        public Looper getSubtaskLooper() {
            if (null == subtaskThread) {
                subtaskThread = new HandlerThread(HANDLER_THREAD_NAME);
                subtaskThread.start();
            }
            return subtaskThread.getLooper();
        }

        public T parkAndExecSubtask(final SubtaskThreadRunnable<T> subtaskRunnable)
                throws InterruptedException {

            if (null != subtaskHandler) {
                // TODO: Bail out here? or wait until the handler is finished?
            }
            subtaskRunnable.retrievingValet = retrievingValet;
            subtaskHandler = new Handler(getSubtaskLooper());
            subtaskHandler.post(subtaskRunnable);
            return parkingValet.park();
        }

    }

    public static abstract class SubtaskThreadRunnable<T> implements Runnable {

        private RetrievingValet<T> retrievingValet;

        public abstract void run(final RetrievingValet<T> retrievingValet);

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
