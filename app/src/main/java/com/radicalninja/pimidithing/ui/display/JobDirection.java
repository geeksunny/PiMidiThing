package com.radicalninja.pimidithing.ui.display;

/**
 * TODO
 */
public interface JobDirection {

    /**
     * TODO
     * @param itemCount
     * @return
     */
    int getCycleLength(final int itemCount);

    /**
     * Determine the starting index for the given itemCount.
     * @param itemCount
     * @return
     */
    int getFirstIndex(final int itemCount);

    /**
     * TODO
     * @return
     */
    int getStepLength();

    /**
     * TODO
     * @param currentIndex
     * @param itemCount
     * @return
     */
    int nextIndex(final int currentIndex, final int itemCount);

}
