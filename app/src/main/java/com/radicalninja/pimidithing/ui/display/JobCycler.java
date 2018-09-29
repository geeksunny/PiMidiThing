package com.radicalninja.pimidithing.ui.display;

import android.graphics.Bitmap;

import com.eon.androidthings.sensehatdriverlibrary.devices.LedMatrix;

import androidx.annotation.NonNull;

/**
 * Maintains and increments frame and scroll indexes for the Job class.
 */
public class JobCycler {

    private final int frameCount;
    private final int cycleCount;
    private final int scrollRate;
    private final JobDirection jobDirection;

    private int frameIndex = -1;
    private int scrollIndex = -1;
    private int scrollCount = 0;
    private int currentCycle = -1;  // TODO: start off at 1??

    // TODO: Implement logic for looping / shuffled indexes
    private boolean looping;
    private boolean shuffled;

    JobCycler(final int frameCount,
              final int cycleCount,
              final int scrollRate,
              @NonNull final JobDirection jobDirection,
              final boolean looping,
              final boolean shuffled) {

        if (null == jobDirection) {
            throw new IllegalArgumentException("JobDirection must not be null.");
        }
        // TODO: Check for valid values before setting anything?
        this.frameCount = frameCount;   // TODO: Run frameCount - 1 through jobDirection.totalIndexes.
        this.cycleCount = cycleCount;
        this.scrollRate = scrollRate;
        this.jobDirection = jobDirection;
        this.looping = looping;
        this.shuffled = shuffled;
    }

    void initScroll(@NonNull final Bitmap currentFrame) {
        final int width = currentFrame.getWidth();
        if (width > LedMatrix.WIDTH) {
            // TODO: clone or share logic with getCurrentScrollOffset for handling incomplete end offsets
            // TODO: store int value with incomplete width?
            scrollCount = (width / scrollRate) + ((width % scrollRate > 0) ? 1 : 0);
        } else {
            scrollCount = 0;
        }
    }

    // TODO: Is this necessary? nextFrameIndex returns frameIndex.
    int getCurrentFrameIndex() {
        return frameIndex;
    }

    // TODO: Is this necessary? nextScrollIndex returns scrollIndex.
    int getCurrentScrollIndex() {
        return scrollIndex;
    }

    int getCurrentScrollOffset() {
        // TODO: Handle scrollCount math for incomplete frames at the end!
        return scrollIndex * scrollRate;
    }

    int getScrollCount() {
        return scrollCount;
    }

    boolean hasNextCycle() {
        return currentCycle <= cycleCount;
    }

    boolean hasNextFrame() {
        // TODO check indexes. Consider if hasNextCycle logic is true as well.
        return false;
    }

    boolean hasNextScroll() {
        // TODO: Check indexes. Consider if hasNextCycle logic is true as well.
        return false;
    }

    int nextFrameIndex() {
        // TODO: increment frame, check cycle for increment
//        return jobDirection.nextIndex(currentIndex, lastIndex);

        // TODO Cycle count logic here!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // TODO: currentCycle needs to incrememnt on the start of each cycle. A cycle length is the number returned by jobCycler.totalIndexes
        // TODO: if is a single scrollable frame, totalIndexes will be calculated against scrollCount

        return frameIndex;
    }

    int nextScrollIndex() {
        // TODO increment scrollIndex, check against scrollCount
//        return jobDirection.nextIndex(currentIndex, lastIndex);
        return scrollIndex;
    }

    public int nextValue(final int currentValue, final int valueOffset) {
        return currentValue + (jobDirection.getStepLength() * valueOffset);
    }

}
