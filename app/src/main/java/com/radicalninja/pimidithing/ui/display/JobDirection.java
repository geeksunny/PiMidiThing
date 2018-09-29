package com.radicalninja.pimidithing.ui.display;

/**
 * TODO
 */
public interface JobDirection {

    /**
     * TODO
     * @param lastIndex
     * @return
     */
    int totalIndexes(final int lastIndex);

    /**
     * TODO
     * @return
     */
    int getStepLength();

    /**
     * TODO
     * @param currentIndex
     * @param lastIndex
     * @return
     */
    int nextIndex(final int currentIndex, final int lastIndex);

}
