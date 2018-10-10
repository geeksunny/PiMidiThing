package com.radicalninja.pimidithing.ui.display;

import android.graphics.Bitmap;

import com.eon.androidthings.sensehatdriverlibrary.devices.LedMatrix;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Maintains and increments frame and scroll indexes for the Job class.
 */
class JobCycler {

    private final int frameCount;
    private final int frameCycleLength;
    private final int scrollRate;
    private final JobDirection jobDirection;
    private final JobDirection scrollDirection;

    private int frameCyclePos = -1;
    private int scrollIndex = -1;
    private int scrollCount = -1;
    private int scrollCyclePos = -1;
    private int scrollWidth = -1;

    private int frameIndex;
    private int remainingCycles;

    JobCycler(final int frameCount,
              final int cycleCount,
              final int scrollRate,
              @NonNull final JobDirection jobDirection,
              @Nullable final JobDirection scrollDirection) {

        if (null == jobDirection) {
            throw new IllegalArgumentException("JobDirection must not be null.");
        }
        // TODO: Check for valid values before setting anything?
        this.frameCount = frameCount;
        this.frameCycleLength = jobDirection.getCycleLength(frameCount);
        this.frameIndex = jobDirection.getFirstIndex(frameCount) - jobDirection.getStepLength();
        this.remainingCycles = cycleCount;
        this.scrollRate = scrollRate;
        this.jobDirection = jobDirection;
        // If scrollDirection is null, we share with jobDirection. TODO: Should this be a sepearate copy, instead?
        this.scrollDirection = (null != scrollDirection) ? scrollDirection : jobDirection;
    }

    /**
     * Reset the scroll data to the given current frame.
     * @param currentFrame The Bitmap to be scrolled.
     */
    void initScroll(@NonNull final Bitmap currentFrame) {
        scrollWidth = currentFrame.getWidth();
        final int itemCount = (scrollWidth / scrollRate) + ((scrollWidth % scrollRate > 0) ? 1 : 0);
        scrollCount = scrollDirection.getCycleLength(itemCount);
        scrollIndex = scrollDirection.getFirstIndex(itemCount) - scrollDirection.getStepLength();
        scrollCyclePos = -1;
    }

    /**
     * Check if there is another cycle after the current one ends.
     * @return True if another cycle is available to execute.
     */
    boolean hasNextCycle() {
        return remainingCycles > 0;
    }

    /**
     * Get the current frame index, as prepared by a prior call to getNextFrameIndex().
     * @return An integer representing the current frame index.
     */
    int getCurrentFrameIndex() {
        return frameIndex;
    }

    /**
     * Step the frame forward and return the next frame index.
     * @return An integer representing the next frame to display.
     */
    int getNextFrameIndex() {
        // Increment frame / cycle index values.
        frameCyclePos++;
        if (frameCyclePos == frameCycleLength) {
            remainingCycles--;
            // Reached the end of the cycle. Resetting values for the next cycle.
            // (Theoretically, the JobDirection object would have safely returned us to the
            //      starting frame index, but we're going to reset it just in case.)
            frameCyclePos = 0;
            frameIndex = jobDirection.getFirstIndex(frameCount);
        } else {
            // Proceed to the next position of this cycle.
            frameIndex = jobDirection.nextIndex(frameIndex, frameCount);
        }
        return frameIndex;
    }

    /**
     * Step the scroll forward and return the next scroll offset.
     * @return An integer representing the x offset to display.
     * @// TODO: The logic needs to be updated for vertical scrolling in the future.
     */
    int getNextScrollOffset() {
        scrollIndex = jobDirection.nextIndex(scrollIndex, scrollCount - 1);
        final int offset = scrollIndex * scrollRate;
        if ((offset + LedMatrix.WIDTH) > scrollWidth) {
            return scrollWidth - LedMatrix.WIDTH;
        } else {
            return offset;
        }
    }

    /**
     * Check if there is any frames remaining.
     * @return True if one or more frames remain.
     */
    boolean hasNextFrame() {
        return (frameCyclePos < frameCycleLength) && hasNextCycle();
    }

    /**
     * Check if there is any scrolls remaining.
     * @return True if one or more scrolls remain.
     */
    boolean hasNextScroll() {
        return scrollCyclePos < scrollCount;
    }

}
