package com.radicalninja.pimidithing.ui.display;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.eon.androidthings.sensehatdriverlibrary.devices.LedMatrix;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Size;

public class Job {

    // TODO: increment cycle count somewhere in the scroll / frame logic
    // TODO: implement support for vertical scrolling
    // TODO: Should there be rotation implemented for scrolling frames?

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

    boolean needsScrolling() {
        return frames[frameIndex].getWidth() > LedMatrix.WIDTH && frameRate > 0;
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

    @NonNull
    Rect getNextScroll() {
        scrollIndex = directionHandler.nextIndex(scrollIndex, scrollCount);
        // TODO!!
        return null;
    }

    boolean isLastScroll() {
        return scrollIndex == scrollCount;
    }

    boolean hasNextFrame() {
        return frameIndex < frames.length;
    }

    @NonNull
    Bitmap getCurrentFrame() {
        // TODO: Is this method necessary?
        return frames[frame];
    }

    @NonNull
    Bitmap getNextFrame() {
        frameIndex = directionHandler.nextIndex(frameIndex, frames.length);
        final Bitmap frame = frames[frameIndex];
        if (needsScrolling()) {
            final int width = frame.getWidth();
            scrollCount = (width / scrollRate) + ((width % scrollRate > 0) ? 1 : 0);
        } else {
            scrollCount = 0;
        }
        scrollIndex = 0;
        // TODO: move rotation forward
        return frame;
    }

    boolean isFirstFrame() {
        return frameIndex == 0;
    }

    boolean isLastFrame() {
        return frameIndex == frames.length - 1;
    }

    public static class Builder {

        private Bitmap[] frames;
        private long frameRate = 100;
        private int scrollRate = 1;
        private int cycles = 0;
        private long minDuration = 0;
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

        public Builder withMinDuration(final long minDuration) {
            if (minDuration < 0) {
                throw new IllegalArgumentException("Min duration must be zero or more.");
            }
            // TODO: Should we check against / raise a lower maxDuration up to minDuration value here?
            this.minDuration = minDuration;
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
            final JobDirectionHandler jobDirectionHandler =
                    new JobDirectionHandler(jobDirection, loopingEnabled, shuffledEnabled);
            return new Job(frames, frameRate, scrollRate, cycles, minDuration, maxDuration,
                    startingRotation, rotationOffset, jobDirectionHandler);
        }

    }

}
