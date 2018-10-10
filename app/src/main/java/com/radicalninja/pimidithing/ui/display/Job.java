package com.radicalninja.pimidithing.ui.display;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.SystemClock;

import com.eon.androidthings.sensehatdriverlibrary.devices.LedMatrix;
import com.radicalninja.pimidithing.util.ValueException;

import java.util.Arrays;
import java.util.Iterator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;

public class Job implements Iterable<Job.Position> {

    // TODO: implement support for vertical scrolling
    // TODO: Should there be rotationOffset implemented for scrolling frames?

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

    int getCurrentRotation() {
        return currentRotation;
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

    @NonNull
    @Override
    public Iterator<Position> iterator() {
        if (!isStarted()) {
            start();
        }
        return new PositionIterator(this);
    }

    static class Position {

        final Bitmap frame;
        final Rect drawBounds;
        final long sleepDuration;
        final int rotationOffset;

        private Position(@NonNull final Bitmap frame,
                         final long sleepDuration,
                         final int rotationOffset) {

            this(frame, new Rect(0, 0, LedMatrix.WIDTH, LedMatrix.HEIGHT),
                    sleepDuration, rotationOffset);
        }

        private Position(@NonNull final Bitmap frame,
                         @NonNull final Rect drawBounds,
                         final long sleepDuration,
                         final int rotationOffset) {

            this.frame = frame;
            this.drawBounds = drawBounds;
            this.sleepDuration = (sleepDuration > 0) ? sleepDuration : 0;
            this.rotationOffset = rotationOffset;
        }

        boolean needsRotation() {
            return rotationOffset != 0;
        }

        int getRotation() {
            return rotationOffset;
        }

    }

    static class PositionIterator implements Iterator<Position> {

        private final Job job;
        private final JobCycler jobCycler;

        PositionIterator(final Job job) {
            this.job = job;
            this.jobCycler = job.jobCycler;
        }

        @Override
        public boolean hasNext() {
            return (jobCycler.hasNextScroll() || jobCycler.hasNextFrame()) && job.isAlive();
        }

        @Override
        public Position next() {
            if (jobCycler.hasNextScroll()) {
                return nextScroll();
            } else if (jobCycler.hasNextFrame()) {
                return nextFrame();
            }
            return null;
        }

        @NonNull
        private Position nextScroll() {
            final Bitmap frame = job.frames[jobCycler.getCurrentFrameIndex()];
            // Currently, only horizontal scrolling is supported.
            final int offset = jobCycler.getNextScrollOffset();
            final Rect bounds = new Rect(offset, 0, LedMatrix.WIDTH, LedMatrix.HEIGHT);
            return new Position(frame, bounds, job.frameRate, job.rotationOffset);
        }

        @NonNull
        private Position nextFrame() {
            final Bitmap frame = job.frames[jobCycler.getNextFrameIndex()];
            jobCycler.initScroll(frame);
            return (jobCycler.hasNextScroll())
                    ? nextScroll()
                    : new Position(frame, job.frameRate, job.rotationOffset);
        }

    }

    public static class Builder {

        private static final long MINIMUM_FRAME_RATE = 100;
        private static final int MINIMUM_SCROLL_RATE = 1;
        private static final int MINIMUM_CYCLES = 1;
        private static final long MINIMUM_MAX_DURATION = 0;

        private Bitmap[] frames;
        private long frameRate = MINIMUM_FRAME_RATE;
        private int scrollRate = MINIMUM_SCROLL_RATE;
        private int cycles = MINIMUM_CYCLES;
        private long maxDuration = MINIMUM_MAX_DURATION;
        private int startingRotation = 0;
        private int rotationOffset = 0;
        private JobDirection jobDirection = JobDirections.FORWARD;
        private JobDirection scrollDirection;

        public Builder withFrame(@NonNull final Bitmap frame) {
            if (null == frame) {
                throw new ValueException.ValueIsNullException("Frame");
            }
            this.frames = new Bitmap[]{frame};
            return this;
        }

        public Builder withFrames(@NonNull @Size(min=1) final Bitmap[] frames) {
            if (null == frames) {
                throw new ValueException.ValueIsNullException("Frames");
            } else if (frames.length == 0) {
                throw new ValueException("Frames must not be empty.");
            }
            for (final Bitmap frame : frames) {
                if (null == frame) {
                    throw new ValueException("Found null entry in frames.");
                }
            }
            this.frames = Arrays.copyOf(frames, frames.length);
            return this;
        }

        public Builder withFrameRate(final long frameRate) {
            if (frameRate < MINIMUM_FRAME_RATE) {
                throw new ValueException.BelowMinimumValueException("Frame rate", MINIMUM_FRAME_RATE);
            }
            this.frameRate = frameRate;
            return this;
        }

        public Builder withScrollRate(final int scrollRate) {
            if (scrollRate < MINIMUM_SCROLL_RATE) {
                throw new ValueException.BelowMinimumValueException("Scroll rate", MINIMUM_SCROLL_RATE);
            }
            this.scrollRate = scrollRate;
            return this;
        }

        public Builder withCycles(final int cycles) {
            if (cycles < 1) {
                throw new ValueException.BelowMinimumValueException("Cycle count", MINIMUM_CYCLES);
            }
            this.cycles = cycles;
            return this;
        }

        public Builder withMaxDuration(final long maxDuration) {
            if (maxDuration < 0) {
                throw new ValueException.BelowMinimumValueException("Max duration", MINIMUM_MAX_DURATION);
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

        public Builder withJobDirection(@NonNull final JobDirection jobDirection) {
            if (null == jobDirection) {
                throw new ValueException.ValueIsNullException("Job direction");
            }
            this.jobDirection = jobDirection;
            return this;
        }

        public Builder withScrollDirection(@Nullable final JobDirection scrollDirection) {
            this.scrollDirection = scrollDirection;
            return this;
        }

        @NonNull
        public Job build() {
            if (null == frames) {
                throw new ValueException("Frames are not set!");
            }
            final JobCycler jobCycler =
                    new JobCycler(frames.length, cycles, scrollRate, jobDirection, scrollDirection);
            return new Job(
                    frames, frameRate, maxDuration, startingRotation, rotationOffset, jobCycler);
        }

    }

}
