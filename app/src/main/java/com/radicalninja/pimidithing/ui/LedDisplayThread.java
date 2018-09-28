package com.radicalninja.pimidithing.ui;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.annotation.Size;
import android.util.Log;

import com.eon.androidthings.sensehatdriverlibrary.devices.LedMatrix;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LedDisplayThread extends Thread {

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
    private Job currentJob;
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

    public void queueJob(@NonNull final Job job) {
        if (null == job) {
            throw new IllegalArgumentException("Job must not be null.");
        }
        // TODO: logic for expiring otherwise "endless" loops?
        jobs.add(job);
        if (parked) {
            unpark();
        }
    }

    public void queueJobs(@NonNull final Job[] jobs) {
        if (null == jobs) {
            throw new IllegalArgumentException("Jobs must not be a null array.");
        }
        for (final Job job : jobs) {
            if (null == job) {
                // TODO: log or throw?
            } else {
                this.jobs.add(job);
            }
        }
        if (parked) {
            unpark();
        }
    }

    public void queueJobs(@NonNull final Collection<Job> jobs) {
        if (null == jobs) {
            throw new IllegalArgumentException("Jobs must not be a null collection.");
        }
        queueJobs(jobs.toArray(new Job[jobs.size()]));
    }

    public void execJobImmediately(@NonNull final Job job, final boolean clearQueue) {
        if (null == job) {
            throw new IllegalArgumentException("Job must not be null.");
        }
        if (clearQueue) {
            jobs.clear();
        }
        jobs.add(job);
        if (null != currentJob) {
            currentJob.stop();
        }
        if (parked) {
            unpark();
        }
    }

    public void execJobsImmediately(@NonNull final Job[] jobs, final boolean clearQueue) {
        if (null == jobs) {
            throw new IllegalArgumentException("Jobs must not be a null array.");
        }
        if (clearQueue) {
            this.jobs.clear();
        }
        for (final Job job : jobs) {
            if (null == job) {
                // TODO: log or throw?
            } else {
                this.jobs.add(job);
            }
        }
        if (null != currentJob) {
            currentJob.stop();
        }
        if (parked) {
            unpark();
        }
    }

    public void execJobsImmediately(@NonNull final Collection<Job> jobs, final boolean clearQueue) {
        if (null == jobs) {
            throw new IllegalArgumentException("Jobs must not be a null collection.");
        }
        execJobsImmediately(jobs.toArray(new Job[jobs.size()]), clearQueue);
    }

    /**
     *
     * @param frame
     * @param bounds Rect defining the range of the bitmap to draw. May be null.
     * @param sleepDuration
     * @return True if no exceptions occurred during the draw and sleep opearations.
     */
    protected boolean draw(final Bitmap frame, @Nullable final Rect bounds, final long sleepDuration) {
        boolean success = true;
        try {
            if (null != bounds) {
                ledMatrix.draw(frame, bounds.left, bounds.top, bounds.width(), bounds.height());
            } else {
                ledMatrix.draw(frame);
            }
        } catch (IOException e) {
            success = false;
            e.printStackTrace();
        }
        try {
            if (sleepDuration > 0) {
                Thread.sleep(sleepDuration);
            }
            // TODO: Log zero sleep duration events here?
        } catch (InterruptedException e) {
            success = false;
            e.printStackTrace();
        }
        return success;
    }

    @Override
    public void run() {
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
            currentJob = jobs.poll();
            if (null == currentJob) {
                continue;
            }
            while (currentJob.hasNextFrame() && !currentJob.isExpired()) {
                final Bitmap currentFrame = currentJob.getNextFrame();
                if (null == currentFrame) {
                    // No frame retrieved.
                    // TODO: How do we handle this?
                } else if (currentJob.needsScrolling()) {
                    while (currentJob.hasNextScroll() && !currentJob.isExpired()) {
                        final Rect scroll = currentJob.getNextScroll();
                        if (null == scroll) {
                            // No scroll retrieved.
                            // TODO: How do we handle this?
                        } else {
                            draw(currentFrame, scroll, currentJob.getSleepDuration());
                        }
                    }
                } else {
                    draw(currentFrame, null, currentJob.getSleepDuration());
                }
            }
            currentJob.recycle();
            currentJob = null;
        }
    }

    public static class Job {

        // TODO: Should a Rotation value be provided? Could be changed with each frame as a "rotating job"

        // TODO: RepeatMode logic to be included with cycles and duration monitoring. Can allow us to remove the *Previous* methods?

        private final Bitmap[] frames;
        private final long frameRate;
        private final int cycles;
        private final long minDuration;
        private final long maxDuration;
        private final int rotationOffset;
        private final RepeatMode repeatMode;

        private int i = 0;
        private int cycle = 0;
        private int frame = 0;
        private boolean paused = false;
        private long startTime = 0;
        private boolean stopped = false;

        private int currentRotation;

        private Job(final Bitmap[] frames,
                    final long frameRate,
                    final int cycles,
                    final long minDuration,
                    final long maxDuration,
                    final int startingRotation,
                    final int rotationOffset,
                    final RepeatMode repeatMode) {

            this.frames = frames;
            this.frameRate = frameRate;
            this.cycles = cycles;
            this.minDuration = minDuration;
            this.maxDuration = maxDuration;
            this.currentRotation = startingRotation;
            this.rotationOffset = rotationOffset;
            this.repeatMode = repeatMode;
        }

        void recycle() {
            for (final Bitmap bitmap : frames) {
                bitmap.recycle();
            }
        }

        long getSleepDuration() {
            return (frameRate > 0) ? frameRate : minDuration;
        }

        // TODO: Is this method necessary?
        boolean needsAnimation() {
            return frames.length > 1 && frameRate > 0;
        }

        boolean needsScrolling() {
            return getCurrentFrame().getWidth() > LedMatrix.WIDTH && frameRate > 0;
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
            this.stopped = true;
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

        boolean hasNextScroll() {
            // TODO: consider RepeatMode here
            return false;
        }

        boolean hasPreviousScroll() {
            // TODO
            return false;
        }

        @Nullable
        Rect getCurrentScroll() {
            // TODO
            return null;
        }

        @Nullable
        Rect getNextScroll() {
            // TODO
            return null;
        }

        @Nullable
        Rect getPreviousScroll() {
            // TODO
            return null;
        }

        boolean isFirstScroll() {
            // todo
            return false;
        }

        boolean isLastScroll() {
            // todo
            return false;
        }

        boolean hasNextFrame() {
            // TODO
            return false;
        }

        boolean hasPreviousFrame() {
            // TODO
            return false;
        }

        @NonNull
        Bitmap getCurrentFrame() {
            return frames[frame];
        }

        @Nullable
        Bitmap getNextFrame() {
            // TODO: rely on isExpired + isPaused for these?
            if (!paused && cycles > 0 && cycle < cycles) {
                // current frame = i % cycles ?
                // return current frame.
                return null;
            } else {
                // Nothing to do here.
                return null;
            }
        }

        @Nullable
        Bitmap getPreviousFrame() {
            // TODO: reverse the iterator
            return null;
        }

        boolean isFirstFrame() {
            // todo
            return false;
        }

        boolean isLastFrame() {
            // todo
            return false;
        }

    }

    public static class JobBuilder {

        private Bitmap[] frames;
        private long frameRate = 100;
        private int cycles = 0;
        private long minDuration = 0;
        private long maxDuration = 0;
        private int startingRotation = 0;
        private int rotationOffset = 0;
        private RepeatMode repeatMode = RepeatMode.LOOP;

        public JobBuilder setFrame(@NonNull final Bitmap frame) {
            if (null == frame) {
                throw new IllegalArgumentException("Frame must not be null.");
            }
            this.frames = new Bitmap[]{frame};
            return this;
        }

        public JobBuilder setFrames(@NonNull @Size(min=1) final Bitmap[] frames) {
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
            return this;
        }

        public JobBuilder setFrameRate(final long frameRate) {
            if (frameRate < 0) {
                throw new IllegalArgumentException("Frame rate must be zero or more.");
            }
            this.frameRate = frameRate;
            return this;
        }

        public JobBuilder setCycles(final int cycles) {
            if (cycles < 0) {
                throw new IllegalArgumentException("Cycle count must be zero or more.");
            }
            this.cycles = cycles;
            return this;
        }

        public JobBuilder setMinDuration(final long minDuration) {
            if (minDuration < 0) {
                throw new IllegalArgumentException("Min duration must be zero or more.");
            }
            // TODO: Should we check against / raise a lower maxDuration up to minDuration value here?
            this.minDuration = minDuration;
            return this;
        }

        public JobBuilder setMaxDuration(final long maxDuration) {
            if (maxDuration < 0) {
                throw new IllegalArgumentException("Max duration must be zero or more.");
            }
            this.maxDuration = maxDuration;
            return this;
        }

        public JobBuilder setStartingRotation(final int startingRotation) {
            this.startingRotation = startingRotation;
            return this;
        }

        public JobBuilder setRotationOffset(final int rotationOffset) {
            this.rotationOffset = rotationOffset;
            return this;
        }

        public JobBuilder setRepeatMode(final RepeatMode repeatMode) {
            if (null == repeatMode) {
                throw new IllegalArgumentException("Repeat mode must not be null.");
            }
            this.repeatMode = repeatMode;
            return this;
        }

        @NonNull
        public Job build() {
            if (null == frames) {
                throw new IllegalArgumentException("Frames are not set!");
            }
            return new Job(frames, frameRate, cycles, minDuration, maxDuration,
                    startingRotation, rotationOffset, repeatMode);
        }

    }

}
