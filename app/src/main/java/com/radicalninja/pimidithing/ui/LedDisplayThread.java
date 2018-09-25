package com.radicalninja.pimidithing.ui;

import android.graphics.Bitmap;
import android.support.annotation.Size;
import android.util.Log;

import com.eon.androidthings.sensehatdriverlibrary.devices.LedMatrix;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LedDisplayThread extends Thread {

    // TODO: implement job queue with immediate displays.
    // TODO: use LockSupport.park/unpark to pause between jobs.
    // TODO: Display rotation should be handled here, scrolling calculations built in.

    public enum Rotation {

        NONE(0),
        CW1(90),
        CW2(180),
        CW3(270),
        CCW1(270),
        CCW2(180),
        CCW3(90);

        private final int value;

        Rotation(final int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

    }

    public enum RepeatMode {
        NONE,
        LOOP,
        PINGPONG
    }

    private static final String TAG = LedDisplayThread.class.getCanonicalName();

    private final ConcurrentLinkedQueue<Job> jobs = new ConcurrentLinkedQueue<>();
    private final LedMatrix ledMatrix;

    private boolean parked = false;
    private Rotation rotation = Rotation.NONE;

    public LedDisplayThread(@NonNull final LedMatrix ledMatrix) {
        this.ledMatrix = ledMatrix;
    }

    public LedDisplayThread(@NonNull final LedMatrix ledMatrix, @Nullable final Rotation rotation) {
        this.ledMatrix = ledMatrix;
        if (null != rotation) {
            this.rotation = rotation;
        }
    }

    protected void park() throws InterruptedException {
        if (parked) {
            // is there anything we should do here? block until its park call can take place?
            return;
        }
        parked = true;
        LockSupport.park();
        if (this.isInterrupted()) {
            throw new InterruptedException("Thread was interrupted while parked.");
        } else if (parked) {
            throw new InterruptedException("Thread was spuriously unparked.");
        }
    }

    protected void unpark() {
        if (!parked) {
            // anything to be done here in this case?
            return;
        }
        parked = false;
        LockSupport.unpark(this);
    }

    public boolean isParked() {
        return parked;
    }

    public void queueJob(final Job job) {
        // TODO: logic for expiring otherwise "endless" loops?
        jobs.add(job);
        if (parked) {
            unpark();
        }
    }

    public void execJobImmediately(final Job job, final boolean clearQueue) {
        if (clearQueue) {
            jobs.clear();
        }
        // TODO: Stop / interrupt currently executing job
        if (parked) {
            unpark();
        }
    }

    @Override
    public void run() {
        // TODO: execute all queued jobs
        // TODO: park thread if no more jobs
        while (true) {
            if (jobs.peek() == null) {
                Log.d(TAG, "Job queue is empty; Parking thread.");
                try {
                    park();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    parked = false;
                }
            }
            final Job currentJob = jobs.poll();
            if (null == currentJob) {
                continue;
            }
            Bitmap currentFrame;
            while ((currentFrame = currentJob.getFrame()) != null) {
                try {
                    ledMatrix.draw(currentFrame);
                } catch (IOException e) {
                    // TODO: Interrupt job here?
                    e.printStackTrace();
                }
                // TODO: Thread.sleep(frameRate) if is animatable...
                // If not animated, Thread.sleep for maxDuration???
            }
        }
    }

    public static class Job {

        // TODO: rotation orientation value control
        // TODO: Strings are implemented by way of their wide bitmaps. automatic scrolling with a default speed, and ability to override these settings

        // TODO: Should a Rotation value be provided? Could be changed with each frame as a "rotating job"

        private final Bitmap[] frames;
        private final long frameRate;
        private final int cycles;
        private final long maxDuration;
        private final RepeatMode repeatMode;

        private int i = 0;
        private boolean paused = false;
        private long startTime = 0;

        private Job(final Bitmap[] frames,
                    final long frameRate,
                    final int cycles,
                    final long maxDuration,
                    final RepeatMode repeatMode) {

            this.frames = frames;
            this.frameRate = frameRate;
            this.cycles = cycles;
            this.maxDuration = maxDuration;
            this.repeatMode = repeatMode;
        }

        boolean needsAnimation() {
            return frames.length > 1 && frameRate > 0;
        }

        boolean needsScrolling() {
            return frames[0].getWidth() > LedMatrix.WIDTH && frameRate > 0;
        }

        void pause() {
            this.paused = true;
        }

        void unpause() {
            this.paused = false;
            // TODO: Start job back up? should paused return a null value as frame to signal?
            // TODO: should maxDuration take into account pausedTime?
        }

        boolean isPaused() {
            return paused;
        }

        void stop() {
            // TODO: interrupt the job? Or should this be pushed back to thread class?
        }

        void start() {
            // TODO: set startTime for use in isExpired
        }

        boolean isStarted() {
            return startTime > 0;
        }

        boolean isExpired() {
            // TODO: check system clock against duration __AND__ max cycles
            return false;
        }

        @Nullable
        Bitmap getFrame() {
            // TODO: rely on isExpired + isPaused for these?
            if (!paused && cycles > 0 && i < cycles) {
                // current frame = i % cycles ?
                // return current frame.
                return null;
            } else {
                // Nothing to do here.
                return null;
            }
        }

        void recycle() {
            for (final Bitmap bitmap : frames) {
                bitmap.recycle();
            }
        }

    }

    public static class JobBuilder {

        private Bitmap[] frames;
        private long frameRate = 100;
        private int cycles = 0;
        private long maxDuration = 0;
        private RepeatMode repeatMode = RepeatMode.LOOP;

        public void setFrame(@NonNull final Bitmap frame) {
            if (null == frame) {
                throw new IllegalArgumentException("Frame must not be null.");
            }
            this.frames = new Bitmap[]{frame};
        }

        public void setFrames(@NonNull @Size(min=1) final Bitmap[] frames) {
            if (null == frames) {
                throw new IllegalArgumentException("Frames must not be null.");
            } else if (frames.length == 0) {
                throw new IllegalArgumentException("Frames must not be empty.");
            }
            for (final Bitmap frame : frames) {
                if (null == frame) {
                    throw new IllegalArgumentException("Found null entry in frames.");
                }
            }
            this.frames = Arrays.copyOf(frames, frames.length);
        }

        public void setFrameRate(final long frameRate) {
            if (frameRate < 0) {
                throw new IllegalArgumentException("Frame rate must be zero or more.");
            }
            this.frameRate = frameRate;
        }

        public void setCycles(final int cycles) {
            if (cycles < 0) {
                throw new IllegalArgumentException("Cycle count must be zero or more.");
            }
            this.cycles = cycles;
        }

        public void setMaxDuration(final long maxDuration) {
            if (maxDuration < 0) {
                throw new IllegalArgumentException("Max duration must be zero or more.");
            }
            this.maxDuration = maxDuration;
        }

        public void setRepeatMode(final RepeatMode repeatMode) {
            if (null == repeatMode) {
                throw new IllegalArgumentException("Repeat mode must not be null.");
            }
            this.repeatMode = repeatMode;
        }

        @NonNull
        public Job build() {
            if (null == frames) {
                throw new IllegalArgumentException("Frames are not set!");
            }
            return new Job(frames, frameRate, cycles, maxDuration, repeatMode);
        }

    }

}
