package com.radicalninja.pimidithing.ui.display;

import android.graphics.Bitmap;

import com.eon.androidthings.sensehatdriverlibrary.devices.LedMatrix;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Maintains and increments frame and scroll indexes for the Job class.
 */
class JobCycler {

    private final int frameCycleLength;
    private final int cycleCount;
    private final int scrollRate;
    private final JobDirection jobDirection;
    private final JobDirection scrollDirection;

    private int scrollIndex = -1;
    private int scrollCount = -1;
    private int scrollCyclePos = -1;
    private int frameCyclePos = 0;
    private int currentCycle = 1;

    private int frameIndex;

    JobCycler(final int frameCount,
              final int cycleCount,
              final int scrollRate,
              @NonNull final JobDirection jobDirection,
              @Nullable final JobDirection scrollDirection) {

        if (null == jobDirection) {
            throw new IllegalArgumentException("JobDirection must not be null.");
        }
        // TODO: Check for valid values before setting anything?
        this.frameCycleLength = jobDirection.getCycleLength(frameCount);
        this.frameIndex = jobDirection.getFirstIndex(frameCount);
        this.cycleCount = cycleCount;
        this.scrollRate = scrollRate;
        this.jobDirection = jobDirection;
        // If scrollDirection is null, we share with jobDirection. TODO: Should this be a sepearate copy, instead?
        this.scrollDirection = (null != scrollDirection) ? scrollDirection : jobDirection;
    }

    void initScroll(@NonNull final Bitmap currentFrame) {
        final int width = currentFrame.getWidth();
        if (width > LedMatrix.WIDTH) {
            //scrollCount = (width / scrollRate) + ((width % scrollRate > 0) ? 1 : 0);
            // TODO: Use math above to implement incomplete final scrolls.
            final int itemCount = width / scrollRate;   // TODO: This math is incorrect, needs to take WIDTH into account
            scrollCount = scrollDirection.getCycleLength(itemCount);
            scrollIndex = scrollDirection.getFirstIndex(itemCount) - scrollDirection.getStepLength();
            scrollCyclePos = -1;
        } else {
            scrollCount = 0;
            scrollIndex = 0;
            scrollCyclePos = 0;
        }
    }

    int getScrollCount() {
        return scrollCount;
    }

    boolean hasNextCycle() {
        return cycleCount == 0 || currentCycle <= cycleCount;
    }

    int getCurrentFrameIndex() {
        return frameIndex;
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
        // TODO: use frame cycle position for this!!
        return (frameIndex < frameCycleLength) && hasNextCycle();
    }

    boolean hasNextScroll() {
        // TODO: need to use a scrollPosition value here rather than scrollIndex. Logic wouldn't work for reverse direction
        return scrollCyclePos < scrollCount;
    }

}
