package com.radicalninja.pimidithing.ui.display;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.SystemClock;

import com.eon.androidthings.sensehatdriverlibrary.devices.LedMatrix;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Size;

public class Job {

    // TODO: implement support for vertical scrolling
    // TODO: Should there be rotation implemented for scrolling frames?

    private final Bitmap[] frames;
    private final long frameRate;
    private final long maxDuration;
    private final int rotationOffset;
    private final JobCycler jobCycler;

    private long expirationTime = 0;
    private boolean stopped = false;

    private int currentRotation;

    private Job(final Bitmap[] frames,
                final long frameRate,
                final long maxDuration,
                final int startingRotation,
                final int rotationOffset,
                final JobCycler jobCycler) {

        this.frames = frames;
        this.frameRate = frameRate;
        this.maxDuration = maxDuration;
        this.currentRotation = startingRotation;
        this.rotationOffset = rotationOffset;
        this.jobCycler = jobCycler;
    }

    void recycle() {
        for (final Bitmap bitmap : frames) {
            bitmap.recycle();
        }
    }

    long getSleepDuration() {
        return (frameRate > 0) ? frameRate : maxDuration;
    }

    boolean needsScrolling() {
        return jobCycler.getScrollCount() > 1;
    }

    boolean needsRotation() {
        return rotationOffset != 0;
    }

    void stop() {
        this.stopped = true;
    }

    void start() {
        if (expirationTime > 0) {
            // TODO: log / throw exception?
            return;
        }
        expirationTime = SystemClock.elapsedRealtime() + maxDuration;
    }

    boolean isStarted() {
        return expirationTime > 0;
    }

    boolean isAlive() {
        return !stopped && jobCycler.hasNextCycle() && (SystemClock.elapsedRealtime() < expirationTime);
    }

    int getCurrentRotation() {
        return currentRotation;
    }

    boolean hasNextScroll() {
        return jobCycler.hasNextScroll();
    }

    @NonNull
    Rect getNextScroll() {
        final int scrollIndex = jobCycler.nextScrollIndex();
        // TODO!!
        return null;
    }

    boolean hasNextFrame() {
        return jobCycler.hasNextFrame();
    }

    @NonNull
    Bitmap getNextFrame() {
        final int frameIndex = jobCycler.nextFrameIndex();
        final Bitmap frame = frames[frameIndex];
        jobCycler.initScroll(frame);
        currentRotation = jobCycler.nextValue(currentRotation, rotationOffset);
        return frame;
    }

    public static class Builder {

        private Bitmap[] frames;
        private long frameRate = 100;
        private int scrollRate = 1;
        private int cycles = 0;
        private long maxDuration = 0;
        private int startingRotation = 0;
        private int rotationOffset = 0;
        private JobDirection jobDirection = JobDirections.FORWARD;
        private boolean loopingEnabled = true;
        private boolean shuffledEnabled = false;

        public Builder withFrame(@NonNull final Bitmap frame) {
            if (null == frame) {
                throw new IllegalArgumentException("Frame must not be null.");
            }
            this.frames = new Bitmap[]{frame};
            return this;
        }

        public Builder withFrames(@NonNull @Size(min=1) final Bitmap[] frames) {
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

        public Builder withFrameRate(final long frameRate) {
            if (frameRate < 0) {
                throw new IllegalArgumentException("Frame rate must be zero or more.");
            }
            this.frameRate = frameRate;
            return this;
        }

        public Builder withScrollRate(final int scrollRate) {
            if (scrollRate < 1) {
                throw new IllegalArgumentException("Scroll rate must be one or more.");
            }
            this.scrollRate = scrollRate;
            return this;
        }

        public Builder withCycles(final int cycles) {
            if (cycles < 0) {
                throw new IllegalArgumentException("Cycle count must be zero or more.");
            }
            this.cycles = cycles;
            return this;
        }

        public Builder withMaxDuration(final long maxDuration) {
            if (maxDuration < 0) {
                throw new IllegalArgumentException("Max duration must be zero or more.");
            }
            this.maxDuration = maxDuration;
            return this;
        }

        public Builder withStartingRotation(final int startingRotation) {
            this.startingRotation = startingRotation;
            return this;
        }

        public Builder withRotationOffset(final int rotationOffset) {
            this.rotationOffset = rotationOffset;
            return this;
        }

        public Builder withJobDirection(final JobDirection jobDirection) {
            if (null == jobDirection) {
                throw new IllegalArgumentException("Job direction must not be null.");
            }
            this.jobDirection = jobDirection;
            return this;
        }

        public Builder withLoopingEnabled(final boolean loopingEnabled) {
            this.loopingEnabled = loopingEnabled;
            return this;
        }

        public Builder withShuffledEnabled(final boolean shuffledEnabled) {
            this.shuffledEnabled = shuffledEnabled;
            return this;
        }

        @NonNull
        public Job build() {
            if (null == frames) {
                throw new IllegalArgumentException("Frames are not set!");
            }
            final JobCycler jobCycler = new JobCycler(
                    frames.length, cycles, scrollRate, jobDirection, loopingEnabled, shuffledEnabled);
            return new Job(frames, frameRate, maxDuration,
                    startingRotation, rotationOffset, jobCycler);
        }

    }

}
