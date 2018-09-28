package com.radicalninja.pimidithing.ui;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

import com.eon.androidthings.sensehatdriverlibrary.devices.LedMatrix;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;

public class LedDisplayThread extends Thread {

    public interface JobDirection {
        int totalIndexes(final int lastIndex);
        int nextIndex(final int currentIndex, final int lastIndex);
    }

    public static final JobDirection NONE = new JobDirection() {
        @Override
        public int totalIndexes(final int lastIndex) {
            return 1;
        }

        @Override
        public int nextIndex(final int currentIndex, final int lastIndex) {
            return currentIndex;
        }
    };

    public static final JobDirection FORWARD = new JobDirection() {
        @Override
        public int totalIndexes(final int lastIndex) {
            return lastIndex + 1;
        }

        @Override
        public int nextIndex(final int currentIndex, final int lastIndex) {
            return (currentIndex == lastIndex) ? 0 : currentIndex + 1;
        }
    };

    public static final JobDirection REVERSE = new JobDirection() {
        @Override
        public int totalIndexes(final int lastIndex) {
            return lastIndex + 1;
        }

        @Override
        public int nextIndex(final int currentIndex, final int lastIndex) {
            return (currentIndex == 0) ? lastIndex : currentIndex - 1;
        }
    };

    public static final JobDirection PINGPONG = new JobDirection() {
        @Override
        public int totalIndexes(final int lastIndex) {
            return lastIndex * 2;
        }

        @Override
        public int nextIndex(final int currentIndex, final int lastIndex) {
            // TODO: How to maintain direction here?
            if (currentIndex == lastIndex) {

            } else if (currentIndex == 0) {

            } else {

            }
            return 0;
        }
    };

    private static final String TAG = LedDisplayThread.class.getCanonicalName();

    private final ConcurrentLinkedQueue<Job> jobs = new ConcurrentLinkedQueue<>();
    private final LedMatrix ledMatrix;

    private boolean parked = false;
    private Job currentJob;

    public LedDisplayThread(@NonNull final LedMatrix ledMatrix) {
        this.ledMatrix = ledMatrix;
    }

