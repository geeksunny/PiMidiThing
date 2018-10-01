package com.radicalninja.pimidithing.ui.display;

import android.graphics.Bitmap;

import com.eon.androidthings.sensehatdriverlibrary.devices.LedMatrix;

import androidx.annotation.NonNull;

/**
 * Maintains and increments frame and scroll indexes for the Job class.
 */
class JobCycler {

    private final int frameCount;
    private final int cycleCount;
    private final int scrollRate;
    private final JobDirection jobDirection;

    private int frameIndex = -1;
    private int scrollIndex = -1;
    private int scrollCount = 0;
    private int cyclePosition = -1;
    private int currentCycle = -1;  // TODO: start off at 1??

    // TODO: Implement logic for shuffled indexes
    private boolean shuffled;

    JobCycler(final int frameCount,
              final int cycleCount,
              final int scrollRate,
              @NonNull final JobDirection jobDirection,
              final boolean shuffled) {

        if (null == jobDirection) {
            throw new IllegalArgumentException("JobDirection must not be null.");
        }
        // TODO: Check for valid values before setting anything?
        this.frameCount = jobDirection.getCycleLength(frameCount);
        // TODO: if cycleCount is 0, hasNextCycle should always be true.
        this.cycleCount = cycleCount;
        this.scrollRate = scrollRate;
        this.jobDirection = jobDirection;
        this.shuffled = shuffled;
    }

    void initScroll(@NonNull final Bitmap currentFrame) {
        final int width = currentFrame.getWidth();
        if (width > LedMatrix.WIDTH) {
            scrollCount = (width / scrollRate) + ((width % scrollRate > 0) ? 1 : 0);
        } else {
            scrollCount = 0;
        }
    }

    int getScrollCount() {
        return scrollCount;
    }

    boolean hasNextCycle() {
        return currentCycle <= cycleCount;
    }

    int getNextFrameIndex() {
        // TODO: increment frame, check cycle for increment
//        return jobDirection.nextIndex(currentIndex, lastIndex);

        // TODO Cycle count logic here!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // TODO: currentCycle needs to incrememnt on the start of each cycle. A cycle length is the number returned by jobCycler.getCycleLength
        // TODO: if is a single scrollable frame, getCycleLength will be calculated against scrollCount

        return frameIndex;
    }

    int getNextScrollOffset() {
        scrollIndex = jobDirection.nextIndex(scrollIndex, scrollCount - 1);
        return scrollIndex * scrollRate;
    }

    boolean hasNextFrame() {
        return (frameIndex < frameCount) && hasNextCycle();
    }

    boolean hasNextScroll() {
        // TODO: need to use a scrollPosition value here rather than scrollIndex. Logic wouldn't work for reverse direction
        return scrollIndex < scrollCount;
    }

    int nextValue(final int currentValue, final int valueOffset) {
        return currentValue + (jobDirection.getStepLength() * valueOffset);
    }

}