    public LedDisplayThread(@NonNull final LedMatrix ledMatrix,
                            @IntRange(from = -3, to = 3) final int rotations) {

        this.ledMatrix = ledMatrix;
        this.ledMatrix.setRotation(rotations);
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

    public static class JobDirectionHandler {

        private final JobDirection jobDirection;

        private boolean looping;
        private boolean shuffled;

        public JobDirectionHandler(@NonNull final JobDirection jobDirection,
                                   final boolean looping,
                                   final boolean shuffled) {

            if (null == jobDirection) {
                throw new IllegalArgumentException("JobDirection must not be null.");
            }
            this.jobDirection = jobDirection;
            this.looping = looping;
            this.shuffled = shuffled;
        }

        public int totalIndexes(final int lastIndex) {
            return jobDirection.totalIndexes(lastIndex);
        }

        public int nextIndex(final int currentIndex, final int lastIndex) {
            return jobDirection.nextIndex(currentIndex, lastIndex);
        }

        public boolean isLooping() {
            return looping;
        }

        public void setLooping(final boolean looping) {
            this.looping = looping;
        }

        public boolean isShuffled() {
            return shuffled;
        }

        public void setShuffled(final boolean shuffled) {
            this.shuffled = shuffled;
        }

    }

    public static class Job {

        // TODO: increment cycle count somewhere in the scroll / frame logic

        private final Bitmap[] frames;
        private final long frameRate;
        private final int scrollRate;
        private final int cycles;
        private final long minDuration;
        private final long maxDuration;
        private final int rotationOffset;
        private final JobDirectionHandler directionHandler;

        private int frameIndex = -1;
        private int scrollIndex = -1;
        private int scrollCount = 0;
        private int cycle = 0;
        private int frame = 0;
        private boolean paused = false;
        private long startTime = 0;
        private boolean stopped = false;

        private int currentRotation;

        private Job(final Bitmap[] frames,
                    final long frameRate,
                    final int scrollRate,
                    final int cycles,
                    final long minDuration,
                    final long maxDuration,
                    final int startingRotation,
                    final int rotationOffset,
                    final JobDirectionHandler directionHandler) {

            this.frames = frames;
            this.frameRate = frameRate;
            this.scrollRate = scrollRate;
            this.cycles = cycles;
            this.minDuration = minDuration;
            this.maxDuration = maxDuration;
            this.currentRotation = startingRotation;
            this.rotationOffset = rotationOffset;
            this.directionHandler = directionHandler;
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

        boolean needsRotation() {
            return rotationOffset != 0;
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

        int getCurrentRotation() {
            return currentRotation;
        }

        boolean hasNextScroll() {
            return scrollIndex < scrollCount;
        }

        @Nullable
        Rect getCurrentScroll() {
            // TODO: create a rect based on scrollIndex
            return null;
        }

        @Nullable
        Rect getNextScroll() {
            scrollIndex = directionHandler.nextIndex(scrollIndex, scrollCount);
            // TODO: move rotation forward... right? Or should this be a separate setting?
            return null;
        }

        boolean isFirstScroll() {
            return scrollIndex == 0;
        }

        boolean isLastScroll() {
            return scrollIndex == scrollCount;
        }

        boolean hasNextFrame() {
            return frameIndex < frames.length;
        }

        @NonNull
        Bitmap getCurrentFrame() {
            return frames[frame];
        }

        @Nullable
        Bitmap getNextFrame() {
            frameIndex = directionHandler.nextIndex(frameIndex, frames.length);
            // TODO: calculate scrollCount by way of current frame width against scrollRate, account for edge to edge display
            scrollCount = 0;    // TODO: CALCULATE HERE
            scrollIndex = 0;
            // TODO: move rotation forward
            return frames[frameIndex];
        }

        boolean isFirstFrame() {
            return frameIndex == 0;
        }

        boolean isLastFrame() {
            return frameIndex == frames.length - 1;
        }

    }

    public static class JobBuilder {

        private Bitmap[] frames;
        private long frameRate = 100;
        private int scrollRate = 1;
        private int cycles = 0;
        private long minDuration = 0;
        private long maxDuration = 0;
        private int startingRotation = 0;
        private int rotationOffset = 0;
        private JobDirection jobDirection = FORWARD;
        private boolean loopingEnabled = true;
        private boolean shuffledEnabled = false;

        public JobBuilder withFrame(@NonNull final Bitmap frame) {
            if (null == frame) {
                throw new IllegalArgumentException("Frame must not be null.");
            }
            this.frames = new Bitmap[]{frame};
            return this;
        }

        public JobBuilder withFrames(@NonNull @Size(min=1) final Bitmap[] frames) {
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

        public JobBuilder withFrameRate(final long frameRate) {
            if (frameRate < 0) {
                throw new IllegalArgumentException("Frame rate must be zero or more.");
            }
            this.frameRate = frameRate;
            return this;
        }

        public JobBuilder withScrollRate(final int scrollRate) {
            if (scrollRate < 1) {
                throw new IllegalArgumentException("Scroll rate must be one or more.");
            }
            this.scrollRate = scrollRate;
            return this;
        }

        public JobBuilder withCycles(final int cycles) {
            if (cycles < 0) {
                throw new IllegalArgumentException("Cycle count must be zero or more.");
            }
            this.cycles = cycles;
            return this;
        }

        public JobBuilder withMinDuration(final long minDuration) {
            if (minDuration < 0) {
                throw new IllegalArgumentException("Min duration must be zero or more.");
            }
            // TODO: Should we check against / raise a lower maxDuration up to minDuration value here?
            this.minDuration = minDuration;
            return this;
        }

        public JobBuilder withMaxDuration(final long maxDuration) {
            if (maxDuration < 0) {
                throw new IllegalArgumentException("Max duration must be zero or more.");
            }
            this.maxDuration = maxDuration;
            return this;
        }

        public JobBuilder withStartingRotation(final int startingRotation) {
            this.startingRotation = startingRotation;
            return this;
        }

        public JobBuilder withRotationOffset(final int rotationOffset) {
            this.rotationOffset = rotationOffset;
            return this;
        }

        public JobBuilder withJobDirection(final JobDirection jobDirection) {
            if (null == jobDirection) {
                throw new IllegalArgumentException("Job direction must not be null.");
            }
            this.jobDirection = jobDirection;
            return this;
        }

        public JobBuilder withLoopingEnabled(final boolean loopingEnabled) {
            this.loopingEnabled = loopingEnabled;
            return this;
        }

        public JobBuilder withShuffledEnabled(final boolean shuffledEnabled) {
            this.shuffledEnabled = shuffledEnabled;
            return this;
        }

        @NonNull
        public Job build() {
            if (null == frames) {
                throw new IllegalArgumentException("Frames are not set!");
            }
            final JobDirectionHandler jobDirectionHandler =
                    new JobDirectionHandler(jobDirection, loopingEnabled, shuffledEnabled);
            return new Job(frames, frameRate, scrollRate, cycles, minDuration, maxDuration,
                    startingRotation, rotationOffset, jobDirectionHandler);
        }

    }

}
